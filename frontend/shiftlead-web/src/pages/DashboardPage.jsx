import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Layout from '../components/Layout';
import api from '../services/api';

const CARDS = [
  { to: '/planning', title: 'Arbeitsplanung',   desc: 'Arbeitspläne erstellen, Schichten planen und veröffentlichen' },
  { to: '/time',     title: 'Arbeitszeiten',     desc: 'Check-in / Check-out der Mitarbeiter einsehen und auswerten' },
  { to: '/orders',   title: 'Aufträge',          desc: 'Zugewiesene Aufträge und Auftragsbezüge verwalten' },
  { to: '/notes',    title: 'Notizen',           desc: 'Schicht- und Auftragsnotizen im Überblick' },
];

export default function DashboardPage() {
  const navigate = useNavigate();
  const shiftLeadId = localStorage.getItem('userId');
  const [stats, setStats] = useState({ plans: 0, published: 0, employees: 0 });

  useEffect(() => {
    const loadStats = async () => {
      try {
        const [plansRes, empRes] = await Promise.all([
          api.get('/api/planning/workplans', { params: { shiftLeadId } }),
          api.get('/api/users', { params: { role: 'EMPLOYEE' } }),
        ]);
        const plans = plansRes.data || [];
        const employees = empRes.data || [];
        setStats({
          plans: plans.length,
          published: plans.filter(p => p.status === 'PUBLISHED').length,
          employees: employees.filter(e => e.active).length,
        });
      } catch {
        // stats bleiben auf 0
      }
    };
    loadStats();
  }, []);

  return (
    <Layout>
      <div className="sl-page">
        <div className="sl-page-header">
          <h1>Übersicht</h1>
          <p>Aktuelle Kennzahlen und Schnellzugriff auf alle Bereiche.</p>
        </div>

        <div className="metric-grid" style={{ marginBottom: 28 }}>
          <div className="metric-card">
            <strong>{stats.plans}</strong>
            <span>Arbeitspläne gesamt</span>
          </div>
          <div className="metric-card">
            <strong>{stats.published}</strong>
            <span>davon veröffentlicht</span>
          </div>
          <div className="metric-card">
            <strong>{stats.employees}</strong>
            <span>aktive Mitarbeiter</span>
          </div>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: 16 }}>
          {CARDS.map(card => (
            <button
              key={card.to}
              className="panel"
              onClick={() => navigate(card.to)}
              style={{ textAlign: 'left', cursor: 'pointer', border: '1px solid #d9e1e8' }}
            >
              <h3 style={{ margin: '0 0 8px', color: '#0f172a' }}>{card.title}</h3>
              <p className="muted" style={{ margin: 0, fontSize: 14, lineHeight: 1.5 }}>{card.desc}</p>
            </button>
          ))}
        </div>
      </div>
    </Layout>
  );
}
