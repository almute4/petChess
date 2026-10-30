package org.chess.mainservice.service.impl;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.chess.mainservice.dto.*;
import org.chess.mainservice.exception.DuplicateResourceException;
import org.chess.mainservice.model.User;
import org.chess.mainservice.repository.UserRepository;
import org.chess.mainservice.security.jwt.JwtService;
import org.chess.mainservice.service.UserService;
import org.chess.mainservice.utils.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public JwtAuthenticationDto signIn(UserCredentialsDto userCredentialsDto) throws AuthenticationException {
        User user = findByCredentials(userCredentialsDto);
        return jwtService.generateAuthToken(user.getEmail());
    }

    @Override
    public JwtAuthenticationDto refreshToken(RefreshTokenDto refreshTokenDto) throws Exception {
        String refreshToken = refreshTokenDto.getRefreshToken();
        if (refreshToken != null && jwtService.validateJwtToken(refreshToken)) {
            User user = findByEmail(jwtService.getEmailFromToken(refreshToken));
            return jwtService.refreshBaseToken(user.getEmail(), refreshToken);
        }
        throw new AuthenticationException("Invalid refresh token");
    }

    public List<UserGetDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserGetDto> userDtos = new ArrayList<>();

        for (User user : users) {
            userDtos.add(userMapper.toGetDto(user));
        }

        return userDtos;
    }

    public UserGetDto createUser(UserUpsertDto userUpsertDto) {
        if (isEmailUsed(userUpsertDto.getEmail())) {
            throw new DuplicateResourceException("Email: " + userUpsertDto.getEmail() + " is already taken");
        }

        if (isUsernameUsed(userUpsertDto.getUsername())) {
            throw new DuplicateResourceException("Username '" + userUpsertDto.getUsername() + "' is already registered");
        }

        User newUser = userMapper.toEntity(userUpsertDto);
        newUser.setPasswordHash(passwordEncoder.encode(newUser.getPasswordHash()));

        User userCreated = userRepository.save(newUser);

        return userMapper.toGetDto(userCreated);
    }

    public UserGetDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        UserGetDto userGetDto = userMapper.toGetDto(user);
        return userGetDto;
    }


    @Override
    @Transactional
    public UserGetDto updateUserById(Long id, UserUpsertDto userUpsertDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        if (!user.getUsername().equals(userUpsertDto.getUsername()) &&
                isUsernameUsed(userUpsertDto.getUsername())) {
            throw new DuplicateResourceException("Username '" + userUpsertDto.getUsername() + "' is already registered");
        }

        if (!user.getEmail().equals(userUpsertDto.getEmail()) &&
                isEmailUsed(userUpsertDto.getEmail())) {
            throw new DuplicateResourceException("Email '" + userUpsertDto.getEmail() + "' is already registered");
        }

        user.setUsername(userUpsertDto.getUsername());
        user.setEmail(userUpsertDto.getEmail());
        user.setPasswordHash(passwordEncoder.encode(userUpsertDto.getPasswordHash()));

        return userMapper.toGetDto(user);
    }

    private Boolean isUsernameUsed(String username) {
        return userRepository.existsByUsername(username);
    }

    private Boolean isEmailUsed(String email) {
        return userRepository.existsByEmail(email);
    }

    private User findByCredentials(UserCredentialsDto userCredentialsDto) throws AuthenticationException {
        Optional<User> optionalUser = userRepository.findByEmail(userCredentialsDto.getEmail());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (passwordEncoder.matches(userCredentialsDto.getPassword(), user.getPasswordHash())) {
                return user;
            }
        }
        throw new AuthenticationException("Email or password is not correct");
    }

    private User findByEmail(String email) throws Exception {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new Exception(String.format("User with email %email not found", email)));
    }
}
