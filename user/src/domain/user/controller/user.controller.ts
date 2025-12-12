import Elysia from "elysia";
import { auth } from "../../../lib/auth";

/**
 * User Controller
 * 
 * IMPORTANTE: O better-auth já expõe endpoints HTTP automaticamente!
 * Quando você faz .mount(auth.handler) no index.ts, os seguintes endpoints ficam disponíveis:
 * 
 * - POST /api/auth/sign-up/email
 * - POST /api/auth/sign-in/email
 * - POST /api/auth/sign-out
 * - GET /api/auth/session
 * - etc...
 * 
 * Você NÃO precisa criar esses endpoints manualmente!
 */

/**
 * 📝 IMPORTANTE SOBRE A SENHA NO SIGN-UP:
 * 
 * Quando você faz sign-up (no frontend ou backend), você passa a senha em TEXTO PLANO:
 * 
 * Frontend:
 *   await authClient.signUp.email({
 *     email: "user@example.com",
 *     password: "minhasenha123", // ← TEXTO PLANO
 *     name: "João",
 *     phone: "11999999999"
 *   })
 * 
 * O que o Better-Auth faz automaticamente:
 *   1. Recebe a senha em texto plano
 *   2. Cria o usuário na tabela `users` (SEM senha, pode ser NULL)
 *   3. Faz hash da senha usando Bun.password.hash()
 *   4. Armazena o hash na tabela `accounts` (não em `users`!)
 * 
 * Você NÃO precisa fazer hash manualmente! O better-auth cuida de tudo.
 * 
 * Estrutura:
 *   - Tabela `users`: armazena dados do usuário (email, name, phone) - SEM senha
 *   - Tabela `accounts`: armazena o hash da senha (providerId = "credential")
 */
export const userController = new Elysia({ prefix: "/api/users" })
  // Exemplo: Endpoint customizado que usa a sessão do usuário
  .get("/me", async ({ headers }) => {
    // Verificar sessão usando auth.api
    const session = await auth.api.getSession({
      headers,
    });

    if (!session) {
      return { error: "Não autenticado" };
    }

    return { user: session.user };
  });

/**
 * RESUMO:
 * 
 * 🔵 BACKEND (user.controller.ts):
 *    - Use: auth.api.signUp.email({ body: {...} })
 *    - Use: auth.api.getSession({ headers })
 *    - NÃO use: authClient (isso é para frontend!)
 * 
 * 🟢 FRONTEND:
 *    - Use: authClient.signUp.email({ email, password, name, phone })
 *    - O authClient faz chamadas HTTP para /api/auth/sign-up/email
 *    - Instale: npm install better-auth
 *    - Configure: const authClient = createAuthClient({ baseURL: "http://localhost:6060" })
 * 
 * 📡 ENDPOINTS AUTOMÁTICOS (já disponíveis):
 *    - POST http://localhost:6060/api/auth/sign-up/email
 *    - POST http://localhost:6060/api/auth/sign-in/email
 *    - POST http://localhost:6060/api/auth/sign-out
 *    - GET http://localhost:6060/api/auth/session
 */

