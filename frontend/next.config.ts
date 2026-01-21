import type { NextConfig } from 'next'

const nextConfig: NextConfig = {
  async rewrites() {
    return [
      {
        source: '/api/auth/:path*',
        destination: 'http://localhost:8085/api/auth/:path*',
      },
      {
        source: '/api/users/:path*',
        destination: 'http://localhost:8085/api/users/:path*',
      },
      {
        source: '/api/addresses/:path*',
        destination: 'http://localhost:8085/api/addresses/:path*',
      },
      {
        source: '/api/products/:path*',
        destination: 'http://localhost:8080/api/products/:path*',
      },
      {
        source: '/api/search/:path*',
        destination: 'http://localhost:8081/api/search/:path*',
      },
    ]
  },
}

export default nextConfig
