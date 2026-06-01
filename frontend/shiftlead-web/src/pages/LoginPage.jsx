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
        setError('Kein Schichtleiter-Zugang');
        return;
      }

      localStorage.setItem('token', data.token);
      localStorage.setItem('role', data.role);
      localStorage.setItem('username', data.username ?? username);
      if (data.userId != null) {
        localStorage.setItem('userId', String(data.userId));
      }

      navigate('/dashboard');
    } catch {
      setError('Login fehlgeschlagen');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={pageStyle}>
      <div style={cardStyle}>
        <h2 style={{ marginTop: 0 }}>Schichtleiter Login</h2>
        <p style={{ color: '#666', marginBottom: 24 }}>
          Melde dich mit einem Benutzer mit Rolle SHIFT_LEAD an.
        </p>
        <form onSubmit={handleLogin}>
          <label style={labelStyle}>
            Benutzername
            <input
              placeholder="z.B. sl.huber"
              value={username}
              onChange={e => setUsername(e.target.value)}
              style={inputStyle}
              autoComplete="username"
            />
          </label>
          <label style={labelStyle}>
            Passwort
            <input
              type="password"
              placeholder="Passwort"
              value={password}
              onChange={e => setPassword(e.target.value)}
              style={inputStyle}
              autoComplete="current-password"
            />
          </label>
          {error && <p style={{ color: '#b91c1c', marginTop: 0 }}>{error}</p>}
          <button type="submit" disabled={loading} style={btnPrimary}>
            {loading ? 'Anmelden…' : 'Anmelden'}
          </button>
        </form>
      </div>
    </div>
  );
}

const pageStyle = {
  minHeight: '100vh',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  background: '#f5f7fb',
  padding: 24,
};

const cardStyle = {
  width: '100%',
  maxWidth: 420,
  background: '#fff',
  border: '1px solid #e5e7eb',
  borderRadius: 12,
  padding: 28,
  boxShadow: '0 10px 30px rgba(15, 23, 42, 0.08)',
};

const labelStyle = { display: 'block', fontSize: 14, fontWeight: 600, marginBottom: 14 };
const inputStyle = { display: 'block', width: '100%', boxSizing: 'border-box', marginTop: 6, padding: '10px 12px', border: '1px solid #cbd5e1', borderRadius: 8, fontSize: 14 };
const btnPrimary = { width: '100%', padding: '10px 16px', background: '#2563eb', color: '#fff', border: 'none', borderRadius: 8, cursor: 'pointer', fontSize: 15, fontWeight: 600 };
