import { useNavigate } from 'react-router-dom';
import Layout from '../components/Layout';

export default function NotesPage() {
  const navigate = useNavigate();

  return (
    <Layout>
      <div className="sl-page">
        <div className="sl-page-header">
          <h1>Notizen</h1>
          <p>Schicht- und Auftragsnotizen · US-SL-011</p>
        </div>

        <div className="panel">
          <div className="empty-state">
            <p style={{ margin: '0 0 8px', fontWeight: 600 }}>Notizen sind direkt in den Schichten gespeichert</p>
            <p style={{ margin: 0, fontSize: 14 }}>
              Jede Schicht im Arbeitsplan kann eine Notiz enthalten. Eine rollenübergreifende
              Notiz-Übersicht kann hier in einer späteren Version erscheinen.
            </p>
          </div>
          <div style={{ marginTop: 14 }}>
            <button className="primary-button" onClick={() => navigate('/planning')}>
              Zur Arbeitsplanung
            </button>
          </div>
        </div>
      </div>
    </Layout>
  );
}
