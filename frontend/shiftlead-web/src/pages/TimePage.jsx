import { useEffect, useState } from 'react';
import Layout from '../components/Layout';
import api from '../services/api';

const today = new Date();
const defaultMonth = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}`;

export default function TimePage() {
  const [employees, setEmployees] = useState([]);
  const [timeData, setTimeData] = useState([]);
  const [detail, setDetail] = useState(null);
  const [detailEntries, setDetailEntries] = useState([]);
  const [month, setMonth] = useState(defaultMonth);
  const [loading, setLoading] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);
  const [error, setError] = useState('');

  const loadData = async () => {
    setLoading(true);
    setError('');
    try {
      const [empRes, timeRes] = await Promise.all([
        api.get('/api/users', { params: { role: 'EMPLOYEE' } }),
        api.get('/api/time/total'),
      ]);
      setEmployees((empRes.data || []).filter(e => e.active));
      setTimeData(timeRes.data || []);
    } catch (err) {
      setError(err.response?.status === 404
        ? 'Time Service noch nicht verfügbar. Sobald Mitarbeiter einchecken, erscheinen hier die Stunden.'
        : 'Daten konnten nicht geladen werden.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadData(); }, []);

  const openDetail = async (employee) => {
    setDetail(employee);
    setDetailEntries([]);
    setDetailLoading(true);
    const [year, m] = month.split('-');
    try {
      const { data } = await api.get(`/api/time/month/${employee.id}`, { params: { month: m, year } });
      setDetailEntries(data || []);
    } catch {
      setDetailEntries([]);
    } finally {
      setDetailLoading(false);
    }
  };

  const employeeName = (emp) => `${emp.firstName} ${emp.lastName}`;

  const hoursForEmployee = (empId) => {
    const entry = timeData.find(t => Number(t.employeeId) === Number(empId));
    return entry ? Number(entry.totalHours || 0).toFixed(2) : '—';
  };

  const totalHours = timeData.reduce((sum, t) => sum + Number(t.totalHours || 0), 0);

  return (
    <Layout>
      <div className="sl-page">
        <div className="section-heading sl-page-header">
          <div>
            <h1>Arbeitszeiten</h1>
            <p>Check-in / Check-out der Mitarbeiter einsehen · US-SL-009</p>
          </div>
          <div className="actions">
            <input
              type="month"
              value={month}
              onChange={e => setMonth(e.target.value)}
              style={{ padding: '8px 10px', border: '1px solid #c8d0d9', borderRadius: 6, fontSize: 14 }}
            />
            <button className="secondary-button" onClick={loadData} disabled={loading}>
              {loading ? 'Lädt…' : 'Aktualisieren'}
            </button>
          </div>
        </div>

        {error && <p className="form-error" style={{ marginBottom: 16 }}>{error}</p>}

        {!error && (
          <>
            <div className="metric-grid" style={{ marginBottom: 20 }}>
              <div className="metric-card">
                <strong>{employees.length}</strong>
                <span>aktive Mitarbeiter</span>
              </div>
              <div className="metric-card">
                <strong>{totalHours.toFixed(2)} h</strong>
                <span>erfasste Gesamtstunden</span>
              </div>
              <div className="metric-card">
                <strong>{timeData.filter(t => !t.checkOut && t.checkIn).length}</strong>
                <span>offene Einträge (kein Check-out)</span>
              </div>
            </div>

            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>Mitarbeiter</th>
                    <th>Benutzername</th>
                    <th>Gesamtstunden</th>
                    <th>Aktion</th>
                  </tr>
                </thead>
                <tbody>
                  {employees.length === 0 && (
                    <tr><td colSpan={4} style={{ textAlign: 'center', padding: 24, color: '#607080' }}>
                      Keine aktiven Mitarbeiter gefunden.
                    </td></tr>
                  )}
                  {employees.map(emp => (
                    <tr key={emp.id}>
                      <td>{employeeName(emp)}</td>
                      <td><span className="muted">{emp.username}</span></td>
                      <td><strong>{hoursForEmployee(emp.id)} h</strong></td>
                      <td>
                        <button className="secondary-button" onClick={() => openDetail(emp)}>
                          Monatsdetail
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </>
        )}

        {detail && (
          <div className="panel" style={{ marginTop: 20 }}>
            <div className="section-heading" style={{ marginBottom: 12 }}>
              <h2 style={{ margin: 0 }}>
                Monatsdetail – {employeeName(detail)} ({month})
              </h2>
              <button className="ghost-button" onClick={() => setDetail(null)}>Schliessen</button>
            </div>

            {detailLoading && <p className="muted">Lade Einträge…</p>}

            {!detailLoading && detailEntries.length === 0 && (
              <div className="empty-state">
                Keine Zeiteinträge für diesen Monat vorhanden.
              </div>
            )}

            {!detailLoading && detailEntries.length > 0 && (
              <div className="table-wrap">
                <table>
                  <thead>
                    <tr>
                      <th>Datum</th>
                      <th>Check-in</th>
                      <th>Check-out</th>
                      <th>Stunden</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {detailEntries.map((entry, i) => {
                      const open = !entry.checkOut;
                      return (
                        <tr key={entry.id ?? i}>
                          <td>{entry.entryDate ?? entry.date ?? '—'}</td>
                          <td>{entry.checkIn ? entry.checkIn.slice(0, 5) : '—'}</td>
                          <td>{entry.checkOut ? entry.checkOut.slice(0, 5) : <span className="status warn">Offen</span>}</td>
                          <td><strong>{Number(entry.totalHours || 0).toFixed(2)} h</strong></td>
                          <td>
                            <span className={`status ${open ? 'warn' : 'ok'}`}>
                              {open ? 'Kein Check-out' : 'Vollständig'}
                            </span>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}
      </div>
    </Layout>
  );
}
