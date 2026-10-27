package org.chess.mainservice.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtService {
    @Value("jwt-secret-temp")
    private String jwtSecret;
}
