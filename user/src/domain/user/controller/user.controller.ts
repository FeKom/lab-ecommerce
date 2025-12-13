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
 * Este controller adiciona endpoints customizados para facilitar o uso.
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

    return { user: session.user };
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

