'use client'

import { createContext, useContext, useState, useEffect, ReactNode } from 'react'
import type { User } from '@/types'
import { authApi } from './api'

// Define o FORMATO dos dados que o contexto vai fornecer
interface AuthContextType {
  user: User | null          // usuário logado (ou null se não logado)
  loading: boolean           // true enquanto verifica se há sessão
  signIn: (email: string, password: string) => Promise<void>
  signUp: (name: string, email: string, password: string, phone?: string) => Promise<void>
  signOut: () => Promise<void>
  refreshUser: () => Promise<void>
}

// Cria o contexto (como um "container" global de dados)
const AuthContext = createContext<AuthContextType | undefined>(undefined)

// Provider = componente que FORNECE os dados para os filhos
export function AuthProvider({ children }: { children: ReactNode }) {
  // Estados locais do provider
  const [user, setUser] = useState<User | null>(null)
  const [loading, setLoading] = useState(true)

  // Função para buscar dados do usuário logado
  const refreshUser = async () => {
    try {
      const data = await authApi.getMe()
      setUser(data?.user || null)
    } catch {
      setUser(null)
    }
  }

  // useEffect com [] = executa UMA vez quando o componente monta
  // Aqui: verifica se o usuário já está logado (tem cookie de sessão)
  useEffect(() => {
    refreshUser().finally(() => setLoading(false))
  }, [])

  // Funções de autenticação
  const signIn = async (email: string, password: string) => {
    const data = await authApi.signIn({ email, password })
    setUser(data.user)  // Atualiza o estado global
  }

  const signUp = async (name: string, email: string, password: string, phone?: string) => {
    const data = await authApi.signUp({ name, email, password, phone })
    setUser(data.user)
  }

  const signOut = async () => {
    await authApi.signOut()
    setUser(null)  // Limpa o usuário do estado
  }

  // Retorna o Provider que envolve os children
  // value = dados que serão acessíveis por qualquer componente filho
  return (
    <AuthContext.Provider value={{ user, loading, signIn, signUp, signOut, refreshUser }}>
      {children}
    </AuthContext.Provider>
  )
}

// Hook customizado para CONSUMIR o contexto
// Qualquer componente pode usar: const { user, signIn } = useAuth()
export function useAuth() {
  const context = useContext(AuthContext)
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}
