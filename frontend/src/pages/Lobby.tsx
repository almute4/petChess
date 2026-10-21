import { useNavigate } from "react-router-dom";

function Lobby() {
  const navigate = useNavigate();

  return (
    <div className="lobby-container">
      <h1>Lobby</h1>
      <div className="lobby-buttons">
        <button
          className="lobby-btn play-btn"
          onClick={() => navigate("/game")}
        >
          Play
        </button>
        <button className="lobby-btn" onClick={() => navigate("/history")}>
          Match History
        </button>
        <button className="lobby-btn" onClick={() => navigate("/settings")}>
          Account Settings
        </button>
      </div>
    </div>
  );
}

export default Lobby;
