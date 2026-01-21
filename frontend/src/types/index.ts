// User types
export interface User {
  id: string
  name: string
  email: string
  phone?: string
  role: string
  active: boolean
  emailVerified: boolean
}

export interface Session {
  id: string
  userId: string
  expiresAt: string
}

export interface AuthResponse {
  user: User
  session?: Session
}

// Product types
export interface Product {
  id: string
  name: string
  price: number
  stock: number
  tags: string[]
  category: string
  description: string
  userId: string
  createdAt: string
  updatedAt: string
}

export interface ProductsResponse {
  content: Product[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
  empty: boolean
}

export interface CreateProductRequest {
  name: string
  price: number
  stock: number
  tags: string[]
  category: string
  description: string
}

export interface UpdateProductRequest extends CreateProductRequest {}

// Search types
export interface SearchProductsResponse {
  products: Product[]
  page: number
  size: number
  total: number
  totalPages: number
}

// Address types
export interface Address {
  id: string
  user_id: string
  name: string
  number: number
  street: string
  state: string
  zip_code: string
  country: string
  complement?: string
  active: boolean
  created_at: string
  updated_at: string
}

export interface CreateAddressRequest {
  name: string
  number: number
  street: string
  state: string
  zip_code: string
  country: string
  complement?: string
}

export interface UpdateAddressRequest extends CreateAddressRequest {}
