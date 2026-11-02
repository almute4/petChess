import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Piece from "../components/Piece";

// Заглавные буквы - белые фигуры, строчные - чёрные.
const initialBoard = [
  ["r", "", "b", "q", "k", "b", "n", "r"],
  ["", "p", "p", "p", "", "p", "p", "p"],
  ["p", "", "n", "", "", "", "", ""],
  ["", "B", "", "", "p", "", "", ""],
  ["", "", "", "", "P", "", "", ""],
  ["", "", "", "", "", "N", "", ""],
  ["P", "P", "P", "P", "", "P", "P", "P"],
  ["R", "N", "B", "Q", "K", "", "", "R"],
];

// Подсветка последнего хода (a7 -> a6): "строка-столбец".
const lastMoveSquares = new Set(["1-0", "2-0"]);

const moves = [
  ["e4", "e5"],
  ["Nf3", "Nc6"],
  ["Bb5", "a6"],
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
        <div className="board-frame">
          <div className="chessboard" role="img" aria-label="Chess board position after 3 moves">
            {initialBoard.flatMap((rank, row) =>
              rank.map((piece, col) => {
                const shade = (row + col) % 2 === 0 ? "light" : "dark";
                const highlight = lastMoveSquares.has(`${row}-${col}`) ? " last-move" : "";
                return (
                  <div
                    key={`${row}-${col}`}
                    className={`chess-square ${shade}${highlight}`}
                    aria-hidden="true"
                  >
                    {piece && <Piece code={piece} />}
                  </div>
                );
              }),
            )}
          </div>
        </div>

        <aside className="game-sidebar">
          <div className="game-timer opponent">{formatTime(opponentTime)}</div>
          <section className="moves-section">
            <h3>Moves</h3>
            <div className="moves-list">
              {moves.map(([white, black], index) => (
                <div className="move-row" key={index}>
                  <span className="move-num">{index + 1}.</span>
                  <span className="move-white">{white}</span>
                  <span className={`move-black${index === moves.length - 1 ? " move-last" : ""}`}>
                    {black}
                  </span>
                </div>
              ))}
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
