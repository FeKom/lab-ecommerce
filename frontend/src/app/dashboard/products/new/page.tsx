'use client'

import { useEffect } from 'react'
import { useRouter } from 'next/navigation'
import Link from 'next/link'
import { useAuth } from '@/lib/auth-context'
import { productsApi } from '@/lib/api'
import { ProductForm } from '@/components/ProductForm'
import type { CreateProductRequest } from '@/types'

export default function NewProductPage() {
  const router = useRouter()
  const { user, loading } = useAuth()

  // Redireciona se não autenticado
  useEffect(() => {
    if (!loading && !user) {
      router.push('/auth/login')
    }
  }, [user, loading, router])

  // Função chamada ao submeter o formulário
  const handleSubmit = async (data: CreateProductRequest) => {
    // Chama a API do Catalog Service
    await productsApi.create(data)
    // Redireciona para o dashboard após criar
    router.push('/dashboard')
  }

  if (loading || !user) {
    return (
      <div className="flex justify-center py-12">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
      </div>
    )
  }

  return (
    <div className="max-w-2xl mx-auto">
      {/* Breadcrumb */}
      <nav className="text-sm text-gray-500 mb-6">
        <Link href="/dashboard" className="hover:text-primary-600">
          Dashboard
        </Link>
        <span className="mx-2">/</span>
        <span className="text-gray-800">Novo Produto</span>
      </nav>

      <div className="bg-white rounded-xl shadow-sm p-8">
        <h1 className="text-2xl font-bold text-gray-800 mb-6">
          Cadastrar Novo Produto
        </h1>

        <ProductForm
          onSubmit={handleSubmit}
          submitLabel="Cadastrar Produto"
        />
      </div>
    </div>
  )
}
