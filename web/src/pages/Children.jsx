import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../lib/api'
import { classifyNutritionalStatus } from '../utils/nutritionalStatus'
import ChildCard from '../components/ChildCard'
import { PlusIcon, UsersIcon } from '../components/icons'
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
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-[22px] font-bold text-gray-900">Children</h1>
        <Link
          to="/children/new"
          className="inline-flex items-center gap-1.5 bg-primary-600 hover:bg-primary-700 text-white text-sm font-semibold px-4 py-2.5 rounded-lg transition-colors duration-150"
        >
          <PlusIcon width={16} height={16} /> Add Child
        </Link>
      </div>

      <div className="flex gap-3 flex-col sm:flex-row">
        <input type="text" placeholder="Search by name…" value={search} onChange={e => setSearch(e.target.value)}
          className="flex-1 border border-gray-200 rounded-lg px-4 py-2.5 text-sm text-gray-900 focus:outline-none focus:ring-2 focus:ring-primary-500/30 focus:border-primary-400" />
        <select value={filter} onChange={e => setFilter(e.target.value)}
          className="border border-gray-200 rounded-lg px-4 py-2.5 text-sm text-gray-900 focus:outline-none focus:ring-2 focus:ring-primary-500/30 focus:border-primary-400 bg-white">
          <option value="all">All status</option>
          <option value="active">Active</option>
          <option value="inactive">Inactive</option>
        </select>
      </div>

      {loading ? (
        <div className="flex items-center justify-center h-48"><div className="w-8 h-8 border-4 border-primary-600 border-t-transparent rounded-full animate-spin" /></div>
      ) : filtered.length === 0 ? (
        <div className="text-center py-16">
          <span className="inline-flex w-12 h-12 rounded-full bg-gray-100 text-gray-400 items-center justify-center mb-3">
            <UsersIcon width={22} height={22} />
          </span>
          <p className="font-medium text-gray-500">No children found</p>
          <Link to="/children/new" className="text-primary-700 text-sm mt-2 inline-block hover:underline">Enroll a child →</Link>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-4">
          {filtered.map(child => <ChildCard key={child.id} child={child} status={latestStatus(child)} />)}
        </div>
      )}
    </div>
  )
}
