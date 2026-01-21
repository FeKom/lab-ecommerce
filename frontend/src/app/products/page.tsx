'use client'

import { useState, useEffect } from 'react'
import { searchApi } from '@/lib/api'
import { ProductCard } from '@/components/ProductCard'
import type { Product } from '@/types'

export default function ProductsPage() {
  // Estados para os produtos e controle da UI
  const [products, setProducts] = useState<Product[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  // Estados para busca e filtros
  const [searchQuery, setSearchQuery] = useState('')
  const [selectedCategory, setSelectedCategory] = useState('')
  const [minPrice, setMinPrice] = useState('')
  const [maxPrice, setMaxPrice] = useState('')

  // Categorias disponíveis (poderia vir de uma API)
  const categories = ['Electronics', 'Clothing', 'Books', 'Home', 'Sports', 'Food', 'Beauty', 'Toys']

  // Carrega produtos ao montar o componente
  useEffect(() => {
    loadProducts()
  }, [])  // [] = executa apenas uma vez

  // Função para carregar todos os produtos
  const loadProducts = async () => {
    setLoading(true)
    setError('')
    try {
      // Usa o Search Service (otimizado para leitura)
      const data = await searchApi.list(0, 50)
      setProducts(data.products || [])
    } catch (err) {
      setError('Erro ao carregar produtos')
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  // Função para buscar por nome
  const handleSearch = async () => {
    if (!searchQuery.trim()) {
      loadProducts()
      return
    }

    setLoading(true)
    setError('')
    try {
      const data = await searchApi.searchByName(searchQuery)
      setProducts(data)
    } catch (err) {
      setError('Erro na busca')
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  // Função para filtrar por categoria
  const handleCategoryFilter = async (category: string) => {
    setSelectedCategory(category)

    if (!category) {
      loadProducts()
      return
    }

    setLoading(true)
    setError('')
    try {
      const data = await searchApi.searchByCategory(category)
      setProducts(data)
    } catch (err) {
      setError('Erro ao filtrar por categoria')
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  // Função para filtrar por faixa de preço
  const handlePriceFilter = async () => {
    const min = parseFloat(minPrice) || 0
    const max = parseFloat(maxPrice) || 999999

    if (min > max) {
      setError('Preço mínimo não pode ser maior que o máximo')
      return
    }

    setLoading(true)
    setError('')
    try {
      const data = await searchApi.searchByPriceRange(min, max)
      setProducts(data)
    } catch (err) {
      setError('Erro ao filtrar por preço')
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  // Limpa todos os filtros
  const clearFilters = () => {
    setSearchQuery('')
    setSelectedCategory('')
    setMinPrice('')
    setMaxPrice('')
    loadProducts()
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-800">Produtos</h1>
        <span className="text-sm text-gray-500">
          {products.length} produto(s) encontrado(s)
        </span>
      </div>

      {/* Filtros */}
      <div className="bg-white rounded-xl shadow-sm p-6 space-y-4">
        {/* Busca por nome */}
        <div className="flex gap-2">
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            // onKeyDown = evento ao pressionar tecla
            onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
            placeholder="Buscar produtos..."
            className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none"
          />
          <button
            onClick={handleSearch}
            className="px-6 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors"
          >
            Buscar
          </button>
        </div>

        {/* Filtros em linha */}
        <div className="flex flex-wrap gap-4 items-end">
          {/* Filtro por categoria */}
          <div className="flex-1 min-w-[200px]">
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Categoria
            </label>
            <select
              value={selectedCategory}
              onChange={(e) => handleCategoryFilter(e.target.value)}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none"
            >
              <option value="">Todas as categorias</option>
              {categories.map((cat) => (
                <option key={cat} value={cat}>{cat}</option>
              ))}
            </select>
          </div>

          {/* Filtro por preço */}
          <div className="flex gap-2 items-end">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Preço mín.
              </label>
              <input
                type="number"
                value={minPrice}
                onChange={(e) => setMinPrice(e.target.value)}
                placeholder="0"
                className="w-28 px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Preço máx.
              </label>
              <input
                type="number"
                value={maxPrice}
                onChange={(e) => setMaxPrice(e.target.value)}
                placeholder="10000"
                className="w-28 px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none"
              />
            </div>
            <button
              onClick={handlePriceFilter}
              className="px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition-colors"
            >
              Filtrar
            </button>
          </div>

          {/* Botão limpar */}
          <button
            onClick={clearFilters}
            className="px-4 py-2 text-gray-500 hover:text-gray-700 transition-colors"
          >
            Limpar filtros
          </button>
        </div>
      </div>

      {/* Mensagem de erro */}
      {error && (
        <div className="bg-red-50 text-red-600 p-4 rounded-lg">
          {error}
        </div>
      )}

      {/* Loading state */}
      {loading ? (
        <div className="flex justify-center py-12">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
        </div>
      ) : products.length === 0 ? (
        // Empty state
        <div className="text-center py-12 text-gray-500">
          <p className="text-lg">Nenhum produto encontrado</p>
          <p className="text-sm mt-2">Tente ajustar os filtros ou faça uma nova busca</p>
        </div>
      ) : (
        // Grid de produtos
        // grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4
        // = responsivo: 1 coluna no mobile, 2 em tablet, 3 em desktop, 4 em telas grandes
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          {products.map((product) => (
            <ProductCard key={product.id} product={product} />
          ))}
        </div>
      )}
    </div>
  )
}
