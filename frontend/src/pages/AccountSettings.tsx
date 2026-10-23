import { useState } from "react";
import { useNavigate } from "react-router-dom";

function AccountSettings() {
  const [name, setName] = useState("KnightRider");
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const navigate = useNavigate();

  const handleSaveName = () => {
    console.log("Save name:", name);
  };

  const handleSavePassword = () => {
    console.log("Save password:", { currentPassword, newPassword });
  };

  const handleLogout = () => {
    navigate("/", { replace: true });
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
        />
        <button className="btn-save" onClick={handleSaveName}>
          Save name
        </button>
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
        <button className="btn-save" onClick={handleSavePassword}>
          Save password
        </button>
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
