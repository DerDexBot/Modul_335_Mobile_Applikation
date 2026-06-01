import { Link } from 'react-router-dom';

export default function DashboardPage() {
  return (
    <div style={{ padding: 32 }}>
      <h1>Schichtleiter Dashboard</h1>
      <nav style={{ display: 'flex', gap: 16, flexDirection: 'column', maxWidth: 300 }}>
        <Link to="/planning">Arbeitsplaene erstellen</Link>
        <Link to="/orders">Auftraege entgegennehmen</Link>
        <Link to="/notes">Notizen erfassen</Link>
      </nav>
    </div>
  );
}
