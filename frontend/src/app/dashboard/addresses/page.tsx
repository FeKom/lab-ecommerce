'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import Link from 'next/link'
import { useAuth } from '@/lib/auth-context'
import { addressesApi } from '@/lib/api'
import type { Address, CreateAddressRequest } from '@/types'

export default function AddressesPage() {
  const router = useRouter()
  const { user, loading: authLoading } = useAuth()

  const [addresses, setAddresses] = useState<Address[]>([])
  const [loading, setLoading] = useState(true)
  const [showForm, setShowForm] = useState(false)
  const [editingId, setEditingId] = useState<string | null>(null)

  // Redireciona se não autenticado
  useEffect(() => {
    if (!authLoading && !user) {
      router.push('/auth/login')
    }
  }, [user, authLoading, router])

  // Carrega endereços
  useEffect(() => {
    if (user) {
      loadAddresses()
    }
  }, [user])

  const loadAddresses = async () => {
    setLoading(true)
    try {
      const data = await addressesApi.list()
      setAddresses(data)
    } catch (err) {
      console.error('Erro ao carregar endereços:', err)
    } finally {
      setLoading(false)
    }
  }

  const handleDelete = async (id: string) => {
    if (!confirm('Deseja excluir este endereço?')) return

    try {
      await addressesApi.delete(id)
      // Atualiza a lista localmente (evita nova chamada à API)
      setAddresses(addresses.filter(a => a.id !== id))
    } catch (err) {
      alert('Erro ao excluir endereço')
    }
  }

  if (authLoading || !user) {
    return (
      <div className="flex justify-center py-12">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
      </div>
    )
  }

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <nav className="text-sm text-gray-500 mb-2">
            <Link href="/dashboard" className="hover:text-primary-600">
              Dashboard
            </Link>
            <span className="mx-2">/</span>
            <span className="text-gray-800">Endereços</span>
          </nav>
          <h1 className="text-2xl font-bold text-gray-800">Meus Endereços</h1>
        </div>
        <button
          onClick={() => { setShowForm(true); setEditingId(null); }}
          className="bg-primary-600 text-white px-4 py-2 rounded-lg hover:bg-primary-700 transition-colors"
        >
          + Novo Endereço
        </button>
      </div>

      {/* Formulário (mostrado condicionalmente) */}
      {showForm && (
        <AddressForm
          editingAddress={editingId ? addresses.find(a => a.id === editingId) : undefined}
          onSave={async () => {
            await loadAddresses()
            setShowForm(false)
            setEditingId(null)
          }}
          onCancel={() => {
            setShowForm(false)
            setEditingId(null)
          }}
        />
      )}

      {/* Lista de endereços */}
      {loading ? (
        <div className="flex justify-center py-8">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
        </div>
      ) : addresses.length === 0 ? (
        <div className="bg-gray-50 rounded-xl p-8 text-center">
          <p className="text-gray-500">Você ainda não tem endereços cadastrados</p>
        </div>
      ) : (
        <div className="grid gap-4">
          {addresses.map((address) => (
            <div key={address.id} className="bg-white rounded-xl shadow-sm p-6">
              <div className="flex items-start justify-between">
                <div>
                  <h3 className="font-semibold text-gray-800">{address.name}</h3>
                  <p className="text-gray-600 mt-1">
                    {address.street}, {address.number}
                    {address.complement && ` - ${address.complement}`}
                  </p>
                  <p className="text-gray-500 text-sm">
                    {address.state} - CEP: {address.zip_code}
                  </p>
                  <p className="text-gray-500 text-sm">{address.country}</p>
                </div>
                <div className="flex gap-2">
                  <button
                    onClick={() => { setEditingId(address.id); setShowForm(true); }}
                    className="text-sm text-primary-600 hover:underline"
                  >
                    Editar
                  </button>
                  <button
                    onClick={() => handleDelete(address.id)}
                    className="text-sm text-red-600 hover:underline"
                  >
                    Excluir
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

// Componente de formulário de endereço (inline neste arquivo)
function AddressForm({
  editingAddress,
  onSave,
  onCancel,
}: {
  editingAddress?: Address
  onSave: () => void
  onCancel: () => void
}) {
  const [name, setName] = useState(editingAddress?.name || '')
  const [street, setStreet] = useState(editingAddress?.street || '')
  const [number, setNumber] = useState(editingAddress?.number?.toString() || '')
  const [complement, setComplement] = useState(editingAddress?.complement || '')
  const [state, setState] = useState(editingAddress?.state || '')
  const [zipCode, setZipCode] = useState(editingAddress?.zip_code || '')
  const [country, setCountry] = useState(editingAddress?.country || 'Brasil')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)

    const data: CreateAddressRequest = {
      name,
      street,
      number: parseInt(number),
      complement: complement || undefined,
      state,
      zip_code: zipCode,
      country,
    }

    try {
      if (editingAddress) {
        await addressesApi.update(editingAddress.id, data)
      } else {
        await addressesApi.create(data)
      }
      onSave()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao salvar endereço')
      setLoading(false)
    }
  }

  return (
    <div className="bg-white rounded-xl shadow-sm p-6">
      <h2 className="text-lg font-semibold text-gray-800 mb-4">
        {editingAddress ? 'Editar Endereço' : 'Novo Endereço'}
      </h2>

      {error && (
        <div className="bg-red-50 text-red-600 p-3 rounded-lg text-sm mb-4">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="grid md:grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Nome do endereço *
            </label>
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
              placeholder="Ex: Casa, Trabalho"
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              CEP *
            </label>
            <input
              type="text"
              value={zipCode}
              onChange={(e) => setZipCode(e.target.value)}
              required
              placeholder="00000-000"
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none"
            />
          </div>
        </div>

        <div className="grid md:grid-cols-3 gap-4">
          <div className="md:col-span-2">
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Rua *
            </label>
            <input
              type="text"
              value={street}
              onChange={(e) => setStreet(e.target.value)}
              required
              placeholder="Nome da rua"
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Número *
            </label>
            <input
              type="number"
              value={number}
              onChange={(e) => setNumber(e.target.value)}
              required
              placeholder="123"
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none"
            />
          </div>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Complemento
          </label>
          <input
            type="text"
            value={complement}
            onChange={(e) => setComplement(e.target.value)}
            placeholder="Apto, Bloco, etc."
            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none"
          />
        </div>

        <div className="grid md:grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Estado *
            </label>
            <input
              type="text"
              value={state}
              onChange={(e) => setState(e.target.value)}
              required
              placeholder="SP"
              maxLength={2}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              País *
            </label>
            <input
              type="text"
              value={country}
              onChange={(e) => setCountry(e.target.value)}
              required
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none"
            />
          </div>
        </div>

        <div className="flex gap-3 pt-4">
          <button
            type="submit"
            disabled={loading}
            className="flex-1 bg-primary-600 text-white py-2 rounded-lg font-semibold hover:bg-primary-700 transition-colors disabled:opacity-50"
          >
            {loading ? 'Salvando...' : 'Salvar'}
          </button>
          <button
            type="button"
            onClick={onCancel}
            className="px-6 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
          >
            Cancelar
          </button>
        </div>
      </form>
    </div>
  )
}
