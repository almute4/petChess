import { useNavigate } from "react-router-dom";

interface Match {
  opponent: string;
  result: "Win" | "Loss" | "Draw";
  moves: number;
  date: string;
}

const matchHistory: Match[] = [
  { opponent: "rook_runner", result: "Win", moves: 32, date: "12 Oct 2026" },
  { opponent: "night_owl", result: "Loss", moves: 41, date: "11 Oct 2026" },
  { opponent: "pawn_star", result: "Draw", moves: 58, date: "11 Oct 2026" },
  { opponent: "castle_64", result: "Win", moves: 27, date: "10 Oct 2026" },
  { opponent: "green_knight", result: "Loss", moves: 36, date: "09 Oct 2026" },
];

function getResultClass(result: string) {
  switch (result) {
    case "Win":
      return "result-win";
    case "Loss":
      return "result-loss";
    case "Draw":
      return "result-draw";
    default:
      return "";
  }
}

function MatchHistory() {
  const navigate = useNavigate();

  return (
    <div className="history-container">
      <h1>Match History</h1>
      <table className="history-table">
        <thead>
          <tr>
            <th>Opponent</th>
            <th>Result</th>
            <th>Moves</th>
            <th>Date</th>
          </tr>
        </thead>
        <tbody>
          {matchHistory.map((match, index) => (
            <tr key={index}>
              <td>{match.opponent}</td>
              <td className={getResultClass(match.result)}>{match.result}</td>
              <td>{match.moves}</td>
              <td>{match.date}</td>
            </tr>
          ))}
        </tbody>
      </table>
      <button className="btn-back" onClick={() => navigate("/lobby")}>
        Back to Lobby
      </button>
    </div>
  );
}

export default MatchHistory;
