package org.chess.mainservice.service.impl;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.chess.mainservice.dto.UserUpsertDto;
import org.chess.mainservice.dto.UserGetDto;
import org.chess.mainservice.exception.DuplicateResourceException;
import org.chess.mainservice.model.User;
import org.chess.mainservice.repository.UserRepository;
import org.chess.mainservice.service.UserService;
import org.chess.mainservice.utils.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
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
        newUser.setPasswordHash(passwordEncoder.encode(userUpsertDto.getPasswordHash()));

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
}
