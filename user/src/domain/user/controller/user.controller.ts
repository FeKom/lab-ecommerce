import Elysia from "elysia";
import { auth } from "../../../lib/auth";
import { pool } from "../../../infra/database/pool";

/**
 * User Controller
 *
 * IMPORTANTE: O better-auth já expõe endpoints HTTP automaticamente!
 * Quando você faz .mount(auth.handler) no index.ts, os seguintes endpoints ficam disponíveis:
 *
 * - POST /api/auth/sign-up/email
 * - POST /api/auth/sign-in/email
 * - POST /api/auth/sign-out
 * - GET /api/auth/session (para uso interno)
 * - etc...
 *
 * Este controller adiciona endpoints customizados para facilitar o uso,
 * incluindo /api/users/session para validação de sessão por outros serviços.
 */

export const userController = new Elysia({ prefix: "/api/users" })
  // Obter informações do usuário logado
  .get("/me", async (context) => {
    const session = await auth.api.getSession({
      headers: context.headers,
    });

    if (!session) {
      context.status(401);
      return { error: "Não autenticado" };
    }

    // Verificar se o email está verificado
    if (!session.user.emailVerified) {
      context.status(401);
      return { error: "Email não verificado. Faça login novamente." };
    }

    return { user: session.user };
  })

  // Endpoint para validação de sessão (usado por outros serviços)
  .get("/session", async (context) => {
    const session = await auth.api.getSession({
      headers: context.headers,
    });

    if (!session) {
      context.status(401);
      return { error: "Não autenticado" };
    }

    // Verificar se o email está verificado
    if (!session.user.emailVerified) {
      context.status(401);
      return { error: "Email não verificado. Faça login novamente." };
    }

    return { user: session.user, session: session.session };
  })

  // Logout
  .post("/logout", async (context) => {
    try {
      await auth.api.signOut({
        headers: context.headers,
      });

      return { message: "Logout realizado com sucesso" };
    } catch (error: any) {
      context.status(400);
      return { error: error.message };
    }
  })

  // Endpoint para desenvolvimento: marcar email como verificado
  .post("/verify-email/:userId", async (context) => {
    try {
      const { userId } = context.params;

      // Atualizar diretamente no banco
      const result = await pool.query(
        'UPDATE users SET email_verified = true WHERE id = $1',
        [userId]
      );

      if (result.rowCount === 0) {
        context.status(404);
        return { error: "Usuário não encontrado" };
      }

      return { message: "Email marcado como verificado", userId };
    } catch (error: any) {
      context.status(400);
      return { error: error.message };
    }
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
 *    - Configure: const authClient = createAuthClient({ baseURL: "http://localhost:3000" })
 *
 * 📡 ENDPOINTS AUTOMÁTICOS (já disponíveis):
 *    - POST http://localhost:3000/api/auth/sign-up/email
 *    - POST http://localhost:3000/api/auth/sign-in/email
 *    - POST http://localhost:3000/api/auth/sign-out
 *    - GET http://localhost:3000/api/auth/session (para uso interno)
 *
 * 🔗 ENDPOINTS CUSTOMIZADOS (adicionados neste controller):
 *    - GET http://localhost:3000/api/users/me (informações do usuário logado)
 *    - GET http://localhost:3000/api/users/session (validação de sessão para outros serviços)
 *    - POST http://localhost:3000/api/users/logout
 *    - POST http://localhost:3000/api/users/verify-email/:userId
 */

