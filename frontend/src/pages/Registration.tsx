import { useState } from "react";
import { useNavigate } from "react-router-dom";

function Registration() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const navigate = useNavigate();

  const handleRegister = (e: React.FormEvent) => {
    e.preventDefault();
    // This frontend currently uses a demo registration flow (no backend connected).
    // On successful form submission, always open the lobby first.
    void email;
    void password;
    navigate("/lobby", { replace: true });
  };

  return (
    <div className="registration-container">
      <h1>Registration</h1>
      <form onSubmit={handleRegister}>
        <div className="form-group">
          <label>Email</label>
          <input
            type="email"
            placeholder="you@example.com"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
        </div>
        <div className="form-group">
          <label>Password</label>
          <input
            type="password"
            placeholder="********"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>
        <button type="submit" className="btn-primary">
          Register
        </button>
      </form>
    </div>
  );
}

export default Registration;
