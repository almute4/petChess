import { request } from "./client";

export interface Player {
  id: number;
  username: string;
}

export interface PlayedMove {
  ply: number;
  uci: string;
  san: string;
  fenAfter: string;
}

export interface GameState {
  id: number;
  whitePlayer: Player;
  blackPlayer: Player | null;
  status: "WAITING" | "IN_PROGRESS" | "FINISHED";
  result: "WHITE_WIN" | "BLACK_WIN" | "DRAW" | null;
  resultReason: string | null;
  currentFen: string;
  turn: "WHITE" | "BLACK";
  moves: PlayedMove[];
  createdAt: string;
  finishedAt: string | null;
}

export interface MoveInput {
  from: string;
  to: string;
  promotion?: string;
}

export const getGame = (id: number) => request<GameState>(`/games/${id}`);
export const waitingGames = () => request<GameState[]>("/games");
export const myGames = () => request<GameState[]>("/games/my");
export const createGame = () => request<GameState>("/games", { method: "POST" });
export const joinGame = (id: number) => request<GameState>(`/games/${id}/join`, { method: "POST" });
export const resignGame = (id: number) => request<GameState>(`/games/${id}/resign`, { method: "POST" });
