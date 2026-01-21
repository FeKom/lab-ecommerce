'use client'

// 'use client' = indica que este componente roda no BROWSER (não no servidor)
// Necessário quando usamos hooks (useState, useEffect) ou eventos (onClick)

import Link from 'next/link'  // Componente do Next.js para navegação SPA (sem reload)
import { useAuth } from '@/lib/auth-context'

export function Navbar() {
  // useAuth() = hook customizado que criamos
  // Pega os dados do AuthContext (user, loading, signOut)
  const { user, loading, signOut } = useAuth()

  return (
    // Tailwind CSS: classes utilitárias para estilização
    // bg-white = background branco
    // shadow-sm = sombra pequena
    // sticky top-0 = fixa no topo ao scrollar
    <nav className="bg-white shadow-sm sticky top-0 z-50">
      <div className="container mx-auto px-4">
        {/* flex = display flex, items-center = alinha verticalmente */}
        {/* justify-between = espaço entre elementos */}
        <div className="flex items-center justify-between h-16">

          {/* Logo / Nome do site */}
          <Link href="/" className="text-xl font-bold text-primary-600">
            E-Commerce Lab
          </Link>

          {/* Links de navegação */}
          <div className="flex items-center gap-6">
            <Link
              href="/products"
              className="text-gray-600 hover:text-primary-600 transition-colors"
            >
              Produtos
            </Link>

            {/* Renderização condicional */}
            {/* Se loading = true, mostra "..." */}
            {/* Se user existe, mostra menu do usuário */}
            {/* Se não, mostra botões de login/cadastro */}
            {loading ? (
              <span className="text-gray-400">...</span>
            ) : user ? (
              // Fragment <> </> = agrupa elementos sem criar div extra
              <>
                <Link
                  href="/dashboard"
                  className="text-gray-600 hover:text-primary-600 transition-colors"
                >
                  Meus Produtos
                </Link>
                <div className="flex items-center gap-4">
                  <span className="text-sm text-gray-500">
                    Olá, {user.name.split(' ')[0]}
                  </span>
                  <button
                    onClick={() => signOut()}
                    className="text-sm text-red-600 hover:text-red-700"
                  >
                    Sair
                  </button>
                </div>
              </>
            ) : (
              <div className="flex items-center gap-4">
                <Link
                  href="/auth/login"
                  className="text-gray-600 hover:text-primary-600 transition-colors"
                >
                  Entrar
                </Link>
                <Link
                  href="/auth/register"
                  className="bg-primary-600 text-white px-4 py-2 rounded-lg hover:bg-primary-700 transition-colors"
                >
                  Cadastrar
                </Link>
              </div>
            )}
          </div>
        </div>
      </div>
    </nav>
  )
}
