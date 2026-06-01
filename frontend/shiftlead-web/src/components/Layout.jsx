import { NavLink, useNavigate } from 'react-router-dom';

const NAV = [
  { to: '/dashboard', label: 'Übersicht' },
  { to: '/planning',  label: 'Planung' },
  { to: '/time',      label: 'Zeiten' },
  { to: '/orders',    label: 'Aufträge' },
  { to: '/notes',     label: 'Notizen' },
];

export default function Layout({ children }) {
  const navigate = useNavigate();
  const username = localStorage.getItem('username') || 'Schichtleiter';

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    localStorage.removeItem('username');
    localStorage.removeItem('userId');
    navigate('/login');
  };

  return (
    <div className="app-shell">
      <header className="sl-header">
        <div className="sl-brand">
          <p className="eyebrow">Planifywork</p>
          <span className="sl-brand-title">Schichtleiter</span>
        </div>

        <nav className="sl-nav">
          {NAV.map(({ to, label }) => (
            <NavLink
              key={to}
              to={to}
              className={({ isActive }) => `sl-nav-link${isActive ? ' active' : ''}`}
            >
              {label}
            </NavLink>
          ))}
        </nav>

        <div className="actions">
          <span className="muted" style={{ fontSize: 14 }}>{username}</span>
          <button className="ghost-button" onClick={logout}>Abmelden</button>
        </div>
      </header>

      <main className="sl-content">
        {children}
      </main>
    </div>
  );
}
