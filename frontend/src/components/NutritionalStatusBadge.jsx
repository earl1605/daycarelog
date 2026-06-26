const colorMap = {
  green:  'bg-green-100 text-green-800',
  yellow: 'bg-yellow-100 text-yellow-800',
  orange: 'bg-orange-100 text-orange-800',
  red:    'bg-red-100 text-red-800',
  gray:   'bg-gray-100 text-gray-600',
}

export default function NutritionalStatusBadge({ status }) {
  if (!status) return null
  const color = colorMap[status.color] ?? colorMap.gray
  return (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${color}`}>
      {status.label}
    </span>
  )
}
