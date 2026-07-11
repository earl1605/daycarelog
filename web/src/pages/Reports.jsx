import { useEffect, useState } from 'react'
import { PieChart, Pie, Cell, Legend, Tooltip, BarChart, Bar, XAxis, YAxis, CartesianGrid, ResponsiveContainer } from 'recharts'
import { api } from '../lib/api'
import { useTheme } from '../contexts/ThemeContext'
import { toLocalDateString } from '../utils/date'
import Pagination from '../components/Pagination'
import { usePagination } from '../utils/usePagination'
import toast from 'react-hot-toast'

const STATUS_LABELS = { NORMAL: 'Normal', UNDERWEIGHT: 'Underweight', SEVERELY_UNDERWEIGHT: 'Severely Underweight', OVERWEIGHT: 'Overweight', UNKNOWN: 'Unknown' }
const STATUS_COLORS = { NORMAL: 'bg-green-400', UNDERWEIGHT: 'bg-orange-400', SEVERELY_UNDERWEIGHT: 'bg-red-400', OVERWEIGHT: 'bg-yellow-400', UNKNOWN: 'bg-gray-300' }
const STATUS_HEX    = { NORMAL: '#4ade80', UNDERWEIGHT: '#fb923c', SEVERELY_UNDERWEIGHT: '#f87171', OVERWEIGHT: '#facc15', UNKNOWN: '#d1d5db' }
const STATUS_BADGE   = { NORMAL: 'bg-green-100 text-green-800', UNDERWEIGHT: 'bg-orange-100 text-orange-800', SEVERELY_UNDERWEIGHT: 'bg-red-100 text-red-800', OVERWEIGHT: 'bg-yellow-100 text-yellow-800', UNKNOWN: 'bg-gray-100 text-gray-600' }

