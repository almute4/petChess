package org.chess.mainservice.dto;

import java.util.Objects;

public class RefreshTokenDto {
    private String refreshToken;

    public RefreshTokenDto(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RefreshTokenDto{");
        sb.append("refreshToken='").append(refreshToken).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RefreshTokenDto that)) return false;
        return Objects.equals(refreshToken, that.refreshToken);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(refreshToken);
    }
}
