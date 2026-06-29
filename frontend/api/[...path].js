const RAILWAY = 'https://daycarelog-production.up.railway.app'

module.exports = async function handler(req, res) {
  try {
    const segments = req.query.path
    const apiPath = Array.isArray(segments) ? segments.join('/') : (segments || '')

    const qs = Object.entries(req.query)
      .filter(([k]) => k !== 'path')
      .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`)
      .join('&')

    const url = `${RAILWAY}/api/${apiPath}${qs ? '?' + qs : ''}`

    const headers = { 'content-type': 'application/json', accept: 'application/json' }
    if (req.headers.authorization) headers.authorization = req.headers.authorization

    const opts = { method: req.method, headers }
    if (req.method !== 'GET' && req.method !== 'HEAD') {
      opts.body = JSON.stringify(req.body ?? {})
    }

    const upstream = await fetch(url, opts)
    const text = await upstream.text()

    res.status(upstream.status)
    res.setHeader('Content-Type', 'application/json')
    try {
      res.json(JSON.parse(text))
    } catch {
      res.end(text)
    }
  } catch (err) {
    res.status(502).json({ message: err.message })
  }
}
