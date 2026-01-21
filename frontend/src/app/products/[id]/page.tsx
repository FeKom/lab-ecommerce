'use client'

// [id] na pasta = rota dinâmica
// Exemplo: /products/123 -> id = "123"

import { useState, useEffect } from 'react'
import { useParams, useRouter } from 'next/navigation'
import Link from 'next/link'
import { searchApi, productsApi } from '@/lib/api'
import { useAuth } from '@/lib/auth-context'
import type { Product } from '@/types'

export default function ProductDetailPage() {
  // useParams = hook do Next.js para pegar parâmetros da URL
  // No caso de /products/[id], retorna { id: "valor" }
  const params = useParams()
  const router = useRouter()
  const { user } = useAuth()

  const [product, setProduct] = useState<Product | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [deleting, setDeleting] = useState(false)

  // Carrega o produto quando o componente monta ou quando o ID muda
  useEffect(() => {
    if (params.id) {
      loadProduct(params.id as string)
    }
  }, [params.id])  // Dependência: re-executa quando params.id mudar

  const loadProduct = async (id: string) => {
    setLoading(true)
    setError('')
    try {
      // Usa o Search Service para buscar o produto
      const data = await searchApi.getById(id)
      setProduct(data)
    } catch (err) {
      setError('Produto não encontrado')
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  // Função para deletar produto (apenas o dono pode deletar)
  const handleDelete = async () => {
    if (!product) return

    // confirm = dialog nativo do navegador
    if (!confirm('Tem certeza que deseja excluir este produto?')) {
      return
    }

    setDeleting(true)
    try {
      // Usa o Catalog Service para deletar
      await productsApi.delete(product.id)
      router.push('/products')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao excluir produto')
      setDeleting(false)
    }
  }

  // Formata preço
  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL',
    }).format(price)
  }

  // Formata data
  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: 'long',
      year: 'numeric',
    })
  }

  // Loading state
  if (loading) {
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
        <Link href="/products" className="text-primary-600 hover:underline mt-4 inline-block">
          Voltar para produtos
        </Link>
      </div>
    )
  }

  // Verifica se o usuário logado é o dono do produto
  const isOwner = user && user.id === product.userId

  return (
    <div className="max-w-4xl mx-auto">
      {/* Breadcrumb (navegação) */}
      <nav className="text-sm text-gray-500 mb-6">
        <Link href="/products" className="hover:text-primary-600">
          Produtos
        </Link>
        <span className="mx-2">/</span>
        <span className="text-gray-800">{product.name}</span>
      </nav>

      <div className="bg-white rounded-xl shadow-sm overflow-hidden">
        <div className="md:flex">
          {/* Imagem placeholder */}
          <div className="md:w-1/2 h-64 md:h-auto bg-gradient-to-br from-gray-100 to-gray-200 flex items-center justify-center">
            <span className="text-8xl">
              {getCategoryEmoji(product.category)}
            </span>
          </div>

          {/* Informações do produto */}
          <div className="md:w-1/2 p-8">
            <div className="flex items-start justify-between">
              <div>
                <span className="text-sm text-primary-600 font-medium uppercase tracking-wide">
                  {product.category}
                </span>
                <h1 className="text-2xl font-bold text-gray-800 mt-1">
                  {product.name}
                </h1>
              </div>

              {/* Botões de ação (apenas para o dono) */}
              {isOwner && (
                <div className="flex gap-2">
                  <Link
                    href={`/dashboard/products/${product.id}/edit`}
                    className="px-3 py-1 text-sm bg-gray-100 text-gray-700 rounded hover:bg-gray-200 transition-colors"
                  >
                    Editar
                  </Link>
                  <button
                    onClick={handleDelete}
                    disabled={deleting}
                    className="px-3 py-1 text-sm bg-red-100 text-red-700 rounded hover:bg-red-200 transition-colors disabled:opacity-50"
                  >
                    {deleting ? 'Excluindo...' : 'Excluir'}
                  </button>
                </div>
              )}
            </div>

            {/* Preço */}
            <p className="text-3xl font-bold text-gray-800 mt-6">
              {formatPrice(product.price)}
            </p>

            {/* Estoque */}
            <div className="mt-4">
              <span className={`inline-block px-3 py-1 rounded-full text-sm font-medium ${
                product.stock > 0
                  ? 'bg-green-100 text-green-700'
                  : 'bg-red-100 text-red-700'
              }`}>
                {product.stock > 0 ? `${product.stock} unidades em estoque` : 'Produto esgotado'}
              </span>
            </div>

            {/* Descrição */}
            <div className="mt-6">
              <h2 className="text-sm font-medium text-gray-700 mb-2">Descrição</h2>
              <p className="text-gray-600">{product.description}</p>
            </div>

            {/* Tags */}
            {product.tags.length > 0 && (
              <div className="mt-6">
                <h2 className="text-sm font-medium text-gray-700 mb-2">Tags</h2>
                <div className="flex flex-wrap gap-2">
                  {product.tags.map((tag) => (
                    <span
                      key={tag}
                      className="px-3 py-1 bg-gray-100 text-gray-600 rounded-full text-sm"
                    >
                      {tag}
                    </span>
                  ))}
                </div>
              </div>
            )}

            {/* Informações adicionais */}
            <div className="mt-8 pt-6 border-t border-gray-100 text-sm text-gray-500">
              <p>Criado em: {formatDate(product.createdAt)}</p>
              {product.updatedAt !== product.createdAt && (
                <p>Atualizado em: {formatDate(product.updatedAt)}</p>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Botão voltar */}
      <div className="mt-6">
        <Link
          href="/products"
          className="text-primary-600 hover:underline inline-flex items-center gap-1"
        >
          ← Voltar para produtos
        </Link>
      </div>
    </div>
  )
}

function getCategoryEmoji(category: string): string {
  const emojis: Record<string, string> = {
    Electronics: '💻',
    Clothing: '👕',
    Books: '📚',
    Home: '🏠',
    Sports: '⚽',
    Food: '🍕',
    Beauty: '💄',
    Toys: '🧸',
  }
  return emojis[category] || '📦'
}
