'use client'

import { useState, useEffect } from 'react'
import { useParams, useRouter } from 'next/navigation'
import Link from 'next/link'
import { useAuth } from '@/lib/auth-context'
import { productsApi } from '@/lib/api'
import { ProductForm } from '@/components/ProductForm'
import type { Product, UpdateProductRequest } from '@/types'

export default function EditProductPage() {
  const params = useParams()
  const router = useRouter()
  const { user, loading: authLoading } = useAuth()

  const [product, setProduct] = useState<Product | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  // Redireciona se não autenticado
  useEffect(() => {
    if (!authLoading && !user) {
      router.push('/auth/login')
    }
  }, [user, authLoading, router])

  // Carrega o produto
  useEffect(() => {
    if (params.id && user) {
      loadProduct(params.id as string)
    }
  }, [params.id, user])

  const loadProduct = async (id: string) => {
    setLoading(true)
    try {
      const data = await productsApi.getById(id)

      // Verifica se o usuário é o dono
      if (data.userId !== user?.id) {
        setError('Você não tem permissão para editar este produto')
        setProduct(null)
      } else {
        setProduct(data)
      }
    } catch (err) {
      setError('Produto não encontrado')
    } finally {
      setLoading(false)
    }
  }

  const handleSubmit = async (data: UpdateProductRequest) => {
    if (!product) return

    await productsApi.update(product.id, data)
    router.push('/dashboard')
  }

  // Loading states
  if (authLoading || loading) {
    return (
      <div className="flex justify-center py-12">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
      </div>
    )
  }

  // Error state
  if (error || !product) {
    return (
      <div className="text-center py-12">
        <p className="text-red-600 text-lg">{error || 'Produto não encontrado'}</p>
        <Link href="/dashboard" className="text-primary-600 hover:underline mt-4 inline-block">
          Voltar para o dashboard
        </Link>
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
        <span className="text-gray-800">Editar Produto</span>
      </nav>

      <div className="bg-white rounded-xl shadow-sm p-8">
        <h1 className="text-2xl font-bold text-gray-800 mb-6">
          Editar Produto
        </h1>

        {/* Passa os dados existentes para o formulário */}
        <ProductForm
          initialData={product}
          onSubmit={handleSubmit}
          submitLabel="Salvar Alterações"
        />
      </div>
    </div>
  )
}
