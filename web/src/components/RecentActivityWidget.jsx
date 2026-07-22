import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../lib/api'
import { useAuth } from '../contexts/AuthContext'
import { formatRelativeTime } from '../utils/date'
import { activityIcon, activityColor } from '../utils/activityMeta'
import { ClockIcon } from './icons'

export default function RecentActivityWidget() {
  const { isAdmin } = useAuth()
  const [items,   setItems]   = useState([])
  const [loading, setLoading] = useState(true)
  const [error,   setError]   = useState(false)

  useEffect(() => {
    let cancelled = false
    api.activity.recent(8)
      .then(res => { if (!cancelled) setItems(res.content) })
      .catch(() => { if (!cancelled) setError(true) })
      .finally(() => { if (!cancelled) setLoading(false) })
    return () => { cancelled = true }
  }, [])

  return (
    <div className="bg-white rounded-xl border border-gray-200/70 p-5">
      <div className="flex items-center justify-between mb-3">
        <h2 className="text-[15px] font-bold text-gray-900">Recent Activity</h2>
        {isAdmin && <Link to="/history" className="text-primary-700 text-sm font-medium hover:underline">View all →</Link>}
      </div>

      {loading ? (
        <div className="flex items-center justify-center h-24">
          <div className="w-6 h-6 border-4 border-primary-600 border-t-transparent rounded-full animate-spin" />
        </div>
      ) : error ? (
        <p className="text-gray-400 text-sm py-4 text-center">Couldn't load recent activity.</p>
      ) : items.length === 0 ? (
        <div className="text-center py-8">
          <span className="inline-flex w-10 h-10 rounded-full bg-gray-100 text-gray-400 items-center justify-center mb-2">
            <ClockIcon width={18} height={18} />
          </span>
          <p className="text-gray-400 text-sm">No activity yet</p>
        </div>
      ) : (
        <ul className="space-y-3">
          {items.map(item => {
            const Icon = activityIcon(item.entityType)
            return (
              <li key={item.id} className="flex items-start gap-3">
                <span className={`w-8 h-8 rounded-full flex items-center justify-center shrink-0 ${activityColor(item.action)}`}>
                  <Icon width={15} height={15} />
                </span>
                <div className="min-w-0 flex-1">
                  <p className="text-sm text-gray-800 leading-snug">{item.description}</p>
                  <p className="text-xs text-gray-400 mt-0.5">{item.actorName} · {formatRelativeTime(item.createdAt)}</p>
                </div>
              </li>
            )
          })}
        </ul>
      )}
    </div>
  )
}
