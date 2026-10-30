package org.chess.mainservice.service;

import org.chess.mainservice.dto.*;

import javax.naming.AuthenticationException;
import java.util.List;

public interface UserService {
    JwtAuthenticationDto signIn(UserCredentialsDto userCredentialsDto) throws AuthenticationException;

    JwtAuthenticationDto refreshToken(RefreshTokenDto refreshTokenDto) throws Exception;

    List<UserGetDto> getAllUsers();

    UserGetDto createUser(UserUpsertDto userUpsertDto);

    UserGetDto getUserById(Long id);

    UserGetDto updateUserById(Long id, UserUpsertDto userUpsertDto);
}
