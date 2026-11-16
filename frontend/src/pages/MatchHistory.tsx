import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { myGames, type GameState } from "../api/games";
import { getErrorMessage } from "../api/client";

export default function MatchHistory() {
  const [games, setGames] = useState<GameState[]>([]);
  const [error, setError] = useState("");
  useEffect(() => {
    myGames().then(setGames).catch(cause => setError(getErrorMessage(cause)));
  }, []);
  return (
    <div className="history-container">
      <h1>Match History</h1>
      {error && <p role="alert">{error}</p>}
      <table className="history-table">
        <thead><tr><th>Game</th><th>Players</th><th>Result</th><th>Date</th></tr></thead>
        <tbody>{games.map(game => (
          <tr key={game.id}>
            <td><Link to={`/game/${game.id}`}>#{game.id}</Link></td>
            <td>{game.whitePlayer.username} / {game.blackPlayer?.username ?? "Waiting"}</td>
            <td>{game.result ?? game.status}</td>
            <td>{new Date(game.createdAt).toLocaleDateString()}</td>
          </tr>
        ))}</tbody>
      </table>
      <Link to="/lobby">Back to Lobby</Link>
    </div>
  );
}
