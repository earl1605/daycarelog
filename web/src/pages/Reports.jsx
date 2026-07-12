import { useEffect, useState } from 'react'
import { LineChart, Line, PieChart, Pie, Cell, Tooltip, Legend, XAxis, YAxis, CartesianGrid, ResponsiveContainer } from 'recharts'
import { api } from '../lib/api'
import { useTheme } from '../contexts/ThemeContext'
import { toLocalDateString } from '../utils/date'
import { computeNutritionalTrend, computeImmunizationDetail } from '../utils/healthTrends'
import Pagination from '../components/Pagination'
import { usePagination } from '../utils/usePagination'
import toast from 'react-hot-toast'

const STATUS_LABELS = { NORMAL: 'Normal', UNDERWEIGHT: 'Underweight', SEVERELY_UNDERWEIGHT: 'Severely Underweight', OVERWEIGHT: 'Overweight', UNKNOWN: 'Unknown' }
const STATUS_BADGE  = { NORMAL: 'bg-green-100 text-green-800', UNDERWEIGHT: 'bg-orange-100 text-orange-800', SEVERELY_UNDERWEIGHT: 'bg-red-100 text-red-800', OVERWEIGHT: 'bg-yellow-100 text-yellow-800', UNKNOWN: 'bg-gray-100 text-gray-600' }
// Status-severity colors (good/warning/serious/critical), not arbitrary categorical hues.
const STATUS_HEX    = { Normal: '#0ca30c', Underweight: '#ec835a', 'Severely Underweight': '#d03b3b', Overweight: '#fab219' }
const IMMUNIZATION_BUCKET_HEX = { 'Fully Immunized': '#0ca30c', 'Partially Immunized': '#fab219', 'Not Started': '#d03b3b' }

