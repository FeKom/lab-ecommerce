'use client'

// Este arquivo separa a lógica de "client components" do layout
// O layout.tsx pode ser Server Component (melhor para SEO)
// Os providers precisam ser Client Components (usam hooks)

import { AuthProvider } from '@/lib/auth-context'
import { ReactNode } from 'react'

export function Providers({ children }: { children: ReactNode }) {
  return (
    // AuthProvider envolve toda a aplicação
    // Assim, qualquer componente pode acessar useAuth()
    <AuthProvider>
      {children}
    </AuthProvider>
  )
}
