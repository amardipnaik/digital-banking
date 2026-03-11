const DEVICE_ID_KEY = 'digital_banking_device_id'

function generateDeviceId() {
  if (window.crypto?.randomUUID) {
    return window.crypto.randomUUID()
  }
  return `device-${Date.now()}-${Math.random().toString(36).slice(2, 10)}`
}

export function getOrCreateDeviceId() {
  const existing = localStorage.getItem(DEVICE_ID_KEY)
  if (existing) {
    return existing
  }
  const next = generateDeviceId()
  localStorage.setItem(DEVICE_ID_KEY, next)
  return next
}
