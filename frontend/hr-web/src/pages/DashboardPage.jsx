import { Link } from 'react-router-dom';

export default function DashboardPage() {
  return (
    <div style={{ padding: 32 }}>
      <h1>HR Dashboard</h1>
      <nav style={{ display: 'flex', gap: 16, flexDirection: 'column', maxWidth: 300 }}>
        <Link to="/shift-leads">Schichtleiterverwaltung</Link>
        <Link to="/hours">Total-Stunden Verwaltung</Link>
        <Link to="/invoices">Rechnungen erstellen</Link>
        <Link to="/reports">Stunden-Auswertungen</Link>
        <Link to="/absences">Absenzen / Ferien Verwaltung</Link>
      </nav>
    </div>
  );
}
