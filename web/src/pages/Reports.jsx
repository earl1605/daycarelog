import { useEffect, useState } from 'react'
import { api } from '../lib/api'
import toast from 'react-hot-toast'

export default function Reports() {
  const [data,    setData]    = useState(null)
  const [month,   setMonth]   = useState(new Date().toISOString().slice(0, 7))
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
            <div className="space-y-3">
              {Object.entries(data.nutritionalStatus ?? {}).map(([label, count]) => {
                const pct = data.total > 0 ? Math.round((count / data.total) * 100) : 0
                const barColor = label === 'Normal' ? 'bg-green-400' : label === 'Overweight' ? 'bg-yellow-400' : label === 'Underweight' ? 'bg-orange-400' : label === 'Severely Underweight' ? 'bg-red-400' : 'bg-gray-300'
                return (
                  <div key={label}>
                    <div className="flex justify-between text-sm mb-1">
                      <span className="text-gray-600">{label}</span>
                      <span className="font-semibold">{count} <span className="text-gray-400 font-normal">({pct}%)</span></span>
                    </div>
                    <div className="h-2 rounded-full bg-gray-100">
                      <div className={`h-2 rounded-full ${barColor} transition-all duration-500`} style={{ width: `${pct}%` }} />
                    </div>
                  </div>
                )
              })}
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
