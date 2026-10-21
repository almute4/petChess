package org.chess.mainservice.service;

import org.chess.mainservice.dto.UserCreateDto;
import org.chess.mainservice.dto.UserGetDto;
import org.chess.mainservice.model.User;
import org.springframework.stereotype.Service;

import java.util.List;

public interface UserService {
    public List<UserGetDto> getAllUsers();

    public UserGetDto createUser(UserCreateDto userCreateDto);

    public UserGetDto getUserById(Long id);
}
