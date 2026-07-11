import { useEffect, useState } from 'react'
import { api } from '../lib/api'
import Pagination from '../components/Pagination'
import { usePagination } from '../utils/usePagination'
import { PlayIcon, TrashIcon, AlertTriangleIcon } from '../components/icons'
import toast from 'react-hot-toast'

const TAB = { HEALTH: 'health', IMMUNIZATIONS: 'immunizations' }

export default function RecycleBin() {
  const [children,      setChildren]      = useState({})
  const [trashedHealth, setTrashedHealth] = useState([])
  const [trashedImms,   setTrashedImms]   = useState([])
  const [tab,           setTab]           = useState(TAB.HEALTH)
  const [loading,       setLoading]       = useState(true)
  const [confirmTarget, setConfirmTarget] = useState(null)

  useEffect(() => { load() }, [])

  async function load() {
    setLoading(true)
    try {
      const [kids, health, imms] = await Promise.all([
        api.children.list(), api.health.trash(), api.immunizations.trash(),
      ])
      const map = {}; kids.forEach(k => { map[k.id] = k })
      setChildren(map); setTrashedHealth(health); setTrashedImms(imms)
    } catch (e) { toast.error(e.message) }
    setLoading(false)
  }

  function childName(childId) {
    const c = children[childId]
    return c ? `${c.firstName} ${c.lastName}` : '—'
  }

  async function handleRestore(kind, id) {
    try {
      if (kind === TAB.HEALTH) {
        await api.health.restore(id)
        setTrashedHealth(prev => prev.filter(r => r.id !== id))
      } else {
        await api.immunizations.restore(id)
        setTrashedImms(prev => prev.filter(r => r.id !== id))
      }
      toast.success('Record restored')
    } catch (e) { toast.error(e.message) }
  }

  async function handlePermanentDelete() {
    const { kind, id } = confirmTarget
    setConfirmTarget(null)
    try {
      if (kind === TAB.HEALTH) {
        await api.health.permanentDelete(id)
        setTrashedHealth(prev => prev.filter(r => r.id !== id))
      } else {
        await api.immunizations.permanentDelete(id)
        setTrashedImms(prev => prev.filter(r => r.id !== id))
      }
      toast.success('Permanently deleted')
    } catch (e) { toast.error(e.message) }
  }

  const { page, setPage, totalPages, paged } = usePagination(tab === TAB.HEALTH ? trashedHealth : trashedImms)

  const tabs = [
    { key: TAB.HEALTH,         label: `Health Records (${trashedHealth.length})` },
    { key: TAB.IMMUNIZATIONS,  label: `Immunizations (${trashedImms.length})` },
  ]

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-[22px] font-bold text-gray-900">Recycle Bin</h1>
        <p className="text-gray-500 text-sm mt-1">Deleted health records and immunizations land here before they're gone for good.</p>
      </div>

      <div className="flex gap-1 bg-gray-100 p-1 rounded-xl max-w-md">
        {tabs.map(t => (
          <button key={t.key} onClick={() => setTab(t.key)}
            className={`flex-1 text-sm font-medium py-2 rounded-lg transition-colors ${tab === t.key ? 'bg-white shadow-sm text-gray-900' : 'text-gray-500 hover:text-gray-700'}`}>
            {t.label}
          </button>
        ))}
      </div>

      {loading ? (
        <div className="flex items-center justify-center h-48"><div className="w-8 h-8 border-4 border-primary-600 border-t-transparent rounded-full animate-spin" /></div>
      ) : (
        <div className="bg-white rounded-xl border border-gray-200/70 overflow-x-auto">
          {paged.length === 0 ? (
            <div className="text-center py-16">
              <span className="inline-flex w-12 h-12 rounded-full bg-gray-100 text-gray-400 items-center justify-center mb-3">
                <TrashIcon width={22} height={22} />
              </span>
              <p className="font-medium text-gray-500">Nothing in the recycle bin</p>
            </div>
          ) : (
            <table className="w-full text-sm min-w-[640px]">
              <thead className="bg-[#FAFAFA] text-gray-500 text-xs uppercase tracking-wide border-b border-gray-200/70">
                <tr>
                  <th className="text-left px-4 py-3 font-medium">Child</th>
                  {tab === TAB.HEALTH ? (
                    <>
                      <th className="text-left px-4 py-3 font-medium">Date</th>
                      <th className="text-left px-4 py-3 font-medium">Weight / Height</th>
                    </>
                  ) : (
                    <>
                      <th className="text-left px-4 py-3 font-medium">Vaccine</th>
                      <th className="text-left px-4 py-3 font-medium">Dose</th>
                    </>
                  )}
                  <th className="text-left px-4 py-3 font-medium">Deleted</th>
                  <th className="text-left px-4 py-3 font-medium">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {paged.map(r => (
                  <tr key={r.id} className="hover:bg-gray-50/60 transition-colors duration-150">
                    <td className="px-4 py-3 font-medium text-gray-900">{childName(r.childId)}</td>
                    {tab === TAB.HEALTH ? (
                      <>
                        <td className="px-4 py-3 text-gray-600">{new Date(r.measurementDate + 'T00:00:00').toLocaleDateString('en-PH')}</td>
                        <td className="px-4 py-3 text-gray-600">{r.weightKg ? `${r.weightKg}kg` : '—'} / {r.heightCm ? `${r.heightCm}cm` : '—'}</td>
                      </>
                    ) : (
                      <>
                        <td className="px-4 py-3 text-gray-600">{r.vaccineName}</td>
                        <td className="px-4 py-3 text-gray-600">Dose {r.doseNumber}</td>
                      </>
                    )}
                    <td className="px-4 py-3 text-gray-400">{new Date(r.deletedAt).toLocaleString('en-PH')}</td>
                    <td className="px-4 py-3">
                      <div className="flex flex-wrap gap-1.5">
                        <button
                          onClick={() => handleRestore(tab, r.id)}
                          className="inline-flex items-center gap-1 px-2.5 py-1 rounded-lg bg-green-50 text-green-700 text-xs font-semibold hover:bg-green-100 transition-colors duration-150"
                        >
                          <PlayIcon width={13} height={13} /> Restore
                        </button>
                        <button
                          onClick={() => setConfirmTarget({ kind: tab, id: r.id })}
                          className="inline-flex items-center gap-1 px-2.5 py-1 rounded-lg bg-red-50 text-red-600 text-xs font-semibold hover:bg-red-100 transition-colors duration-150"
                        >
                          <TrashIcon width={13} height={13} /> Delete Forever
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
          <Pagination page={page} totalPages={totalPages} onChange={setPage} />
        </div>
      )}

      {confirmTarget && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4">
          <div className="bg-white rounded-xl shadow-2xl w-full max-w-sm p-6 animate-scale-in">
            <span className="mx-auto flex w-12 h-12 rounded-full bg-red-50 text-red-600 items-center justify-center mb-3">
              <AlertTriangleIcon width={22} height={22} />
            </span>
            <h2 className="text-[17px] font-bold text-gray-900 text-center mb-1">Delete forever?</h2>
            <p className="text-sm text-gray-500 text-center mb-5">
              This cannot be undone. The record will be permanently removed.
            </p>
            <div className="flex gap-3">
              <button onClick={() => setConfirmTarget(null)}
                className="flex-1 py-2.5 rounded-lg border border-gray-200 text-sm font-semibold text-gray-600 hover:bg-gray-50 transition-colors duration-150">
                Cancel
              </button>
              <button onClick={handlePermanentDelete}
                className="flex-1 py-2.5 rounded-lg bg-red-600 hover:bg-red-700 text-white text-sm font-semibold transition-colors duration-150">
                Yes, Delete Forever
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
