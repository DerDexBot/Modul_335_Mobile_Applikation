import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';

export default function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const { data } = await api.post('/api/auth/login', { username, password });
      if (data.role !== 'SHIFT_LEAD') {
        setError('Kein Schichtleiter-Zugang. Bitte mit einem SHIFT_LEAD-Konto anmelden.');
        return;
      }
      localStorage.setItem('token', data.token);
      localStorage.setItem('role', data.role);
      localStorage.setItem('username', data.username ?? username);
      if (data.userId != null) localStorage.setItem('userId', String(data.userId));
      navigate('/dashboard');
    } catch {
      setError('Login fehlgeschlagen. Bitte Benutzername und Passwort prüfen.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-shell">
      <div className="login-panel">
        <div>
          <p className="eyebrow">Planifywork</p>
          <h1>Schichtleiter</h1>
          <p className="muted" style={{ margin: '4px 0 0', fontSize: 14 }}>
            Anmelden mit Rolle SHIFT_LEAD
          </p>
        </div>

        <form onSubmit={handleLogin}>
          <label>
            Benutzername
            <input
              value={username}
              onChange={e => setUsername(e.target.value)}
              placeholder="z.B. sl.huber"
              autoComplete="username"
              required
            />
          </label>
          <label>
            Passwort
            <input
              type="password"
              value={password}
              onChange={e => setPassword(e.target.value)}
              placeholder="Passwort"
              autoComplete="current-password"
              required
            />
          </label>
          {error && <p className="form-error">{error}</p>}
          <button type="submit" className="primary-button" disabled={loading} style={{ width: '100%' }}>
            {loading ? 'Anmelden…' : 'Anmelden'}
          </button>
        </form>
      </div>
    </div>
  );
}
