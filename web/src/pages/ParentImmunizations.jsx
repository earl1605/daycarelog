import { useEffect, useState } from 'react'
import { api } from '../lib/api'
import ImmunizationChecklist from '../components/ImmunizationChecklist'
import toast from 'react-hot-toast'

export default function ParentImmunizations() {
  const [children,     setChildren]     = useState([])
  const [records,      setRecords]      = useState([])
  const [schedule,     setSchedule]     = useState([])
  const [selectedChild, setSelectedChild] = useState('')
  const [loading,      setLoading]      = useState(true)

  useEffect(() => {
    async function load() {
      try {
        const [kids, recs, sch] = await Promise.all([api.children.mine(), api.immunizations.mine(), api.immunizations.schedule()])
        setChildren(kids)
        setRecords(recs)
        setSchedule(sch)
        if (kids.length > 0) setSelectedChild(String(kids[0].id))
      } catch { toast.error('Failed to load immunizations') }
      setLoading(false)
    }
    load()
  }, [])

  const childRecords = records.filter(r => r.childId === Number(selectedChild))

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between flex-wrap gap-3">
        <h1 className="text-[22px] font-bold text-gray-900">Immunizations</h1>
        {children.length > 1 && (
          <select value={selectedChild} onChange={e => setSelectedChild(e.target.value)}
            className="border border-gray-200 rounded-lg px-3 py-2 text-sm text-gray-900 focus:outline-none focus:ring-2 focus:ring-primary-500/30 focus:border-primary-400">
            {children.map(c => <option key={c.id} value={c.id}>{c.firstName} {c.lastName}</option>)}
          </select>
        )}
      </div>

      {loading ? (
        <div className="flex items-center justify-center h-48"><div className="w-8 h-8 border-4 border-primary-600 border-t-transparent rounded-full animate-spin" /></div>
      ) : children.length === 0 ? (
        <p className="text-gray-400 text-sm">No children linked to your account.</p>
      ) : (
        <ImmunizationChecklist schedule={schedule} records={childRecords} />
      )}
    </div>
  )
}