export default function Reports() {
  const { theme } = useTheme()
  const isDark = theme === 'dark'
  const [data,    setData]    = useState(null)
  const [month,   setMonth]   = useState(toLocalDateString().slice(0, 7))
  const [loading, setLoading] = useState(true)
  const [nutritionalTrend,   setNutritionalTrend]   = useState([])
  const [immunizationDetail, setImmunizationDetail] = useState({ buckets: [], perVaccine: [] })

  useEffect(() => {
    setLoading(true)
    api.reports.monthly(month).then(d => { setData(d); setLoading(false) }).catch(e => { toast.error(e.message); setLoading(false) })
  }, [month])

  // Independent of the selected month -- always "as of today" -- so it only needs to load once.
  useEffect(() => {
    Promise.all([api.children.list(), api.health.list(), api.immunizations.list(), api.immunizations.schedule()])
      .then(([children, health, immunizations, schedule]) => {
        const activeChildren = children.filter(c => c.enrollmentStatus === 'active')
        setNutritionalTrend(computeNutritionalTrend(activeChildren, health))
        setImmunizationDetail(computeImmunizationDetail(activeChildren, immunizations, schedule))
      })
      .catch(() => toast.error('Failed to load trend data'))
  }, [])

  function downloadCSV() {
    if (!data) return
    const rows = [['Name', 'Sex', 'Date of Birth', 'Nutritional Status']]
    ;(data.children ?? []).forEach(c => rows.push([`${c.firstName} ${c.lastName}`, c.sex, c.dateOfBirth, '—']))
    const csv  = rows.map(r => r.join(',')).join('\n')
    const blob = new Blob([csv], { type: 'text/csv' })
    const url  = URL.createObjectURL(blob)
    const a    = document.createElement('a'); a.href = url; a.download = `report_${month}.csv`; a.click()
    URL.revokeObjectURL(url)
    toast.success('Report downloaded')
  }

  const childMap = Object.fromEntries((data?.children ?? []).map(c => [c.id, c]))
  const healthRecords = data?.healthRecords ?? []
  const { page, setPage, totalPages, paged } = usePagination(healthRecords)

  const tooltipStyle = {
    borderRadius: '10px',
    border: `1px solid ${isDark ? '#374151' : '#E5E7EB'}`,
    boxShadow: '0 4px 16px rgba(0,0,0,0.06)',
    fontSize: '13px',
    backgroundColor: isDark ? '#1f2937' : '#fff',
    color: isDark ? '#f9fafb' : '#111827',
  }

  return (
    <div className="space-y-5">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h1 className="text-[22px] font-bold text-gray-900">Reports</h1>
        <div className="flex gap-2 flex-wrap">
          <input type="month" value={month} onChange={e => setMonth(e.target.value)}
            className="border border-gray-200 rounded-lg px-3 py-2 text-sm text-gray-900 focus:outline-none focus:ring-2 focus:ring-primary-500/30 focus:border-primary-400" />
          <button onClick={downloadCSV} className="bg-primary-600 hover:bg-primary-700 text-white text-sm font-semibold px-4 py-2.5 rounded-lg transition-colors duration-150">↓ Export CSV</button>
        </div>
      </div>

      {loading ? (
        <div className="flex items-center justify-center h-48"><div className="w-8 h-8 border-4 border-primary-600 border-t-transparent rounded-full animate-spin" /></div>
      ) : data && (
        <>
          <div className="bg-white rounded-xl border border-gray-200/70 p-5">
            <h2 className="text-[15px] font-bold text-gray-900 mb-3">Attendance Summary</h2>
            <div className="grid grid-cols-2 sm:grid-cols-5 gap-4 text-sm">
              {[['Active Children', data.total], ['School Days', data.schoolDays], ['Total Present', data.presentCount], ['Total Absent', data.absentCount], ['Attendance Rate', `${data.attendanceRate}%`]].map(([l, v]) => (
                <div key={l}>
                  <p className="text-gray-400">{l}</p>
                  <p className="font-semibold text-gray-900 text-lg">{v}</p>
                </div>
              ))}
            </div>
          </div>

          <div className="bg-white rounded-xl border border-gray-200/70 p-5">
            <h2 className="text-[15px] font-bold text-gray-900 mb-3">Nutritional Status Trend</h2>
            {data.total === 0 ? (
              <p className="text-gray-400 text-sm">No active children yet.</p>
            ) : (
              <ResponsiveContainer width="100%" height={200}>
                <LineChart data={nutritionalTrend}>
                  <CartesianGrid strokeDasharray="3 3" stroke={isDark ? '#374151' : '#F0F0EE'} vertical={false} />
                  <XAxis dataKey="month" tick={{ fontSize: 11, fill: '#9CA3AF' }} axisLine={false} tickLine={false} />
                  <YAxis tick={{ fontSize: 11, fill: '#9CA3AF' }} axisLine={false} tickLine={false} domain={[0, 100]} unit="%" width={36} />
                  <Tooltip contentStyle={tooltipStyle} formatter={value => `${value}%`} />
                  <Legend wrapperStyle={{ fontSize: '11px' }} />
                  {Object.entries(STATUS_HEX).map(([key, color]) => (
                    <Line key={key} type="monotone" dataKey={key} stroke={color} strokeWidth={2} dot={{ r: 3 }} />
                  ))}
                </LineChart>
              </ResponsiveContainer>
            )}
          </div>

          <div className="bg-white rounded-xl border border-gray-200/70 p-5">
            <h2 className="text-[15px] font-bold text-gray-900 mb-3">Immunization Coverage</h2>
            {data.total === 0 ? (
              <p className="text-gray-400 text-sm">No active children yet.</p>
            ) : (
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-5 items-center">
                <ResponsiveContainer width="100%" height={220}>
                  <PieChart>
                    <Pie data={immunizationDetail.buckets} dataKey="value" nameKey="name" cx="50%" cy="50%" innerRadius={58} outerRadius={90} paddingAngle={2}>
                      {immunizationDetail.buckets.map(b => <Cell key={b.name} fill={IMMUNIZATION_BUCKET_HEX[b.name]} />)}
                    </Pie>
                    <Tooltip contentStyle={tooltipStyle} />
                    <Legend verticalAlign="bottom" height={36} wrapperStyle={{ fontSize: '12px' }} />
                  </PieChart>
                </ResponsiveContainer>
                <div className="space-y-2">
                  <p className="text-xs font-semibold text-gray-400 uppercase tracking-wide mb-1">By vaccine</p>
                  {immunizationDetail.perVaccine.map(v => {
                    const pct = v.total > 0 ? Math.round((v.covered / v.total) * 100) : 0
                    return (
                      <div key={v.vaccine} className="flex items-center justify-between text-sm">
                        <span className="text-gray-600">{v.vaccine}</span>
                        <span className="font-medium text-gray-900">{v.covered}/{v.total} <span className="text-gray-400 font-normal">({pct}%)</span></span>
                      </div>
                    )
                  })}
                </div>
              </div>
            )}
          </div>

          <div className="bg-white rounded-xl border border-gray-200/70 overflow-x-auto">
            <div className="flex items-center justify-between px-5 py-3">
              <h2 className="text-[15px] font-bold text-gray-900">Health Records This Month</h2>
              <span className="text-sm text-gray-400">{healthRecords.length} record{healthRecords.length !== 1 ? 's' : ''}</span>
            </div>
            {healthRecords.length === 0 ? (
              <p className="text-gray-400 text-sm px-5 pb-5">No health records logged in {month}.</p>
            ) : (
              <table className="w-full text-sm min-w-[640px]">
                <thead className="bg-[#FAFAFA] text-gray-500 text-xs uppercase tracking-wide border-b border-gray-200/70">
                  <tr>
                    <th className="text-left px-4 py-2.5 font-medium">Child</th>
                    <th className="text-left px-4 py-2.5 font-medium">Date</th>
                    <th className="text-left px-4 py-2.5 font-medium">Weight</th>
                    <th className="text-left px-4 py-2.5 font-medium">Height</th>
                    <th className="text-left px-4 py-2.5 font-medium">Status</th>
                    <th className="text-left px-4 py-2.5 font-medium">Remarks</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {paged.map(r => {
                    const child = childMap[r.childId]
                    return (
                      <tr key={r.id} className="hover:bg-gray-50/60 transition-colors duration-150">
                        <td className="px-4 py-2.5 font-medium text-gray-900">{child ? `${child.firstName} ${child.lastName}` : '—'}</td>
                        <td className="px-4 py-2.5 text-gray-600">{new Date(r.measurementDate + 'T00:00:00').toLocaleDateString('en-PH')}</td>
                        <td className="px-4 py-2.5 text-gray-600">{r.weightKg ? `${r.weightKg} kg` : '—'}</td>
                        <td className="px-4 py-2.5 text-gray-600">{r.heightCm ? `${r.heightCm} cm` : '—'}</td>
                        <td className="px-4 py-2.5">
                          {r.nutritionalStatus ? (
                            <span className={`text-xs font-medium px-2 py-0.5 rounded-full ${STATUS_BADGE[r.nutritionalStatus] ?? 'bg-gray-100 text-gray-600'}`}>
                              {STATUS_LABELS[r.nutritionalStatus] ?? r.nutritionalStatus}
                            </span>
                          ) : '—'}
                        </td>
                        <td className="px-4 py-2.5 text-gray-400">{r.remarks || '—'}</td>
                      </tr>
                    )
                  })}
                </tbody>
              </table>
            )}
            <Pagination page={page} totalPages={totalPages} onChange={setPage} />
          </div>
        </>
      )}
    </div>
  )
}
