# Exemplo de uso do Better Auth no Frontend

## Instalação

```bash
npm install better-auth
```

## Configuração do Auth Client

```typescript
// frontend/src/lib/auth-client.ts
import { createAuthClient } from "better-auth/react"; // ou "better-auth/angular" se usar Angular

export const authClient = createAuthClient({
  baseURL: "http://localhost:6060", // URL do seu backend
  basePath: "/api/auth", // caminho base configurado no backend
});
```

## Uso no Frontend (React/Next.js)

```tsx
// frontend/src/components/SignUpForm.tsx
import { authClient } from "@/lib/auth-client";

export function SignUpForm() {
  const handleSignUp = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    
    const formData = new FormData(e.currentTarget);
    
    try {
      // ✅ Use authClient no frontend!
      const result = await authClient.signUp.email({
        email: formData.get("email") as string,
        password: formData.get("password") as string,
        name: formData.get("name") as string,
        phone: formData.get("phone") as string, // campo adicional
      });

      if (result.error) {
        console.error("Erro ao criar conta:", result.error);
        return;
      }

      console.log("Usuário criado:", result.data);
      // Redirecionar ou mostrar mensagem de sucesso
    } catch (error) {
      console.error("Erro:", error);
    }
  };

  return (
    <form onSubmit={handleSignUp}>
      <input name="email" type="email" required />
      <input name="password" type="password" required />
      <input name="name" type="text" required />
      <input name="phone" type="tel" required />
      <button type="submit">Cadastrar</button>
    </form>
  );
}
```

## Uso no Frontend (Angular)

```typescript
// frontend/src/app/services/auth.service.ts
import { Injectable } from '@angular/core';
import { createAuthClient } from 'better-auth/angular';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private authClient = createAuthClient({
    baseURL: 'http://localhost:6060',
    basePath: '/api/auth',
  });

  async signUp(email: string, password: string, name: string, phone: string) {
    return await this.authClient.signUp.email({
      email,
      password,
      name,
      phone,
    });
  }

  async signIn(email: string, password: string) {
    return await this.authClient.signIn.email({
      email,
      password,
    });
  }

  async getSession() {
    return await this.authClient.getSession();
  }
}
```

## Endpoints Disponíveis Automaticamente

O better-auth cria automaticamente estes endpoints quando você faz `.mount(auth.handler)`:

- `POST /api/auth/sign-up/email` - Criar conta
- `POST /api/auth/sign-in/email` - Fazer login
- `POST /api/auth/sign-out` - Fazer logout
- `GET /api/auth/session` - Obter sessão atual
- `POST /api/auth/verify-email` - Verificar email
- `POST /api/auth/resend-verification` - Reenviar email de verificação

## Diferença entre Backend e Frontend

| Local | O que usar | Exemplo |
|-------|------------|---------|
| **Backend** | `auth.api.*` | `auth.api.signUp.email({ body: {...} })` |
| **Frontend** | `authClient.*` | `authClient.signUp.email({ email, password })` |

**Backend**: Acesso direto à API, sem HTTP  
**Frontend**: Faz chamadas HTTP para os endpoints do backend

