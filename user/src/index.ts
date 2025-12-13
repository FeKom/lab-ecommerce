import openapi from "@elysiajs/openapi";
import Elysia from "elysia";
import {auth, OpenAPI} from "./lib/auth"
import { userController } from "./domain/user/controller/user.controller";
import { addressController } from "./domain/address/controller/address.controller";

const betterAuth = new Elysia({ name: "better-auth" })
  .mount(auth.handler)
  .macro({
    auth: {
      async resolve({ status, request: { headers } }) {
        const session = await auth.api.getSession({
          headers,
        });
        if (!session) return status(401);
        return {
          user: session.user,
          session: session.session,
        };
      },
    },
  });

const app = new Elysia()
  .use(openapi({
    documentation: {
      components: await OpenAPI.components,
      paths: await OpenAPI.getPaths()
    }
  }
  ))
  .use(betterAuth)
  .use(userController)
  .use(addressController)
  .get("/", () => "Hello Elysia")
  .get("/health", () => ({ status: "ok" }))
  .listen(6060);

console.log(
  `🦊 Elysia is running at ${app.server?.hostname}:${app.server?.port}`,
);

export default app;
