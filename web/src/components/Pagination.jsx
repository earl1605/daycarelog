export default function Pagination({ page, totalPages, onChange }) {
  if (totalPages <= 1) return null
  return (
    <div className="flex items-center justify-between px-4 py-3 border-t border-gray-200/70 text-sm">
      <button onClick={() => onChange(page - 1)} disabled={page <= 1}
        className="px-3 py-1.5 rounded-lg border border-gray-200 text-gray-600 disabled:opacity-40 disabled:cursor-not-allowed hover:bg-gray-50 transition-colors duration-150">
        Previous
      </button>
      <span className="text-gray-500">Page {page} of {totalPages}</span>
      <button onClick={() => onChange(page + 1)} disabled={page >= totalPages}
        className="px-3 py-1.5 rounded-lg border border-gray-200 text-gray-600 disabled:opacity-40 disabled:cursor-not-allowed hover:bg-gray-50 transition-colors duration-150">
        Next
      </button>
    </div>
  )
}
