import { useCallback, useEffect, useState } from 'react'
import { confirmVerification, me, requestVerification } from '../api/authApi'
import { useAuth } from '../context/useAuth'
import { getApiErrorMessage } from '../lib/http'

function statusPillClass(status) {
  if (status === 'ACTIVE') {
    return 'pill pill-success'
  }
  if (status === 'PENDING_VERIFICATION') {
    return 'pill pill-warning'
  }
  return 'pill pill-danger'
}

function verificationPillClass(isVerified) {
  return isVerified ? 'pill pill-success' : 'pill pill-warning'
}

export default function MePage() {
  const { user: sessionUser, setAuth, token } = useAuth()
  const [profile, setProfile] = useState(sessionUser)
  const [emailToken, setEmailToken] = useState('')
  const [mobileToken, setMobileToken] = useState('')
  const [emailMessage, setEmailMessage] = useState('')
  const [mobileMessage, setMobileMessage] = useState('')
  const [emailOtpRequested, setEmailOtpRequested] = useState(false)
  const [mobileOtpRequested, setMobileOtpRequested] = useState(false)
  const [error, setError] = useState('')
  const [loadingEmail, setLoadingEmail] = useState(false)
  const [loadingMobile, setLoadingMobile] = useState(false)

  const loadProfile = useCallback(async () => {
    const response = await me()
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
  }, [setAuth, token])

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

  async function handleRequest(channel) {
    if (!profile) {
      return
    }
    setError('')
    if (channel === 'EMAIL') {
      setLoadingEmail(true)
      setEmailMessage('')
    } else {
      setLoadingMobile(true)
      setMobileMessage('')
    }

    try {
      const loginId = channel === 'EMAIL' ? profile.email : profile.mobileNumber
      const response = await requestVerification({ loginId, channel })
      const message = response?.data?.message || `${channel} verification token requested successfully.`
      if (channel === 'EMAIL') {
        setEmailToken('')
        setEmailOtpRequested(true)
        setEmailMessage(message)
      } else {
        setMobileToken('')
        setMobileOtpRequested(true)
        setMobileMessage(message)
      }
    } catch (err) {
      setError(getApiErrorMessage(err))
    } finally {
      if (channel === 'EMAIL') {
        setLoadingEmail(false)
      } else {
        setLoadingMobile(false)
      }
    }
  }

  async function handleConfirm(channel) {
    if (!profile) {
      return
    }
    setError('')
    const tokenValue = channel === 'EMAIL' ? emailToken : mobileToken
    if (!tokenValue.trim()) {
      setError(`Enter ${channel} verification token.`)
      return
    }

    if (channel === 'EMAIL') {
      setLoadingEmail(true)
      setEmailMessage('')
    } else {
      setLoadingMobile(true)
      setMobileMessage('')
    }

    try {
      const loginId = channel === 'EMAIL' ? profile.email : profile.mobileNumber
      const response = await confirmVerification({ loginId, channel, token: tokenValue.trim() })
      const message = response?.data?.message || `${channel} verified successfully.`

      if (channel === 'EMAIL') {
        setEmailMessage(message)
        setEmailToken('')
        setEmailOtpRequested(false)
      } else {
        setMobileMessage(message)
        setMobileToken('')
        setMobileOtpRequested(false)
      }

      await loadProfile()
    } catch (err) {
      setError(getApiErrorMessage(err))
    } finally {
      if (channel === 'EMAIL') {
        setLoadingEmail(false)
      } else {
        setLoadingMobile(false)
      }
    }
  }

  return (
    <div className="stack">
      <section className="panel profile-panel">
        <div className="profile-header">
          <div>
            <h2>My Profile</h2>
            <p className="profile-subtitle">Manage your account details and verification status.</p>
          </div>
          {profile ? (
            <div className="profile-badges">
              <span className="pill pill-role">{profile.role}</span>
              <span className={statusPillClass(profile.status)}>{profile.status}</span>
            </div>
          ) : null}
        </div>

        {error ? <p className="status-error">{error}</p> : null}
        {profile ? (
          <div className="profile-kv-grid">
            <div className="kv-item">
              <span className="kv-label">User ID</span>
              <span className="kv-value">{profile.id}</span>
            </div>
            <div className="kv-item">
              <span className="kv-label">Email</span>
              <span className="kv-value">{profile.email}</span>
            </div>
            <div className="kv-item">
              <span className="kv-label">Mobile</span>
              <span className="kv-value">{profile.mobileNumber}</span>
            </div>
            <div className="kv-item">
              <span className="kv-label">Email Verification</span>
              <span className={verificationPillClass(profile.emailVerified)}>{profile.emailVerified ? 'Verified' : 'Pending'}</span>
            </div>
            <div className="kv-item">
              <span className="kv-label">Mobile Verification</span>
              <span className={verificationPillClass(profile.mobileVerified)}>{profile.mobileVerified ? 'Verified' : 'Pending'}</span>
            </div>
          </div>
        ) : (
          <p>Loading profile...</p>
        )}
      </section>

      {profile && (!profile.emailVerified || !profile.mobileVerified) ? (
        <section className="panel verification-panel">
          <h3>Complete Verification</h3>
          <p className="profile-subtitle">Verify your email and mobile to complete onboarding.</p>
          <div className="verification-grid">
            {!profile.emailVerified ? (
              <div className="verify-card">
                <div className="verify-card-head">
                  <h4>Verify Email</h4>
                  <span className="pill pill-warning">Pending</span>
                </div>
                <p className="verify-target">{profile.email}</p>
                <div className="verify-form">
                  <button className="button button-secondary auth-submit" type="button" onClick={() => handleRequest('EMAIL')} disabled={loadingEmail}>
                    {loadingEmail ? 'Requesting...' : emailOtpRequested ? 'Resend Email OTP' : 'Request Email OTP'}
                  </button>
                  {emailOtpRequested ? (
                    <div className="otp-panel">
                      <label className="verify-label">
                        Enter Email OTP
                        <input value={emailToken} onChange={(event) => setEmailToken(event.target.value)} placeholder="6-digit OTP" />
                      </label>
                      <button className="button auth-submit" type="button" onClick={() => handleConfirm('EMAIL')} disabled={loadingEmail}>
                        {loadingEmail ? 'Verifying...' : 'Verify Email'}
                      </button>
                    </div>
                  ) : (
                    <p className="verify-note">Click the button above to receive the OTP and continue verification.</p>
                  )}
                  {emailMessage ? <p className="status-success">{emailMessage}</p> : null}
                </div>
              </div>
            ) : null}

            {!profile.mobileVerified ? (
              <div className="verify-card">
                <div className="verify-card-head">
                  <h4>Verify Mobile</h4>
                  <span className="pill pill-warning">Pending</span>
                </div>
                <p className="verify-target">{profile.mobileNumber}</p>
                <div className="verify-form">
                  <button className="button button-secondary auth-submit" type="button" onClick={() => handleRequest('MOBILE')} disabled={loadingMobile}>
                    {loadingMobile ? 'Requesting...' : mobileOtpRequested ? 'Resend Mobile OTP' : 'Request Mobile OTP'}
                  </button>
                  {mobileOtpRequested ? (
                    <div className="otp-panel">
                      <label className="verify-label">
                        Enter Mobile OTP
                        <input value={mobileToken} onChange={(event) => setMobileToken(event.target.value)} placeholder="6-digit OTP" />
                      </label>
                      <button className="button auth-submit" type="button" onClick={() => handleConfirm('MOBILE')} disabled={loadingMobile}>
                        {loadingMobile ? 'Verifying...' : 'Verify Mobile'}
                      </button>
                    </div>
                  ) : (
                    <p className="verify-note">Click the button above to receive the OTP and continue verification.</p>
                  )}
                  {mobileMessage ? <p className="status-success">{mobileMessage}</p> : null}
                </div>
              </div>
            ) : null}
          </div>
        </section>
      ) : null}
    </div>
  )
}
