import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { createGame, joinGame, waitingGames, type GameState } from "../api/games";
import { getCurrentUser } from "../api/auth";
import { getErrorMessage } from "../api/client";

export default function Lobby() {
  const navigate = useNavigate();
  const [games, setGames] = useState<GameState[]>([]);
  const [userId, setUserId] = useState<number>();
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  const refresh = async () => {
    try { setGames(await waitingGames()); } catch (cause) { setError(getErrorMessage(cause)); }
  };
  useEffect(() => {
    void refresh();
    getCurrentUser().then(user => setUserId(user.id)).catch(cause => setError(getErrorMessage(cause)));
  }, []);
  const play = async (game?: GameState) => {
    setBusy(true);
    setError("");
    try {
      let state = game;
      if (!state) state = await createGame();
      else if (state.whitePlayer.id !== userId) state = await joinGame(state.id);
      navigate(`/game/${state.id}`);
    } catch (cause) { setError(getErrorMessage(cause)); }
    finally { setBusy(false); }
  };
  return (
    <div className="lobby-container"><div className="lobby-buttons">
      <h1>Lobby</h1>
      {error && <p role="alert">{error}</p>}
      <button className="lobby-btn play-btn" disabled={busy} onClick={() => void play()}>Create game</button>
      <button className="lobby-btn" onClick={() => void refresh()}>Refresh games</button>
      {games.map(game => (
        <button className="lobby-btn" key={game.id} disabled={busy || !userId} onClick={() => void play(game)}>
          #{game.id} · {game.whitePlayer.username} · {game.whitePlayer.id === userId ? "Open" : "Join"}
        </button>
      ))}
      <button className="lobby-btn" onClick={() => navigate("/history")}>Match History</button>
      <button className="lobby-btn" onClick={() => navigate("/settings")}>Account Settings</button>
    </div></div>
  );
}
