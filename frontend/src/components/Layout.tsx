import type { ReactNode } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

export function Layout({ children }: { children: ReactNode }) {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  return (
    <div className="min-h-screen">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-6 py-4">
          <Link to="/" className="text-lg font-semibold tracking-tight text-slate-900">
            ATS Resume Screener
          </Link>
          {user && (
            <nav className="flex items-center gap-6 text-sm">
              <Link to="/" className="text-slate-600 hover:text-slate-900">
                Dashboard
              </Link>
              <Link to="/upload" className="text-slate-600 hover:text-slate-900">
                Upload
              </Link>
              <span className="text-slate-400">|</span>
              <span className="text-slate-600">{user.fullName}</span>
              <button
                onClick={() => {
                  logout()
                  navigate('/login')
                }}
                className="rounded-md border border-slate-300 px-3 py-1.5 text-slate-700 hover:bg-slate-100"
              >
                Log out
              </button>
            </nav>
          )}
        </div>
      </header>
      <main className="mx-auto max-w-6xl px-6 py-8">{children}</main>
    </div>
  )
}
