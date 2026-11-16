package org.chess.mainservice.service;

import org.chess.mainservice.dto.GameDto;

public record GameChanged(String type, GameDto game) {
}
