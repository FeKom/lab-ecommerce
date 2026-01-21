'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import Link from 'next/link'
import { useAuth } from '@/lib/auth-context'
import { productsApi } from '@/lib/api'
import { ProductCard } from '@/components/ProductCard'
import type { Product } from '@/types'

export default function DashboardPage() {
  const router = useRouter()
  const { user, loading: authLoading } = useAuth()

  const [products, setProducts] = useState<Product[]>([])
  const [loading, setLoading] = useState(true)

  // Redireciona para login se não estiver autenticado
  useEffect(() => {
    if (!authLoading && !user) {
      router.push('/auth/login')
    }
  }, [user, authLoading, router])

  // Carrega produtos do usuário
  useEffect(() => {
    if (user) {
      loadUserProducts()
    }
  }, [user])

  const loadUserProducts = async () => {
    setLoading(true)
    try {
      // Carrega todos os produtos e filtra pelo userId
      // Idealmente teria um endpoint GET /api/products?userId=xxx
      const data = await productsApi.list(0, 100)
      const userProducts = data.content.filter(p => p.userId === user?.id)
      setProducts(userProducts)
    } catch (err) {
      console.error('Erro ao carregar produtos:', err)
    } finally {
      setLoading(false)
    }
  }

  // Mostra loading enquanto verifica autenticação
  if (authLoading) {
    return (
      <div className="flex justify-center py-12">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
      </div>
    )
  }

  // Se não tem usuário (e não está loading), não renderiza nada
  // O useEffect vai redirecionar para login
  if (!user) {
    return null
  }

  return (
    <div className="space-y-8">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-800">Meu Dashboard</h1>
          <p className="text-gray-500">Olá, {user.name}! Gerencie seus produtos e endereços.</p>
        </div>
      </div>

      {/* Cards de navegação */}
      <div className="grid md:grid-cols-2 gap-6">
        <Link href="/dashboard/products/new">
          <div className="bg-primary-600 text-white p-6 rounded-xl hover:bg-primary-700 transition-colors cursor-pointer">
            <h2 className="text-xl font-semibold mb-2">+ Novo Produto</h2>
            <p className="text-primary-100">Cadastre um novo produto para venda</p>
          </div>
        </Link>

        <Link href="/dashboard/addresses">
          <div className="bg-white border border-gray-200 p-6 rounded-xl hover:shadow-md transition-shadow cursor-pointer">
            <h2 className="text-xl font-semibold text-gray-800 mb-2">Meus Endereços</h2>
            <p className="text-gray-500">Gerencie seus endereços de entrega</p>
          </div>
        </Link>
      </div>

      {/* Lista de produtos do usuário */}
      <section>
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-xl font-semibold text-gray-800">Meus Produtos</h2>
          <span className="text-sm text-gray-500">{products.length} produto(s)</span>
        </div>

        {loading ? (
          <div className="flex justify-center py-8">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
          </div>
        ) : products.length === 0 ? (
          <div className="bg-gray-50 rounded-xl p-8 text-center">
            <p className="text-gray-500 mb-4">Você ainda não tem produtos cadastrados</p>
            <Link
              href="/dashboard/products/new"
              className="inline-block bg-primary-600 text-white px-6 py-2 rounded-lg hover:bg-primary-700 transition-colors"
            >
              Cadastrar primeiro produto
            </Link>
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {products.map((product) => (
              <ProductCard key={product.id} product={product} />
            ))}
          </div>
        )}
      </section>
    </div>
  )
}
