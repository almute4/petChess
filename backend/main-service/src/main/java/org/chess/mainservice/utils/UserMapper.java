package org.chess.mainservice.utils;

import org.chess.mainservice.dto.UserUpsertDto;
import org.chess.mainservice.dto.UserGetDto;
import org.chess.mainservice.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public User toEntity(UserUpsertDto userUpsertDto) {
        return new User(userUpsertDto.getUsername(), userUpsertDto.getEmail(), userUpsertDto.getPasswordHash());
    }

    public UserUpsertDto toCreteDto(User user) {
        return new UserUpsertDto(user.getUsername(), user.getEmail(), user.getPasswordHash());
    }

    public UserGetDto toGetDto(User user) {
        return new UserGetDto(user.getId(), user.getUsername(), user.getEmail());
    }
}
