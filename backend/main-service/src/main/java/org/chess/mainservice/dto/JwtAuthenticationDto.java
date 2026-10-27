package org.chess.mainservice.dto;

import java.util.Objects;

public class JwtAuthenticationDto {
    private String token;
    private String refreshToken;

    public JwtAuthenticationDto() {
    }

    public JwtAuthenticationDto(String token, String refreshToken) {
        this.token = token;
        this.refreshToken = refreshToken;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JwtAuthenticationDto{");
        sb.append("token='").append(token).append('\'');
        sb.append(", refreshToken='").append(refreshToken).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof JwtAuthenticationDto that)) return false;
        return Objects.equals(token, that.token) && Objects.equals(refreshToken, that.refreshToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(token, refreshToken);
    }
}
