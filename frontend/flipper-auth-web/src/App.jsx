import { useCallback, useEffect, useMemo, useState } from 'react'
import axios from 'axios'
import { API_BASE_URL } from './config'
import './App.css'

const DEFAULT_USERNAME = 'emp.meier'

function formatDate(value) {
  if (!value) {
    return '-'
  }

  return new Intl.DateTimeFormat('de-CH', {
    dateStyle: 'short',
    timeStyle: 'medium',
  }).format(new Date(value))
}

function statusLabel(value) {
  return value ? 'Angemeldet' : 'Abgemeldet'
}

function App() {
  const api = useMemo(() => axios.create({ baseURL: API_BASE_URL }), [])
  const [users, setUsers] = useState([])
  const [sessions, setSessions] = useState([])
  const [currentSession, setCurrentSession] = useState(null)
  const [currentStatus, setCurrentStatus] = useState(null)
  const [username, setUsername] = useState(DEFAULT_USERNAME)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const loadDashboard = useCallback(async () => {
    const [usersResponse, sessionsResponse] = await Promise.all([
      api.get('/api/flipper-auth/admin/users'),
      api.get('/api/flipper-auth/admin/sessions'),
    ])
    setUsers(usersResponse.data)
    setSessions(sessionsResponse.data)
  }, [api])

  const refreshStatus = useCallback(
    async (sessionId) => {
      if (!sessionId) {
        return
      }

      const response = await api.get(`/api/flipper-auth/status/${sessionId}`)
      setCurrentStatus(response.data)
      await loadDashboard()
    },
    [api, loadDashboard],
  )

  useEffect(() => {
    const timer = window.setTimeout(() => {
      loadDashboard().catch((err) => {
        setError(err.response?.data?.message || err.message)
      })
    }, 0)

    return () => window.clearTimeout(timer)
  }, [loadDashboard])

  useEffect(() => {
    if (!currentSession?.sessionId || currentStatus?.used) {
      return undefined
    }

    const timer = window.setInterval(() => {
      refreshStatus(currentSession.sessionId).catch((err) => {
        setError(err.response?.data?.message || err.message)
      })
    }, 2000)

    return () => window.clearInterval(timer)
  }, [currentSession?.sessionId, currentStatus?.used, refreshStatus])

  async function runAction(action) {
    setLoading(true)
    setError('')
    setMessage('')

    try {
      const response = await api.post('/api/flipper-auth/start', {
        username: username.trim(),
        action,
      })
      setCurrentSession(response.data)
      setCurrentStatus(null)
      setMessage(`${action === 'LOGIN' ? 'Login' : 'Logout'} gestartet`)
      await loadDashboard()
    } catch (err) {
      setError(err.response?.data?.message || err.message)
    } finally {
      setLoading(false)
    }
  }

  async function simulateDevice() {
    if (!currentSession?.sessionId) {
      return
    }

    setLoading(true)
    setError('')

    try {
      const response = await api.post('/api/flipper-auth/simulate-device', {
        sessionId: currentSession.sessionId,
      })
      setMessage(response.data.message)
      await refreshStatus(currentSession.sessionId)
    } catch (err) {
      setError(err.response?.data?.message || err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="app-shell">
      <header className="topbar">
        <div>
          <p className="eyebrow">Flipper Auth MVP</p>
          <h1>Auth Dashboard</h1>
        </div>
        <div className="api-pill">{API_BASE_URL}</div>
      </header>

      <section className="toolbar" aria-label="Aktionen">
        <label className="username-field">
          <span>Username</span>
          <input
            value={username}
            onChange={(event) => setUsername(event.target.value)}
            disabled={loading}
          />
        </label>
        <button type="button" onClick={loadDashboard} disabled={loading}>
          Refresh
        </button>
        <button type="button" onClick={() => runAction('LOGIN')} disabled={loading}>
          Start Login
        </button>
        <button type="button" onClick={() => runAction('LOGOUT')} disabled={loading}>
          Start Logout
        </button>
        <button
          type="button"
          className="simulate"
          onClick={simulateDevice}
          disabled={loading || !currentSession?.sessionId}
        >
          Flipper simulieren
        </button>
      </section>

      {(message || error) && (
        <section className={`notice ${error ? 'error' : 'success'}`}>
          {error || message}
        </section>
      )}

      <section className="current-session">
        <div>
          <p className="section-label">Aktuelle Session</p>
          <h2>{currentSession ? currentSession.action : 'Keine Session gestartet'}</h2>
        </div>
        {currentSession ? (
          <dl className="session-grid">
            <div>
              <dt>Session ID</dt>
              <dd>{currentSession.sessionId}</dd>
            </div>
            <div>
              <dt>Challenge</dt>
              <dd className="challenge">{currentSession.challenge}</dd>
            </div>
            <div>
              <dt>Läuft ab</dt>
              <dd>{formatDate(currentSession.expiresAt)}</dd>
            </div>
            <div>
              <dt>Status</dt>
              <dd>
                {currentStatus
                  ? `${currentStatus.used ? 'Benutzt' : 'Wartet'} · ${statusLabel(
                      currentStatus.loggedIn,
                    )}`
                  : 'Warte auf Flipper'}
              </dd>
            </div>
          </dl>
        ) : (
          <p className="empty">Starte einen Login oder Logout für einen vorhandenen Benutzer.</p>
        )}
      </section>

      <section className="split-layout">
        <div className="panel">
          <div className="panel-heading">
            <p className="section-label">User</p>
            <span>{users.length}</span>
          </div>
          <div className="user-list">
            {users.map((user) => (
              <article className="user-card" key={user.id}>
                <div>
                  <h3>{user.username}</h3>
                  <p>ID {user.id}</p>
                </div>
                <span className={`status ${user.loggedIn ? 'online' : 'offline'}`}>
                  {statusLabel(user.loggedIn)}
                </span>
              </article>
            ))}
          </div>
        </div>

        <div className="panel">
          <div className="panel-heading">
            <p className="section-label">Sessions</p>
            <span>{sessions.length}</span>
          </div>
          <div className="session-list">
            {sessions.map((session) => (
              <article className="session-row" key={session.id}>
                <div>
                  <h3>
                    #{session.id} · {session.action}
                  </h3>
                  <p>{session.username}</p>
                </div>
                <div className="session-meta">
                  <span>{session.used ? 'Benutzt' : 'Offen'}</span>
                  <span>{session.success ? 'Erfolgreich' : 'Ausstehend'}</span>
                  <span>{formatDate(session.createdAt)}</span>
                </div>
              </article>
            ))}
            {sessions.length === 0 && <p className="empty">Noch keine Sessions.</p>}
          </div>
        </div>
      </section>
    </main>
  )
}

export default App
