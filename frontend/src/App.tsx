import { Navigate, Outlet, Route, Routes } from "react-router-dom";
import { tokenStorage } from "./api/client";
import Login from "./pages/Login";
import Registration from "./pages/Registration";
import Lobby from "./pages/Lobby";
import AccountSettings from "./pages/AccountSettings";
import MatchHistory from "./pages/MatchHistory";
import Game from "./pages/Game";

// Страницы только для авторизованных.
function ProtectedRoute() {
  return tokenStorage.hasSession() ? (
    <Outlet />
  ) : (
    <Navigate to="/login" replace />
  );
}

// Логин и регистрация недоступны, если уже вошли.
function PublicOnlyRoute() {
  return tokenStorage.hasSession() ? (
    <Navigate to="/lobby" replace />
  ) : (
    <Outlet />
  );
}

function App() {
  return (
    <Routes>
      <Route element={<PublicOnlyRoute />}>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Registration />} />
      </Route>

      <Route element={<ProtectedRoute />}>
        <Route path="/lobby" element={<Lobby />} />
        <Route path="/settings" element={<AccountSettings />} />
        <Route path="/history" element={<MatchHistory />} />
        <Route path="/game" element={<Game />} />
      </Route>

      <Route path="*" element={<Navigate to="/lobby" replace />} />
    </Routes>
  );
}

export default App;
