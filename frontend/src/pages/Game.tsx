import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import Piece from "../components/Piece";
import { getCurrentUser, type User } from "../api/auth";
import { getErrorMessage } from "../api/client";
import { resignGame } from "../api/games";
import { useGame } from "../hooks/useGame";

export default function Game() {
  const { id } = useParams();
  const { game, connected, pending, error, setError, sendMove } = useGame(Number(id));
  const [user, setUser] = useState<User | null>(null);
  const [selected, setSelected] = useState<string | null>(null);
  const [promotion, setPromotion] = useState("q");

  useEffect(() => {
    let active = true;
    getCurrentUser().then(value => { if (active) setUser(value); })
      .catch(cause => { if (active) setError(getErrorMessage(cause)); });
    return () => { active = false; };
  }, [setError]);

  if (!game) return <main className="game-container"><p>{error || "Loading game…"}</p><Link to="/lobby">Lobby</Link></main>;

  const isWhite = game.whitePlayer.id === user?.id;
  const myTurn = game.turn === (isWhite ? "WHITE" : "BLACK");
  const canMove = !!user && connected && !pending && myTurn && game.status === "IN_PROGRESS";
  const squares = game.currentFen.split(" ")[0].split("/").flatMap((rank, row) => {
    const pieces = [...rank].flatMap(piece => /[1-8]/.test(piece) ? Array(Number(piece)).fill("") : [piece]);
    return pieces.map((piece: string, col: number) => ({
      piece, square: `${"abcdefgh"[col]}${8 - row}`, row, col,
    }));
  });
  if (!isWhite) squares.reverse();
  const lastMove = game.moves[game.moves.length - 1]?.uci;

  const chooseSquare = (square: string, piece: string) => {
    if (!canMove) return;
    const ownPiece = piece && (isWhite ? piece === piece.toUpperCase() : piece === piece.toLowerCase());
    if (ownPiece) {
      setSelected(square);
    } else if (selected) {
      const moving = squares.find(cell => cell.square === selected)?.piece;
      const promotes = moving?.toLowerCase() === "p" && (square[1] === "8" || square[1] === "1");
      sendMove({ from: selected, to: square, ...(promotes ? { promotion } : {}) });
      setSelected(null);
    }
  };
  const resign = async () => {
    if (!window.confirm("Are you sure you want to resign?")) return;
    try { await resignGame(game.id); } catch (cause) { setError(getErrorMessage(cause)); }
  };

  return (
    <main className="game-container">
      <div className="game-layout">
        <div className="board-frame"><div className="chessboard" aria-label="Chess board">
          {squares.map(({ piece, square, row, col }) => (
            <button key={square} type="button"
              className={`chess-square ${(row + col) % 2 === 0 ? "light" : "dark"}${selected === square || lastMove?.slice(0, 2) === square || lastMove?.slice(2, 4) === square ? " last-move" : ""}`}
              aria-label={`${square}${piece ? " " + piece : ""}`} aria-pressed={selected === square}
              disabled={!canMove} onClick={() => chooseSquare(square, piece)}>
              {piece && <Piece code={piece} />}
            </button>
          ))}
        </div></div>
        <aside className="game-sidebar">
          <p>{game.whitePlayer.username} vs {game.blackPlayer?.username ?? "Waiting for opponent"}</p>
          <p>{connected ? "Connected" : "Reconnecting…"}</p>
          <p>{game.status === "FINISHED" ? `${game.result} · ${game.resultReason}` :
            game.status === "WAITING" ? "Waiting for opponent" : myTurn ? "Your turn" : "Opponent's turn"}</p>
          {error && <p role="alert">{error}</p>}
          <section className="moves-section"><h3>Moves</h3><div className="moves-list">
            {game.moves.filter(move => move.ply % 2 === 1).map(move => (
              <div className="move-row" key={move.ply}>
                <span className="move-num">{Math.ceil(move.ply / 2)}.</span>
                <span className="move-white">{move.san}</span>
                <span className="move-black">{game.moves.find(next => next.ply === move.ply + 1)?.san}</span>
              </div>
            ))}
          </div></section>
          <label>Promote pawn to{" "}
            <select value={promotion} onChange={event => setPromotion(event.target.value)}>
              <option value="q">Queen</option><option value="r">Rook</option>
              <option value="b">Bishop</option><option value="n">Knight</option>
            </select>
          </label>
          <button className="btn-resign" disabled={!connected || game.status !== "IN_PROGRESS"} onClick={resign}>Resign</button>
          <Link to="/lobby">Lobby</Link>
        </aside>
      </div>
    </main>
  );
}
