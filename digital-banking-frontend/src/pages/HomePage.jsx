import { useEffect, useState } from 'react'
import { checkApiHealth } from '../api/authApi'
import { getApiErrorMessage } from '../lib/http'

export default function HomePage() {
  const [health, setHealth] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    let mounted = true
    checkApiHealth()
      .then((data) => {
        if (mounted) {
          setHealth(data?.data || data)
        }
      })
      .catch((err) => {
        if (mounted) {
          setError(getApiErrorMessage(err))
        }
      })
    return () => {
      mounted = false
    }
  }, [])

  return (
    <div className="stack">
      <section className="panel hero">
        <h1>Digital Banking Frontend</h1>
        <p>This React app is integrated with your authentication backend APIs.</p>
        <div className="endpoint-list">
          <a href="http://localhost:8080/swagger-ui.html" target="_blank" rel="noreferrer">
            Open Backend Swagger
          </a>
          <a href="http://localhost:8080/actuator/health" target="_blank" rel="noreferrer">
            Open Actuator Health
          </a>
        </div>
      </section>

      <section className="panel">
        <h2>Backend API Health</h2>
        {error ? <p className="status-error">{error}</p> : null}
        {health ? (
          <div className="grid-two">
            <div>
              <strong>Status:</strong> {health.status || 'UP'}
            </div>
            <div>
              <strong>Application:</strong> {health.application || '-'}
            </div>
            <div>
              <strong>Port:</strong> {health.port || '-'}
            </div>
            <div>
              <strong>Uptime (ms):</strong> {health.uptimeMs ?? '-'}
            </div>
          </div>
        ) : (
          <p>Loading health information...</p>
        )}
      </section>
    </div>
  )
}
