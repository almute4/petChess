import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

const initialBoard = [
  ["♜", "♞", "♝", "♛", "♚", "♝", "♞", "♜"],
  ["", "♟", "♟", "♟", "", "♟", "♟", "♟"],
  ["♟", "", "", "", "", "", "", ""],
  ["", "♗", "", "", "♟", "", "", ""],
  ["", "", "", "", "♙", "", "", ""],
  ["", "", "", "", "", "♘", "", ""],
  ["♙", "♙", "♙", "♙", "", "♙", "♙", "♙"],
  ["♖", "♘", "♗", "♕", "♔", "", "", "♖"],
];

function Game() {
  const [myTime, setMyTime] = useState(582);
  const opponentTime = 582;
  const navigate = useNavigate();

  const formatTime = (seconds: number) => {
    const m = Math.floor(seconds / 60).toString().padStart(2, "0");
    const s = (seconds % 60).toString().padStart(2, "0");
    return `${m}:${s}`;
  };

  useEffect(() => {
    const interval = window.setInterval(() => {
      setMyTime((prev) => (prev > 0 ? prev - 1 : 0));
    }, 1000);
    return () => window.clearInterval(interval);
  }, []);

  const handleResign = () => {
    if (window.confirm("Are you sure you want to resign?")) {
      navigate("/lobby");
    }
  };

  return (
    <main className="game-container">
      <div className="game-layout">
        <div className="chessboard" role="img" aria-label="Chess board position after 3 moves">
          {initialBoard.flatMap((rank, row) =>
            rank.map((piece, col) => {
              const isWhite = ["♙", "♖", "♘", "♗", "♕", "♔"].includes(piece);
              const isPiece = piece !== "";
              return (
                <div
                  key={`${row}-${col}`}
                  className={`chess-square ${(row + col) % 2 === 0 ? "light" : "dark"}${isPiece && isWhite ? " white-piece" : ""}`}
                  aria-hidden="true"
                >
                  {piece}
                </div>
              );
            }),
          )}
        </div>

        <aside className="game-sidebar">
          <div className="game-timer opponent">{formatTime(opponentTime)}</div>
          <section className="moves-section">
            <h3>Moves</h3>
            <div className="moves-list">
              1. e4&nbsp;&nbsp; e5{"\n"}
              2. Nf3&nbsp; Nc6{"\n"}
              3. Bb5&nbsp; a6
            </div>
          </section>
          <div className="game-timer">{formatTime(myTime)}</div>
          <button className="btn-resign" onClick={handleResign}>Resign</button>
        </aside>
      </div>
    </main>
  );
}

export default Game;
