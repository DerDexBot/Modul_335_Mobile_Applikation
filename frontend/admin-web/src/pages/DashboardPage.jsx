import { Link } from 'react-router-dom';

export default function DashboardPage() {
  return (
    <div style={{ padding: 32 }}>
      <h1>Admin Dashboard</h1>
      <nav style={{ display: 'flex', gap: 16, flexDirection: 'column', maxWidth: 300 }}>
        <Link to="/orders">Auftraege verwalten</Link>
        <Link to="/hr">HR verwalten</Link>
        <Link to="/company">Firma verwalten</Link>
        <Link to="/salary">Lohn / Stunden</Link>
      </nav>
    </div>
  );
}
