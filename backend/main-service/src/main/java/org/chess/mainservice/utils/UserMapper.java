package org.chess.mainservice.utils;

import org.chess.mainservice.dto.UserCreateDto;
import org.chess.mainservice.dto.UserGetDto;
import org.chess.mainservice.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public User toEntity(UserCreateDto userCreateDto) {
        return new User(userCreateDto.getUsername(), userCreateDto.getEmail(), userCreateDto.getPasswordHash());
    }

    public UserCreateDto toCreteDto(User user) {
        return new UserCreateDto(user.getUsername(), user.getEmail(), user.getPasswordHash());
    }

    public UserGetDto toGetDto(User user) {
        return new UserGetDto(user.getId(), user.getUsername(), user.getEmail());
    }
}
