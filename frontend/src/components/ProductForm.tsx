'use client'

import { useState } from 'react'
import type { CreateProductRequest, Product } from '@/types'

// Props do componente
// initialData = dados para edição (opcional)
// onSubmit = função chamada ao salvar
// submitLabel = texto do botão
interface ProductFormProps {
  initialData?: Product
  onSubmit: (data: CreateProductRequest) => Promise<void>
  submitLabel: string
}

export function ProductForm({ initialData, onSubmit, submitLabel }: ProductFormProps) {
  // Estados do formulário - inicializa com dados existentes ou valores padrão
  const [name, setName] = useState(initialData?.name || '')
  const [price, setPrice] = useState(initialData?.price?.toString() || '')
  const [stock, setStock] = useState(initialData?.stock?.toString() || '')
  const [category, setCategory] = useState(initialData?.category || '')
  const [description, setDescription] = useState(initialData?.description || '')
  // Tags como string separada por vírgulas (mais fácil de editar)
  const [tagsInput, setTagsInput] = useState(initialData?.tags?.join(', ') || '')

  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const categories = ['Electronics', 'Clothing', 'Books', 'Home', 'Sports', 'Food', 'Beauty', 'Toys']

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')

    // Validações básicas
    if (!name.trim()) {
      setError('Nome é obrigatório')
      return
    }

    const priceNum = parseFloat(price)
    if (isNaN(priceNum) || priceNum <= 0) {
      setError('Preço deve ser maior que zero')
      return
    }

    const stockNum = parseInt(stock)
    if (isNaN(stockNum) || stockNum < 0) {
      setError('Estoque deve ser zero ou maior')
      return
    }

    if (!category) {
      setError('Selecione uma categoria')
      return
    }

    // Converte string de tags para array
    // "tag1, tag2, tag3" -> ["tag1", "tag2", "tag3"]
    const tags = tagsInput
      .split(',')
      .map(tag => tag.trim())
      .filter(tag => tag.length > 0)

    if (tags.length === 0) {
      setError('Adicione pelo menos uma tag')
      return
    }

    setLoading(true)

    try {
      await onSubmit({
        name: name.trim(),
        price: priceNum,
        stock: stockNum,
        category,
        description: description.trim(),
        tags,
      })
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao salvar produto')
      setLoading(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      {error && (
        <div className="bg-red-50 text-red-600 p-3 rounded-lg text-sm">
          {error}
        </div>
      )}

      {/* Nome */}
      <div>
        <label htmlFor="name" className="block text-sm font-medium text-gray-700 mb-1">
          Nome do produto *
        </label>
        <input
          type="text"
          id="name"
          value={name}
          onChange={(e) => setName(e.target.value)}
          required
          maxLength={100}
          className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none"
          placeholder="Ex: Notebook Dell Inspiron 15"
        />
      </div>

      {/* Preço e Estoque em linha */}
      <div className="grid grid-cols-2 gap-4">
        <div>
          <label htmlFor="price" className="block text-sm font-medium text-gray-700 mb-1">
            Preço (R$) *
          </label>
          <input
            type="number"
            id="price"
            value={price}
            onChange={(e) => setPrice(e.target.value)}
            required
            min="0.01"
            step="0.01"
            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none"
            placeholder="0,00"
          />
        </div>
        <div>
          <label htmlFor="stock" className="block text-sm font-medium text-gray-700 mb-1">
            Estoque *
          </label>
          <input
            type="number"
            id="stock"
            value={stock}
            onChange={(e) => setStock(e.target.value)}
            required
            min="0"
            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none"
            placeholder="0"
          />
        </div>
      </div>

      {/* Categoria */}
      <div>
        <label htmlFor="category" className="block text-sm font-medium text-gray-700 mb-1">
          Categoria *
        </label>
        <select
          id="category"
          value={category}
          onChange={(e) => setCategory(e.target.value)}
          required
          className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none"
        >
          <option value="">Selecione uma categoria</option>
          {categories.map((cat) => (
            <option key={cat} value={cat}>{cat}</option>
          ))}
        </select>
      </div>

      {/* Tags */}
      <div>
        <label htmlFor="tags" className="block text-sm font-medium text-gray-700 mb-1">
          Tags * <span className="text-gray-400 font-normal">(separadas por vírgula)</span>
        </label>
        <input
          type="text"
          id="tags"
          value={tagsInput}
          onChange={(e) => setTagsInput(e.target.value)}
          className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none"
          placeholder="Ex: notebook, dell, laptop, 2024"
        />
      </div>

      {/* Descrição */}
      <div>
        <label htmlFor="description" className="block text-sm font-medium text-gray-700 mb-1">
          Descrição
        </label>
        <textarea
          id="description"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          rows={4}
          maxLength={1000}
          className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none resize-none"
          placeholder="Descreva seu produto..."
        />
        <p className="text-xs text-gray-400 mt-1">{description.length}/1000 caracteres</p>
      </div>

      {/* Botão de submit */}
      <button
        type="submit"
        disabled={loading}
        className="w-full bg-primary-600 text-white py-3 rounded-lg font-semibold hover:bg-primary-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
      >
        {loading ? 'Salvando...' : submitLabel}
      </button>
    </form>
  )
}
