import { Link } from 'react-router-dom';

export default function NotesPage() {
  return (
    <div style={{ padding: 32, maxWidth: 900, margin: '0 auto' }}>
      <Link to="/dashboard" style={{ color: '#475569', fontSize: 14, textDecoration: 'none' }}>← Dashboard</Link>
      <section style={panelStyle}>
        <h1 style={{ marginTop: 0 }}>Notizen</h1>
        <p style={{ color: '#64748b', lineHeight: 1.6 }}>
          Notizen werden aktuell direkt pro Schicht im Arbeitsplan gespeichert. Eine separate Notizen-Übersicht
          kann später alle Schicht- und Auftragsnotizen gesammelt anzeigen.
        </p>
        <Link to="/planning" style={buttonLink}>Zur Arbeitsplanung</Link>
      </section>
    </div>
  );
}

const panelStyle = { marginTop: 18, background: '#fff', border: '1px solid #e2e8f0', borderRadius: 12, padding: 24 };
const buttonLink = { display: 'inline-block', marginTop: 10, padding: '9px 14px', background: '#2563eb', color: '#fff', borderRadius: 8, textDecoration: 'none', fontWeight: 700 };
