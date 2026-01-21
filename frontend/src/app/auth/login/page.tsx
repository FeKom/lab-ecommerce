'use client'

// 'use client' porque usamos hooks (useState) e eventos (onSubmit)

import { useState } from 'react'
import { useRouter } from 'next/navigation'  // Hook do Next.js para navegação
import Link from 'next/link'
import { useAuth } from '@/lib/auth-context'

export default function LoginPage() {
  // useRouter = hook para navegar programaticamente
  const router = useRouter()

  // useAuth = nosso hook customizado para autenticação
  const { signIn } = useAuth()

  // Estados do formulário
  // FormEvent = tipo TypeScript para eventos de formulário
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  // Função executada ao submeter o formulário
  const handleSubmit = async (e: React.FormEvent) => {
    // preventDefault = impede o comportamento padrão (reload da página)
    e.preventDefault()

    setError('')
    setLoading(true)

    try {
      // Chama a API de login
      await signIn(email, password)
      // Se sucesso, redireciona para a home
      router.push('/')
    } catch (err) {
      // Se erro, mostra mensagem
      setError(err instanceof Error ? err.message : 'Erro ao fazer login')
    } finally {
      // finally = executa sempre, com ou sem erro
      setLoading(false)
    }
  }

  return (
    <div className="max-w-md mx-auto">
      <div className="bg-white rounded-xl shadow-sm p-8">
        <h1 className="text-2xl font-bold text-gray-800 mb-6 text-center">
          Entrar
        </h1>

        {/* Mensagem de erro */}
        {error && (
          <div className="bg-red-50 text-red-600 p-3 rounded-lg mb-4 text-sm">
            {error}
          </div>
        )}

        {/* Formulário */}
        {/* onSubmit = evento disparado ao submeter */}
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label
              htmlFor="email"
              className="block text-sm font-medium text-gray-700 mb-1"
            >
              Email
            </label>
            <input
              type="email"
              id="email"
              value={email}
              // onChange = evento disparado a cada digitação
              // e.target.value = valor atual do input
              onChange={(e) => setEmail(e.target.value)}
              required
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none transition-all"
              placeholder="seu@email.com"
            />
          </div>

          <div>
            <label
              htmlFor="password"
              className="block text-sm font-medium text-gray-700 mb-1"
            >
              Senha
            </label>
            <input
              type="password"
              id="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none transition-all"
              placeholder="••••••••"
            />
          </div>

          <button
            type="submit"
            disabled={loading}  // Desabilita enquanto carrega
            className="w-full bg-primary-600 text-white py-2 rounded-lg font-semibold hover:bg-primary-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {/* Renderização condicional: muda texto baseado no estado */}
            {loading ? 'Entrando...' : 'Entrar'}
          </button>
        </form>

        <p className="mt-6 text-center text-sm text-gray-600">
          Não tem conta?{' '}
          <Link href="/auth/register" className="text-primary-600 hover:underline">
            Cadastre-se
          </Link>
        </p>
      </div>
    </div>
  )
}
