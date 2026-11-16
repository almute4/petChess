package org.chess.mainservice.model;

public enum GameResultReason {
    CHECKMATE,
    STALEMATE,
    RESIGN,
    THREEFOLD_REPETITION,
    FIFTY_MOVE_RULE,
    INSUFFICIENT_MATERIAL
}
