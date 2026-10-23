import { Routes, Route } from "react-router-dom";
import Registration from "./pages/Registration";
import Lobby from "./pages/Lobby";
import AccountSettings from "./pages/AccountSettings";
import MatchHistory from "./pages/MatchHistory";
import Game from "./pages/Game";

function App() {
  return (
    <Routes>
      <Route path="/" element={<Registration />} />
      <Route path="/lobby" element={<Lobby />} />
      <Route path="/settings" element={<AccountSettings />} />
      <Route path="/history" element={<MatchHistory />} />
      <Route path="/game" element={<Game />} />
    </Routes>
  );
}

export default App;
