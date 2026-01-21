import type { Metadata } from "next";
import "./globals.css";
import { Providers } from "./providers";
import { Navbar } from "@/components/Navbar";

// Metadata = configuração de SEO (título, descrição da página)
// Isso é específico do Next.js - renderizado no servidor
export const metadata: Metadata = {
  title: "E-Commerce Lab",
  description: "Plataforma de e-commerce com microserviços",
};

// RootLayout = componente que envolve TODAS as páginas
// É como o "template" principal do site
export default function RootLayout({
  children, // children = o conteúdo da página atual
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="pt-BR">
      <body className="min-h-screen bg-gray-50">
        {/* Providers = componente que agrupa todos os Context Providers */}
        <Providers>
          <Navbar />
          <main className="container mx-auto px-4 py-8">{children}</main>
        </Providers>
      </body>
    </html>
  );
}
