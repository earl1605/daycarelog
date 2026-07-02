export default function StatCard({ icon: Icon, label, value, sub, color = 'primary' }) {
  const colors = {
    primary: 'bg-primary-50 text-primary-600',
    blue:    'bg-blue-50 text-blue-600',
    violet:  'bg-violet-50 text-violet-600',
    amber:   'bg-amber-50 text-amber-600',
  }
  return (
    <div className="bg-[#FAFAFA] rounded-xl border border-gray-200/70 p-4 md:p-5 flex items-center gap-4">
      <div className={`w-10 h-10 rounded-lg flex items-center justify-center shrink-0 ${colors[color] ?? colors.primary}`}>
        <Icon width={20} height={20} />
      </div>
      <div className="min-w-0">
        <p className="text-[12px] font-medium text-gray-500 uppercase tracking-wide truncate">{label}</p>
        <p className="text-[28px] leading-tight font-bold text-gray-900">{value ?? '—'}</p>
        {sub && <p className="text-xs text-gray-400">{sub}</p>}
      </div>
    </div>
  )
}
