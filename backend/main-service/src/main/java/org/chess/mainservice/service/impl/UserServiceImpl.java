package org.chess.mainservice.service.impl;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.chess.mainservice.dto.UserUpsertDto;
import org.chess.mainservice.dto.UserGetDto;
import org.chess.mainservice.model.User;
import org.chess.mainservice.repository.UserRepository;
import org.chess.mainservice.service.UserService;
import org.chess.mainservice.utils.UserMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
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
        

        User newUser = userMapper.toEntity(userUpsertDto);
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
                userRepository.existsByUsername(userUpsertDto.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }

        if (!user.getEmail().equals(userUpsertDto.getEmail()) &&
                userRepository.existsByEmail(userUpsertDto.getEmail())) {
            throw new IllegalArgumentException("Email is already taken");
        }

        user.setUsername(userUpsertDto.getUsername());
        user.setEmail(userUpsertDto.getEmail());
        user.setPasswordHash(userUpsertDto.getPasswordHash());

        return userMapper.toGetDto(user);
    }

    private Boolean isUsernameOrEmailUsed(String username, String email) {
        return userRepository.existsByUsername(username) || userRepository.existsByEmail(email);
    }
}
