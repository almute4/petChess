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

    public UserGetDto toDto(User user) {
        return new UserGetDto(user.getUsername(), user.getEmail());
    }
}
