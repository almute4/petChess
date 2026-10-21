package org.chess.mainservice.service;

import org.chess.mainservice.dto.UserUpsertDto;
import org.chess.mainservice.dto.UserGetDto;

import java.util.List;

public interface UserService {
    public List<UserGetDto> getAllUsers();

    public UserGetDto createUser(UserUpsertDto userUpsertDto);

    public UserGetDto getUserById(Long id);
}
