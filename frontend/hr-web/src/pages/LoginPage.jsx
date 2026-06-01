import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';

export default function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError]     = useState('');
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');
    try {
      const { data } = await api.post('/api/auth/login', { username, password });
      if (data.role !== 'HR') {
        setError('Kein HR-Zugang');
        return;
      }
      localStorage.setItem('token', data.token);
      navigate('/dashboard');
    } catch {
      setError('Login fehlgeschlagen');
    }
  };

  return (
    <div style={{ maxWidth: 400, margin: '100px auto', padding: 24 }}>
      <h2>HR Login</h2>
      <form onSubmit={handleLogin}>
        <input placeholder="Benutzername" value={username}
          onChange={e => setUsername(e.target.value)} style={{ display: 'block', width: '100%', marginBottom: 12 }} />
        <input type="password" placeholder="Passwort" value={password}
          onChange={e => setPassword(e.target.value)} style={{ display: 'block', width: '100%', marginBottom: 12 }} />
        {error && <p style={{ color: 'red' }}>{error}</p>}
        <button type="submit">Anmelden</button>
      </form>
    </div>
  );
}
