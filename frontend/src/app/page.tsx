import Link from 'next/link'

// Esta é a página inicial (rota: /)
// É um Server Component por padrão (não tem 'use client')
// Server Components são renderizados no servidor = melhor SEO e performance
export default function HomePage() {
  return (
    <div className="space-y-12">
      {/* Hero Section */}
      <section className="text-center py-16 bg-gradient-to-r from-primary-600 to-primary-800 rounded-2xl text-white">
        <h1 className="text-4xl md:text-5xl font-bold mb-4">
          Bem-vindo ao E-Commerce Lab
        </h1>
        <p className="text-xl text-primary-100 mb-8 max-w-2xl mx-auto">
          Plataforma de estudos com React, Next.js, TypeScript e Tailwind CSS
          integrada com microserviços reais.
        </p>
        <div className="flex gap-4 justify-center">
          <Link
            href="/products"
            className="bg-white text-primary-600 px-6 py-3 rounded-lg font-semibold hover:bg-primary-50 transition-colors"
          >
            Ver Produtos
          </Link>
          <Link
            href="/auth/register"
            className="border-2 border-white text-white px-6 py-3 rounded-lg font-semibold hover:bg-white/10 transition-colors"
          >
            Criar Conta
          </Link>
        </div>
      </section>

      {/* Features Section */}
      <section>
        <h2 className="text-2xl font-bold text-gray-800 mb-8 text-center">
          Tecnologias Utilizadas
        </h2>
        <div className="grid md:grid-cols-3 gap-6">
          <FeatureCard
            title="React + Next.js"
            description="Framework moderno para criar aplicações web com SSR, SSG e otimizações automáticas."
          />
          <FeatureCard
            title="TypeScript"
            description="Tipagem estática para JavaScript, evitando bugs e melhorando a produtividade."
          />
          <FeatureCard
            title="Tailwind CSS"
            description="Framework CSS utilitário para estilização rápida e consistente."
          />
          <FeatureCard
            title="APIs REST"
            description="Consumo de múltiplos microserviços: User, Catalog e Search."
          />
          <FeatureCard
            title="Autenticação"
            description="Sistema completo de login com cookies e sessões seguras."
          />
          <FeatureCard
            title="CRUD Completo"
            description="Criar, ler, atualizar e deletar produtos e endereços."
          />
        </div>
      </section>

      {/* API Endpoints Section */}
      <section className="bg-gray-100 rounded-xl p-8">
        <h2 className="text-2xl font-bold text-gray-800 mb-6">
          Microserviços Disponíveis
        </h2>
        <div className="grid md:grid-cols-3 gap-6">
          <ServiceCard
            name="User Service"
            port="8085"
            description="Autenticação, usuários e endereços"
            endpoints={['POST /api/auth/sign-up/email', 'POST /api/auth/sign-in/email', 'GET /api/users/me', 'CRUD /api/addresses']}
          />
          <ServiceCard
            name="Catalog Service"
            port="8080"
            description="CRUD de produtos (escrita)"
            endpoints={['GET /api/products', 'POST /api/products', 'PUT /api/products/:id', 'DELETE /api/products/:id']}
          />
          <ServiceCard
            name="Search Service"
            port="8081"
            description="Busca otimizada (leitura)"
            endpoints={['GET /api/search/products', 'GET /api/search/products/search?q=', 'GET /api/search/products/category?category=']}
          />
        </div>
      </section>
    </div>
  )
}

// Componentes auxiliares (podem estar em arquivos separados)
// Props = propriedades que o componente recebe

function FeatureCard({ title, description }: { title: string; description: string }) {
  return (
    <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100 hover:shadow-md transition-shadow">
      <h3 className="font-semibold text-lg text-gray-800 mb-2">{title}</h3>
      <p className="text-gray-600">{description}</p>
    </div>
  )
}

function ServiceCard({
  name,
  port,
  description,
  endpoints
}: {
  name: string
  port: string
  description: string
  endpoints: string[]
}) {
  return (
    <div className="bg-white p-6 rounded-xl shadow-sm">
      <div className="flex items-center justify-between mb-2">
        <h3 className="font-semibold text-lg text-gray-800">{name}</h3>
        <span className="text-xs bg-primary-100 text-primary-700 px-2 py-1 rounded">
          :{port}
        </span>
      </div>
      <p className="text-gray-600 text-sm mb-4">{description}</p>
      <ul className="space-y-1">
        {/* map = itera sobre array e retorna elementos */}
        {endpoints.map((endpoint, index) => (
          <li key={index} className="text-xs font-mono text-gray-500">
            {endpoint}
          </li>
        ))}
      </ul>
    </div>
  )
}
