import { useEffect, useState } from 'react'
import { api } from '../lib/api'
import Pagination from '../components/Pagination'
import { formatManilaDateTime } from '../utils/date'
import { activityIcon, activityColor, activityLabel, entityLabel, ENTITY_TYPES } from '../utils/activityMeta'
import { ActivityActions } from '../utils/activityActions'
import { ClockIcon } from '../components/icons'
import toast from 'react-hot-toast'

const PAGE_SIZE = 20

export default function History() {
  const [items,       setItems]       = useState([])
  const [totalPages,  setTotalPages]  = useState(1)
  const [page,         setPage]        = useState(0) // 0-based, matches the API
  const [loading,      setLoading]     = useState(true)
  const [filters,      setFilters]     = useState({ entityType: '', action: '', from: '', to: '' })

  useEffect(() => { load() }, [page, filters])

  async function load() {
    setLoading(true)
    try {
      const res = await api.activity.search({
        ...filters,
        from: filters.from ? `${filters.from}T00:00:00` : '',
        to:   filters.to   ? `${filters.to}T23:59:59`   : '',
        page, size: PAGE_SIZE,
      })
      setItems(res.content)
      setTotalPages(res.totalPages || 1)
    } catch (e) { toast.error(e.message) }
    setLoading(false)
  }

  function updateFilter(key, value) {
    setFilters(prev => ({ ...prev, [key]: value, ...(key === 'entityType' ? { action: '' } : {}) }))
    setPage(0)
  }

  function clearFilters() {
    setFilters({ entityType: '', action: '', from: '', to: '' })
    setPage(0)
  }

  const availableActions = filters.entityType
    ? Object.values(ActivityActions).filter(a => a.startsWith(entityActionPrefix(filters.entityType)))
    : Object.values(ActivityActions)

  const hasFilters = filters.entityType || filters.action || filters.from || filters.to

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-[22px] font-bold text-gray-900">Activity History</h1>
        <p className="text-gray-500 text-sm mt-1">A full audit trail of changes across the system.</p>
      </div>

      <div className="bg-white rounded-xl border border-gray-200/70 p-4 flex flex-wrap items-end gap-3">
        <div>
          <label className="block text-xs font-medium text-gray-500 mb-1">Entity</label>
          <select value={filters.entityType} onChange={e => updateFilter('entityType', e.target.value)}
            className="text-sm border border-gray-200 rounded-lg px-2.5 py-1.5 focus:outline-none focus:ring-2 focus:ring-primary-200">
            <option value="">All</option>
            {ENTITY_TYPES.map(t => <option key={t} value={t}>{entityLabel(t)}</option>)}
          </select>
        </div>
        <div>
          <label className="block text-xs font-medium text-gray-500 mb-1">Action</label>
          <select value={filters.action} onChange={e => updateFilter('action', e.target.value)}
            className="text-sm border border-gray-200 rounded-lg px-2.5 py-1.5 focus:outline-none focus:ring-2 focus:ring-primary-200">
            <option value="">All</option>
            {availableActions.map(a => <option key={a} value={a}>{activityLabel(a)}</option>)}
          </select>
        </div>
        <div>
          <label className="block text-xs font-medium text-gray-500 mb-1">From</label>
          <input type="date" value={filters.from} onChange={e => updateFilter('from', e.target.value)}
            className="text-sm border border-gray-200 rounded-lg px-2.5 py-1.5 focus:outline-none focus:ring-2 focus:ring-primary-200" />
        </div>
        <div>
          <label className="block text-xs font-medium text-gray-500 mb-1">To</label>
          <input type="date" value={filters.to} onChange={e => updateFilter('to', e.target.value)}
            className="text-sm border border-gray-200 rounded-lg px-2.5 py-1.5 focus:outline-none focus:ring-2 focus:ring-primary-200" />
        </div>
        {hasFilters && (
          <button onClick={clearFilters} className="text-sm text-gray-500 hover:text-gray-800 px-2 py-1.5">
            Clear filters
          </button>
        )}
      </div>

      {loading ? (
        <div className="flex items-center justify-center h-48"><div className="w-8 h-8 border-4 border-primary-600 border-t-transparent rounded-full animate-spin" /></div>
      ) : (
        <div className="bg-white rounded-xl border border-gray-200/70 overflow-x-auto">
          {items.length === 0 ? (
            <div className="text-center py-16">
              <span className="inline-flex w-12 h-12 rounded-full bg-gray-100 text-gray-400 items-center justify-center mb-3">
                <ClockIcon width={22} height={22} />
              </span>
              <p className="font-medium text-gray-500">No activity found</p>
            </div>
          ) : (
            <table className="w-full text-sm min-w-[720px]">
              <thead className="bg-[#FAFAFA] text-gray-500 text-xs uppercase tracking-wide border-b border-gray-200/70">
                <tr>
                  <th className="text-left px-4 py-2.5 font-medium">Action</th>
                  <th className="text-left px-4 py-2.5 font-medium">Description</th>
                  <th className="text-left px-4 py-2.5 font-medium">Actor</th>
                  <th className="text-left px-4 py-2.5 font-medium">When</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {items.map(item => {
                  const Icon = activityIcon(item.entityType)
                  return (
                    <tr key={item.id} className="hover:bg-gray-50/60 transition-colors duration-150">
                      <td className="px-4 py-2.5">
                        <span className={`inline-flex items-center gap-1.5 px-2 py-1 rounded-full text-xs font-medium ${activityColor(item.action)}`}>
                          <Icon width={12} height={12} /> {activityLabel(item.action)}
                        </span>
                      </td>
                      <td className="px-4 py-2.5 text-gray-800">{item.description}</td>
                      <td className="px-4 py-2.5 text-gray-600">{item.actorName}</td>
                      <td className="px-4 py-2.5 text-gray-400 whitespace-nowrap">{formatManilaDateTime(item.createdAt)}</td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          )}
          <Pagination page={page + 1} totalPages={totalPages} onChange={p => setPage(p - 1)} />
        </div>
      )}
    </div>
  )
}

function entityActionPrefix(entityType) {
  return { CHILD: 'CHILD', ATTENDANCE: 'ATTENDANCE', HEALTH_RECORD: 'HEALTH_RECORD', USER: 'USER', GUARDIAN: 'GUARDIAN' }[entityType] ?? ''
}
