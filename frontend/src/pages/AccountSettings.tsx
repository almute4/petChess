import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  getCurrentUser,
  login,
  logout,
  updateUser,
  type User,
} from "../api/auth";
import { ApiError, getErrorMessage } from "../api/client";

type Message = { kind: "ok" | "error"; text: string } | null;

function AccountSettings() {
  const [user, setUser] = useState<User | null>(null);
  const [name, setName] = useState("");
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [nameMessage, setNameMessage] = useState<Message>(null);
  const [passwordMessage, setPasswordMessage] = useState<Message>(null);
  const navigate = useNavigate();

  useEffect(() => {
    let cancelled = false;
    getCurrentUser()
      .then((u) => {
        if (cancelled) return;
        setUser(u);
        setName(u.username);
      })
      .catch((err) => {
        if (!cancelled)
          setNameMessage({ kind: "error", text: getErrorMessage(err) });
      });
    return () => {
      cancelled = true;
    };
  }, []);

  const handleSaveName = async () => {
    if (!user) return;
    const username = name.trim();
    // На PUT /api/users/{id} на бэке нет @Valid, поэтому границы проверяем здесь.
    if (username.length < 3 || username.length > 50) {
      setNameMessage({
        kind: "error",
        text: "Name must be 3-50 characters long",
      });
      return;
    }
    try {
      const updated = await updateUser(user.id, {
        username,
        email: user.email,
      });
      setUser(updated);
      setName(updated.username);
      setNameMessage({ kind: "ok", text: "Name saved" });
    } catch (err) {
      setNameMessage({ kind: "error", text: getErrorMessage(err) });
    }
  };

  const handleSavePassword = async () => {
    if (!user) return;
    if (!currentPassword || !newPassword) {
      setPasswordMessage({
        kind: "error",
        text: "Fill in both password fields",
      });
      return;
    }
    if (newPassword.length < 6 || newPassword.length > 100) {
      setPasswordMessage({
        kind: "error",
        text: "New password must be 6-100 characters long",
      });
      return;
    }

    // На бэке нет проверки текущего пароля при смене, поэтому подтверждаем его повторным логином.
    try {
      await login(user.email, currentPassword);
    } catch (err) {
      const wrong = err instanceof ApiError && err.status === 401;
      setPasswordMessage({
        kind: "error",
        text: wrong ? "Current password is incorrect" : getErrorMessage(err),
      });
      return;
    }

    try {
      await updateUser(user.id, {
        username: user.username,
        email: user.email,
        password: newPassword,
      });
      setCurrentPassword("");
      setNewPassword("");
      setPasswordMessage({ kind: "ok", text: "Password changed" });
    } catch (err) {
      setPasswordMessage({ kind: "error", text: getErrorMessage(err) });
    }
  };

  const handleLogout = () => {
    logout();
    navigate("/login", { replace: true });
  };

  return (
    <div className="settings-container">
      <h1>Account Settings</h1>

      <div className="settings-section">
        <label>Name</label>
        <input
          type="text"
          value={name}
          onChange={(e) => setName(e.target.value)}
          disabled={!user}
        />
        <button className="btn-save" onClick={handleSaveName} disabled={!user}>
          Save name
        </button>
        {nameMessage && (
          <p className={`form-message ${nameMessage.kind}`}>
            {nameMessage.text}
          </p>
        )}
      </div>

      <div className="settings-section">
        <label>Current password</label>
        <input
          type="password"
          placeholder="********"
          value={currentPassword}
          onChange={(e) => setCurrentPassword(e.target.value)}
        />
      </div>

      <div className="settings-section">
        <label>New password</label>
        <input
          type="password"
          placeholder="Enter new password"
          value={newPassword}
          onChange={(e) => setNewPassword(e.target.value)}
        />
        <button
          className="btn-save"
          onClick={handleSavePassword}
          disabled={!user}
        >
          Save password
        </button>
        {passwordMessage && (
          <p className={`form-message ${passwordMessage.kind}`}>
            {passwordMessage.text}
          </p>
        )}
      </div>

      <div className="settings-actions">
        <button className="btn-secondary" onClick={() => navigate("/lobby")}>
          Back to lobby
        </button>
        <button className="btn-logout" onClick={handleLogout}>
          Log out
        </button>
      </div>
    </div>
  );
}

export default AccountSettings;
