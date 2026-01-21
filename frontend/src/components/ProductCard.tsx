import Link from 'next/link'
import type { Product } from '@/types'

// Interface para props do componente
// Definir tipos ajuda a evitar bugs e melhora o autocomplete
interface ProductCardProps {
  product: Product
}

// Componente de card de produto reutilizável
export function ProductCard({ product }: ProductCardProps) {
  // Formata preço em Real brasileiro
  const formattedPrice = new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL',
  }).format(product.price)

  return (
    <Link href={`/products/${product.id}`}>
      {/* group = permite aplicar estilos nos filhos ao hover do pai */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden hover:shadow-md transition-shadow group">
        {/* Placeholder de imagem */}
        <div className="h-48 bg-gradient-to-br from-gray-100 to-gray-200 flex items-center justify-center">
          <span className="text-4xl text-gray-400">
            {/* Emoji baseado na categoria */}
            {getCategoryEmoji(product.category)}
          </span>
        </div>

        <div className="p-4">
          {/* Categoria */}
          <span className="text-xs text-primary-600 font-medium uppercase tracking-wide">
            {product.category}
          </span>

          {/* Nome do produto */}
          <h3 className="font-semibold text-gray-800 mt-1 group-hover:text-primary-600 transition-colors line-clamp-2">
            {product.name}
          </h3>

          {/* Descrição truncada */}
          <p className="text-sm text-gray-500 mt-2 line-clamp-2">
            {product.description}
          </p>

          {/* Preço e estoque */}
          <div className="flex items-center justify-between mt-4">
            <span className="text-lg font-bold text-gray-800">
              {formattedPrice}
            </span>
            <span className={`text-xs px-2 py-1 rounded ${
              product.stock > 0
                ? 'bg-green-100 text-green-700'
                : 'bg-red-100 text-red-700'
            }`}>
              {product.stock > 0 ? `${product.stock} em estoque` : 'Esgotado'}
            </span>
          </div>

          {/* Tags */}
          {product.tags.length > 0 && (
            <div className="flex flex-wrap gap-1 mt-3">
              {/* slice(0, 3) = pega apenas as 3 primeiras tags */}
              {product.tags.slice(0, 3).map((tag) => (
                <span
                  key={tag}
                  className="text-xs bg-gray-100 text-gray-600 px-2 py-0.5 rounded"
                >
                  {tag}
                </span>
              ))}
            </div>
          )}
        </div>
      </div>
    </Link>
  )
}

// Função auxiliar para retornar emoji baseado na categoria
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
