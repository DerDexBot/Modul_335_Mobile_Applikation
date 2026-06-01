import { Link } from 'react-router-dom';

export default function OrdersPage() {
  return (
    <div style={{ padding: 32, maxWidth: 900, margin: '0 auto' }}>
      <Link to="/dashboard" style={{ color: '#475569', fontSize: 14, textDecoration: 'none' }}>← Dashboard</Link>
      <section style={panelStyle}>
        <h1 style={{ marginTop: 0 }}>Aufträge</h1>
        <p style={{ color: '#64748b', lineHeight: 1.6 }}>
          Die Auftragsansicht ist vorbereitet. Sobald der Order Service eigene Auftragsdaten liefert,
          können hier die zugewiesenen Aufträge des Schichtleiters angezeigt werden.
        </p>
        <p style={{ color: '#64748b', lineHeight: 1.6 }}>
          Für die Planung kann eine optionale Auftrag-ID bereits direkt beim Hinzufügen einer Schicht erfasst werden.
        </p>
      </section>
    </div>
  );
}

const panelStyle = { marginTop: 18, background: '#fff', border: '1px solid #e2e8f0', borderRadius: 12, padding: 24 };
