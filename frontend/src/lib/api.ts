import type {
  AuthResponse,
  User,
  Product,
  ProductsResponse,
  CreateProductRequest,
  UpdateProductRequest,
  SearchProductsResponse,
  Address,
  CreateAddressRequest,
  UpdateAddressRequest,
} from '@/types'

const API_BASE = ''

// Auth API (User Service - port 8085)
export const authApi = {
  async signUp(data: { name: string; email: string; password: string; phone?: string }): Promise<AuthResponse> {
    const res = await fetch(`${API_BASE}/api/auth/sign-up/email`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify(data),
    })
    if (!res.ok) {
      const error = await res.json().catch(() => ({ message: 'Erro ao criar conta' }))
      throw new Error(error.message || 'Erro ao criar conta')
    }
    return res.json()
  },

  async signIn(data: { email: string; password: string }): Promise<AuthResponse> {
    const res = await fetch(`${API_BASE}/api/auth/sign-in/email`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify(data),
    })
    if (!res.ok) {
      const error = await res.json().catch(() => ({ message: 'Email ou senha inválidos' }))
      throw new Error(error.message || 'Email ou senha inválidos')
    }
    return res.json()
  },

  async signOut(): Promise<void> {
    await fetch(`${API_BASE}/api/auth/sign-out`, {
      method: 'POST',
      credentials: 'include',
    })
  },

  async getMe(): Promise<{ user: User } | null> {
    const res = await fetch(`${API_BASE}/api/users/me`, {
      credentials: 'include',
    })
    if (!res.ok) return null
    return res.json()
  },
}

// Products API (Catalog Service - port 8080)
export const productsApi = {
  async list(page = 0, size = 20, sortBy = 'createdAt', sortDir = 'desc'): Promise<ProductsResponse> {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
      sortBy,
      sortDir,
    })
    const res = await fetch(`${API_BASE}/api/products?${params}`)
    if (!res.ok) throw new Error('Erro ao carregar produtos')
    return res.json()
  },

  async getById(id: string): Promise<Product> {
    const res = await fetch(`${API_BASE}/api/products/${id}`)
    if (!res.ok) throw new Error('Produto não encontrado')
    return res.json()
  },

  async create(data: CreateProductRequest): Promise<Product> {
    const res = await fetch(`${API_BASE}/api/products`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify(data),
    })
    if (!res.ok) {
      const error = await res.json().catch(() => ({ message: 'Erro ao criar produto' }))
      throw new Error(error.message || 'Erro ao criar produto')
    }
    return res.json()
  },

  async update(id: string, data: UpdateProductRequest): Promise<Product> {
    const res = await fetch(`${API_BASE}/api/products/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify(data),
    })
    if (!res.ok) {
      const error = await res.json().catch(() => ({ message: 'Erro ao atualizar produto' }))
      throw new Error(error.message || 'Erro ao atualizar produto')
    }
    return res.json()
  },

  async delete(id: string): Promise<void> {
    const res = await fetch(`${API_BASE}/api/products/${id}`, {
      method: 'DELETE',
      credentials: 'include',
    })
    if (!res.ok) {
      const error = await res.json().catch(() => ({ message: 'Erro ao deletar produto' }))
      throw new Error(error.message || 'Erro ao deletar produto')
    }
  },
}

// Search API (Search Service - port 8081)
export const searchApi = {
  async list(page = 0, size = 20): Promise<SearchProductsResponse> {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
    })
    const res = await fetch(`${API_BASE}/api/search/products?${params}`)
    if (!res.ok) throw new Error('Erro ao carregar produtos')
    return res.json()
  },

  async getById(id: string): Promise<Product> {
    const res = await fetch(`${API_BASE}/api/search/products/${id}`)
    if (!res.ok) throw new Error('Produto não encontrado')
    return res.json()
  },

  async searchByName(query: string): Promise<Product[]> {
    const params = new URLSearchParams({ q: query })
    const res = await fetch(`${API_BASE}/api/search/products/search?${params}`)
    if (!res.ok) throw new Error('Erro na busca')
    return res.json()
  },

  async searchByCategory(category: string): Promise<Product[]> {
    const params = new URLSearchParams({ category })
    const res = await fetch(`${API_BASE}/api/search/products/category?${params}`)
    if (!res.ok) throw new Error('Erro na busca por categoria')
    return res.json()
  },

  async searchByPriceRange(minPrice: number, maxPrice: number): Promise<Product[]> {
    const params = new URLSearchParams({
      minPrice: minPrice.toString(),
      maxPrice: maxPrice.toString(),
    })
    const res = await fetch(`${API_BASE}/api/search/products/price-range?${params}`)
    if (!res.ok) throw new Error('Erro na busca por preço')
    return res.json()
  },
}

// Addresses API (User Service - port 8085)
export const addressesApi = {
  async list(): Promise<Address[]> {
    const res = await fetch(`${API_BASE}/api/addresses`, {
      credentials: 'include',
    })
    if (!res.ok) throw new Error('Erro ao carregar endereços')
    return res.json()
  },

  async getById(id: string): Promise<Address> {
    const res = await fetch(`${API_BASE}/api/addresses/${id}`, {
      credentials: 'include',
    })
    if (!res.ok) throw new Error('Endereço não encontrado')
    return res.json()
  },

  async create(data: CreateAddressRequest): Promise<Address> {
    const res = await fetch(`${API_BASE}/api/addresses`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify(data),
    })
    if (!res.ok) {
      const error = await res.json().catch(() => ({ message: 'Erro ao criar endereço' }))
      throw new Error(error.message || 'Erro ao criar endereço')
    }
    return res.json()
  },

  async update(id: string, data: UpdateAddressRequest): Promise<Address> {
    const res = await fetch(`${API_BASE}/api/addresses/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify(data),
    })
    if (!res.ok) {
      const error = await res.json().catch(() => ({ message: 'Erro ao atualizar endereço' }))
      throw new Error(error.message || 'Erro ao atualizar endereço')
    }
    return res.json()
  },

  async delete(id: string): Promise<void> {
    const res = await fetch(`${API_BASE}/api/addresses/${id}`, {
      method: 'DELETE',
      credentials: 'include',
    })
    if (!res.ok) {
      const error = await res.json().catch(() => ({ message: 'Erro ao deletar endereço' }))
      throw new Error(error.message || 'Erro ao deletar endereço')
    }
  },
}
