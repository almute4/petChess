package org.chess.mainservice.service.impl;

import org.chess.mainservice.dto.UserCreateDto;
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

    public UserGetDto createUser(UserCreateDto userCreateDto) {
        User user = userMapper.toEntity(userCreateDto);
        User userCreated = userRepository.save(user);

        return userMapper.toGetDto(userCreated);
    }
}