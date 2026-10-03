import { createContext, useContext, useMemo, useState, type ReactNode } from 'react'
import { api, setToken, getToken } from '../api/client'
import type { AuthResponse, Role } from '../api/types'

interface CurrentUser {
  email: string
  fullName: string
  role: Role
}

interface AuthContextValue {
  user: CurrentUser | null
  login: (email: string, password: string) => Promise<void>
  register: (email: string, password: string, fullName: string) => Promise<void>
  logout: () => void
}

const USER_STORAGE_KEY = 'ats_resume_user'

const AuthContext = createContext<AuthContextValue | null>(null)

function loadStoredUser(): CurrentUser | null {
  if (!getToken()) return null
  const raw = localStorage.getItem(USER_STORAGE_KEY)
  return raw ? (JSON.parse(raw) as CurrentUser) : null
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<CurrentUser | null>(loadStoredUser)

  const applyAuthResponse = (response: AuthResponse) => {
    setToken(response.token)
    const currentUser: CurrentUser = { email: response.email, fullName: response.fullName, role: response.role }
    localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(currentUser))
    setUser(currentUser)
  }

  const login = async (email: string, password: string) => {
    const response = await api.post<AuthResponse>('/auth/login', { email, password })
    applyAuthResponse(response)
  }

  const register = async (email: string, password: string, fullName: string) => {
    const response = await api.post<AuthResponse>('/auth/register', { email, password, fullName })
    applyAuthResponse(response)
  }

  const logout = () => {
    setToken(null)
    localStorage.removeItem(USER_STORAGE_KEY)
    setUser(null)
  }

  const value = useMemo(() => ({ user, login, register, logout }), [user])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}
