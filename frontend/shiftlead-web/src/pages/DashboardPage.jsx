import { Link, useNavigate } from 'react-router-dom';

const CARDS = [
  { to: '/planning', icon: '📅', title: 'Arbeitsplanung', desc: 'Arbeitspläne erstellen, Schichten planen und veröffentlichen' },
  { to: '/orders', icon: '📦', title: 'Aufträge', desc: 'Zugewiesene Aufträge und Auftragsbezug für Schichten' },
  { to: '/notes', icon: '📝', title: 'Notizen', desc: 'Notizen zu Schichten und Aufträgen dokumentieren' },
];

export default function DashboardPage() {
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
    <div style={{ padding: 32, maxWidth: 1000, margin: '0 auto' }}>
      <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 32, gap: 16 }}>
        <div>
          <h1 style={{ margin: 0 }}>Schichtleiter Dashboard</h1>
          <p style={{ margin: '6px 0 0', color: '#64748b' }}>Angemeldet als {username}</p>
        </div>
        <button onClick={logout} style={btnSecondary}>Abmelden</button>
      </header>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(230px, 1fr))', gap: 20 }}>
        {CARDS.map(card => (
          <Link key={card.to} to={card.to} style={{ textDecoration: 'none' }}>
            <article style={cardStyle}>
              <div style={{ fontSize: 34, marginBottom: 14 }}>{card.icon}</div>
              <h2 style={{ fontSize: 18, margin: '0 0 8px', color: '#0f172a' }}>{card.title}</h2>
              <p style={{ fontSize: 14, color: '#64748b', margin: 0, lineHeight: 1.5 }}>{card.desc}</p>
            </article>
          </Link>
        ))}
      </div>
    </div>
  );
}

const cardStyle = {
  height: '100%',
  padding: 24,
  border: '1px solid #e2e8f0',
  borderRadius: 12,
  background: '#fff',
  boxShadow: '0 4px 16px rgba(15, 23, 42, 0.06)',
};

const btnSecondary = { padding: '8px 14px', border: '1px solid #cbd5e1', borderRadius: 8, background: '#fff', cursor: 'pointer', fontSize: 14 };
