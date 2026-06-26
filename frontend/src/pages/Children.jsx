import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../lib/api'
import { classifyNutritionalStatus } from '../utils/nutritionalStatus'
import ChildCard from '../components/ChildCard'
import toast from 'react-hot-toast'

export default function Children() {
  const [children, setChildren] = useState([])
  const [health,   setHealth]   = useState([])
  const [loading,  setLoading]  = useState(true)
  const [search,   setSearch]   = useState('')
  const [filter,   setFilter]   = useState('all')

  useEffect(() => {
    async function load() {
      try {
        const [kids, hr] = await Promise.all([api.children.list(), api.health.list()])
        setChildren(kids); setHealth(hr)
      } catch { toast.error('Failed to load children') }
      setLoading(false)
    }
    load()
  }, [])

  function latestStatus(child) {
    const record = health.find(h => h.childId === child.id)
    if (!record) return null
    return classifyNutritionalStatus(record.weightKg, child.dateOfBirth, child.sex)
  }

  const filtered = children
    .filter(c => filter === 'all' || c.enrollmentStatus === filter)
    .filter(c => `${c.firstName} ${c.lastName}`.toLowerCase().includes(search.toLowerCase()))

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-extrabold text-gray-900">Children</h1>
        <Link to="/children/new" className="bg-primary-600 hover:bg-primary-700 text-white text-sm font-semibold px-4 py-2 rounded-xl transition-colors">+ Add Child</Link>
      </div>

      <div className="flex gap-3 flex-col sm:flex-row">
        <input type="text" placeholder="Search by name…" value={search} onChange={e => setSearch(e.target.value)}
          className="flex-1 border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500" />
        <select value={filter} onChange={e => setFilter(e.target.value)}
          className="border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500 bg-white">
          <option value="all">All status</option>
          <option value="active">Active</option>
          <option value="inactive">Inactive</option>
        </select>
      </div>

      {loading ? (
        <div className="flex items-center justify-center h-48"><div className="w-8 h-8 border-4 border-primary-600 border-t-transparent rounded-full animate-spin" /></div>
      ) : filtered.length === 0 ? (
        <div className="text-center py-16 text-gray-400">
          <p className="text-4xl mb-3">👦</p>
          <p className="font-medium">No children found</p>
          <Link to="/children/new" className="text-primary-600 text-sm mt-2 inline-block hover:underline">Enroll a child →</Link>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-3">
          {filtered.map(child => <ChildCard key={child.id} child={child} status={latestStatus(child)} />)}
        </div>
      )}
    </div>
  )
}
