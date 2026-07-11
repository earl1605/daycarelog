import { useEffect, useState } from 'react'

export const PAGE_SIZE = 10

export function usePagination(items, pageSize = PAGE_SIZE) {
  const [page, setPage] = useState(1)
  const totalPages = Math.max(1, Math.ceil(items.length / pageSize))

  // Jump back to page 1 whenever the filtered result count changes (e.g. a new search term).
  useEffect(() => { setPage(1) }, [items.length])

  const safePage = Math.min(page, totalPages)
  const paged = items.slice((safePage - 1) * pageSize, safePage * pageSize)
  return { page: safePage, setPage, totalPages, paged }
}
