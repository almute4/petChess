package org.chess.mainservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record MoveRequest(
        @NotNull @Pattern(regexp = "[a-h][1-8]") String from,
        @NotNull @Pattern(regexp = "[a-h][1-8]") String to,
        @Pattern(regexp = "[qrbn]") String promotion) {
    public String uci() {
        return from + to + (promotion == null ? "" : promotion);
    }
}