export default function Reports() {
  const { theme } = useTheme()
  const isDark = theme === 'dark'
  const [data,    setData]    = useState(null)
  const [month,   setMonth]   = useState(toLocalDateString().slice(0, 7))
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    setLoading(true)
    api.reports.monthly(month).then(d => { setData(d); setLoading(false) }).catch(e => { toast.error(e.message); setLoading(false) })
  }, [month])

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

  const pieData = Object.entries(data?.nutritionalStatus ?? {})
    .filter(([, count]) => count > 0)
    .map(([code, count]) => ({ code, name: STATUS_LABELS[code] ?? code, value: count }))

  const tooltipStyle = {
    borderRadius: '10px',
    border: `1px solid ${isDark ? '#374151' : '#E5E7EB'}`,
    boxShadow: '0 4px 16px rgba(0,0,0,0.06)',
    fontSize: '13px',
    backgroundColor: isDark ? '#1f2937' : '#fff',
    color: isDark ? '#f9fafb' : '#111827',
  }

  return (
    <div className="space-y-6">
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
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="bg-white rounded-xl border border-gray-200/70 p-6">
              <h2 className="text-[17px] font-bold text-gray-900 mb-4">Attendance Summary</h2>
              <div className="space-y-3 text-sm">
                {[['Active Children', data.total], ['School Days', data.schoolDays], ['Total Present', data.presentCount], ['Total Absent', data.absentCount], ['Attendance Rate', `${data.attendanceRate}%`]].map(([l, v]) => (
                  <div key={l} className="flex justify-between items-center">
                    <span className="text-gray-500">{l}</span>
                    <span className="font-semibold text-gray-900">{v}</span>
                  </div>
                ))}
              </div>
            </div>

            <div className="bg-white rounded-xl border border-gray-200/70 p-6">
              <h2 className="text-[17px] font-bold text-gray-900 mb-4">Nutritional Status</h2>
              {pieData.length === 0 ? (
                <p className="text-gray-400 text-sm">No health records yet.</p>
              ) : (
                <ResponsiveContainer width="100%" height={220}>
                  <PieChart>
                    <Pie data={pieData} dataKey="value" nameKey="name" cx="50%" cy="50%" innerRadius={55} outerRadius={85} paddingAngle={2}>
                      {pieData.map(d => <Cell key={d.code} fill={STATUS_HEX[d.code] ?? '#d1d5db'} />)}
                    </Pie>
                    <Tooltip contentStyle={tooltipStyle} />
                    <Legend verticalAlign="bottom" height={36} wrapperStyle={{ fontSize: '12px' }} />
                  </PieChart>
                </ResponsiveContainer>
              )}
            </div>
          </div>

          <div className="bg-white rounded-xl border border-gray-200/70 p-6">
            <h2 className="text-[17px] font-bold text-gray-900 mb-4">Immunization Coverage</h2>
            <p className="text-xs text-gray-400 -mt-3 mb-4">Share of currently active children fully dosed for each vaccine (not limited to the selected month).</p>
            {(data.immunizationCoverage ?? []).length === 0 ? (
              <p className="text-gray-400 text-sm">No vaccine schedule data.</p>
            ) : (
              <ResponsiveContainer width="100%" height={240}>
                <BarChart data={data.immunizationCoverage} barCategoryGap="30%">
                  <CartesianGrid strokeDasharray="3 3" stroke={isDark ? '#374151' : '#F0F0EE'} vertical={false} />
                  <XAxis dataKey="vaccine" tick={{ fontSize: 11, fill: '#9CA3AF' }} axisLine={false} tickLine={false} />
                  <YAxis tick={{ fontSize: 12, fill: '#9CA3AF' }} axisLine={false} tickLine={false} allowDecimals={false} />
                  <Tooltip
                    cursor={{ fill: isDark ? '#374151' : '#F7F7F5' }}
                    contentStyle={tooltipStyle}
                    formatter={(value, _name, props) => [`${value} / ${props.payload.total}`, 'Covered']}
                  />
                  <Bar dataKey="covered" name="Covered" fill="#16a34a" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            )}
          </div>

          <div className="bg-white rounded-xl border border-gray-200/70 overflow-x-auto">
            <div className="flex items-center justify-between px-6 py-4">
              <h2 className="text-[17px] font-bold text-gray-900">Health Records This Month</h2>
              <span className="text-sm text-gray-400">{healthRecords.length} record{healthRecords.length !== 1 ? 's' : ''}</span>
            </div>
            {healthRecords.length === 0 ? (
              <p className="text-gray-400 text-sm px-6 pb-6">No health records logged in {month}.</p>
            ) : (
              <table className="w-full text-sm min-w-[640px]">
                <thead className="bg-[#FAFAFA] text-gray-500 text-xs uppercase tracking-wide border-b border-gray-200/70">
                  <tr>
                    <th className="text-left px-4 py-3 font-medium">Child</th>
                    <th className="text-left px-4 py-3 font-medium">Date</th>
                    <th className="text-left px-4 py-3 font-medium">Weight</th>
                    <th className="text-left px-4 py-3 font-medium">Height</th>
                    <th className="text-left px-4 py-3 font-medium">Status</th>
                    <th className="text-left px-4 py-3 font-medium">Remarks</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {paged.map(r => {
                    const child = childMap[r.childId]
                    return (
                      <tr key={r.id} className="hover:bg-gray-50/60 transition-colors duration-150">
                        <td className="px-4 py-3 font-medium text-gray-900">{child ? `${child.firstName} ${child.lastName}` : '—'}</td>
                        <td className="px-4 py-3 text-gray-600">{new Date(r.measurementDate + 'T00:00:00').toLocaleDateString('en-PH')}</td>
                        <td className="px-4 py-3 text-gray-600">{r.weightKg ? `${r.weightKg} kg` : '—'}</td>
                        <td className="px-4 py-3 text-gray-600">{r.heightCm ? `${r.heightCm} cm` : '—'}</td>
                        <td className="px-4 py-3">
                          {r.nutritionalStatus ? (
                            <span className={`text-xs font-medium px-2 py-0.5 rounded-full ${STATUS_BADGE[r.nutritionalStatus] ?? 'bg-gray-100 text-gray-600'}`}>
                              {STATUS_LABELS[r.nutritionalStatus] ?? r.nutritionalStatus}
                            </span>
                          ) : '—'}
                        </td>
                        <td className="px-4 py-3 text-gray-400">{r.remarks || '—'}</td>
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
