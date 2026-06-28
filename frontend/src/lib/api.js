const BASE = typeof window !== 'undefined' && window.location.hostname !== 'localhost'
  ? 'https://daycarelog-production.up.railway.app/api'
  : '/api'

function getToken() {
  return localStorage.getItem('dcl_token')
}

async function request(path, options = {}) {
  const token = getToken()
  const res = await fetch(`${BASE}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    ...options,
  })

  if (!res.ok) {
    let msg = `Request failed (${res.status})`
    try { msg = (await res.json()).message ?? msg } catch (_) {}
    throw new Error(msg)
  }

  // 204 No Content
  if (res.status === 204) return null
  return res.json()
}

export const api = {
  auth: {
    login:    (email, password)              => request('/auth/login',    { method: 'POST', body: JSON.stringify({ email, password }) }),
    register: (email, password, firstName, lastName, middleName, suffix) => request('/auth/register', { method: 'POST', body: JSON.stringify({ email, password, firstName, lastName, middleName, suffix }) }),
  },
  children: {
    list:   ()          => request('/children'),
    get:    id          => request(`/children/${id}`),
    create: data        => request('/children', { method: 'POST', body: JSON.stringify(data) }),
    update: (id, data)  => request(`/children/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
    delete: id          => request(`/children/${id}`, { method: 'DELETE' }),
  },
  attendance: {
    getByDate:  date    => request(`/attendance?date=${date}`),
    getByChild: childId => request(`/attendance/child/${childId}`),
    getRange:   (s, e)  => request(`/attendance/range?start=${s}&end=${e}`),
    saveBulk:   records => request('/attendance/bulk', { method: 'POST', body: JSON.stringify(records) }),
  },
  health: {
    list:       ()         => request('/health-records'),
    getByChild: childId    => request(`/health-records/child/${childId}`),
    create:     data       => request('/health-records', { method: 'POST', body: JSON.stringify(data) }),
  },
  reports: {
    monthly: month => request(`/reports/monthly?month=${month}`),
  },
  users: {
    list:           ()          => request('/users'),
    updateRole:     (id, role)  => request(`/users/${id}/role`, { method: 'PUT', body: JSON.stringify({ role }) }),
    updateProfile:  (id, data)  => request(`/users/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  },
}
