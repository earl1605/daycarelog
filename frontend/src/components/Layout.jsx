import { useState } from 'react'
import { Outlet } from 'react-router-dom'
import Sidebar from './Sidebar'
import { MenuIcon } from './icons'

export default function Layout() {
  const [sidebarOpen, setSidebarOpen] = useState(false)

  return (
    <div className="flex h-screen bg-white overflow-hidden">
      <Sidebar open={sidebarOpen} onClose={() => setSidebarOpen(false)} />

      <div className="flex-1 flex flex-col overflow-hidden">
        {/* Mobile top bar */}
        <header className="lg:hidden flex items-center gap-3 px-4 py-3 bg-[#F7F7F5] border-b border-gray-200/70">
          <button
            onClick={() => setSidebarOpen(true)}
            className="p-2 rounded-lg text-gray-500 hover:bg-gray-900/[0.04] hover:text-gray-800 transition-colors duration-150"
          >
            <MenuIcon width={20} height={20} />
          </button>
          <span className="font-bold text-gray-900 text-[15px]">DaycareLog</span>
        </header>

        <main className="flex-1 overflow-y-auto p-4 md:p-6 bg-white">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
