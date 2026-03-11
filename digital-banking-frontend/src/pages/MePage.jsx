import { useEffect, useState } from 'react'
import { me } from '../api/authApi'
import { useAuth } from '../context/useAuth'
import { getApiErrorMessage } from '../lib/http'

export default function MePage() {
  const { user: sessionUser, setAuth, token } = useAuth()
  const [profile, setProfile] = useState(sessionUser)
  const [error, setError] = useState('')

  useEffect(() => {
    let mounted = true
    me()
      .then((response) => {
        if (!mounted) {
          return
        }
        const freshUser = {
          id: response.data.id,
          role: response.data.role,
          status: response.data.status,
          email: response.data.email,
          mobileNumber: response.data.mobileNumber,
          emailVerified: response.data.emailVerified,
          mobileVerified: response.data.mobileVerified,
        }
        setProfile(freshUser)
        setAuth(token, freshUser)
      })
      .catch((err) => {
        if (mounted) {
          setError(getApiErrorMessage(err))
        }
      })
    return () => {
      mounted = false
    }
  }, [setAuth, token])

  return (
    <section className="panel">
      <h2>My Profile</h2>
      {error ? <p className="status-error">{error}</p> : null}
      {profile ? (
        <div className="grid-two">
          <div><strong>User ID:</strong> {profile.id}</div>
          <div><strong>Role:</strong> {profile.role}</div>
          <div><strong>Status:</strong> {profile.status}</div>
          <div><strong>Email:</strong> {profile.email}</div>
          <div><strong>Mobile:</strong> {profile.mobileNumber}</div>
          <div><strong>Email Verified:</strong> {String(profile.emailVerified)}</div>
          <div><strong>Mobile Verified:</strong> {String(profile.mobileVerified)}</div>
        </div>
      ) : (
        <p>Loading profile...</p>
      )}
    </section>
  )
}
