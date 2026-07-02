import { Link } from 'react-router-dom'
import { formatAge } from '../utils/nutritionalStatus'
import NutritionalStatusBadge from './NutritionalStatusBadge'

export default function ChildCard({ child, status }) {
  const initials = `${child.firstName?.[0] ?? ''}${child.lastName?.[0] ?? ''}`.toUpperCase()
  return (
    <Link
      to={`/children/${child.id}`}
      className="bg-[#FAFAFA] rounded-xl border border-gray-200/70 p-5 flex items-center gap-4 transition-colors duration-150 hover:bg-gray-100/70 hover:border-gray-300"
    >
      <div className="w-12 h-12 rounded-full bg-primary-100 text-primary-700 flex items-center justify-center font-bold text-lg shrink-0">
        {initials}
      </div>
      <div className="flex-1 min-w-0">
        <p className="font-semibold text-gray-900 truncate">{child.firstName} {child.lastName}</p>
        <p className="text-sm text-gray-500">{child.sex} · {formatAge(child.dateOfBirth)}</p>
        {status && <NutritionalStatusBadge status={status} />}
      </div>
      <span className={`shrink-0 w-2 h-2 rounded-full ${child.enrollmentStatus === 'active' ? 'bg-green-400' : 'bg-gray-300'}`} />
    </Link>
  )
}
