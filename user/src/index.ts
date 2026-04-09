import openapi from "@elysiajs/openapi";
import cors from "@elysiajs/cors";
import Elysia from "elysia";
import { auth, OpenAPI } from "./lib/auth";
import { userController } from "./domain/user/controller/user.controller";
import { addressController } from "./domain/address/controller/address.controller";
import logixlysia from "logixlysia";

const betterAuth = new Elysia({ name: "better-auth" })
  .use(
    cors({
      origin: "http://localhost:3000",
      methods: ["GET", "POST", "PUT", "DELETE"],
      allowedHeaders: ["Content-Type", "Authorization"],
      exposeHeaders: ["Content-Type", "Authorization"],
      maxAge: 3600,
      credentials: true,
    }),
  )
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
  .use(
    openapi({
      documentation: {
        components: await OpenAPI.components,
        paths: await OpenAPI.getPaths(),
      },
    }),
  )
  .use(
    logixlysia({
      config: {
        showStartupMessage: true,
        startupMessageFormat: "simple",
        timestamp: {
          translateTime: "yyyy-mm-dd HH:MM:ss.SSS",
        },
        //logFilePath: "./logs/example.log",
        ip: true,
        customLogFormat:
          "🦊 {now} {level} {duration} {method} {pathname} {status} {message} {ip}",
      },
    }),
  )
  .use(betterAuth)
  .use(userController)
  .use(addressController)
  .get("/", () => "Hello Elysia")
  .get("/health", () => ({ status: "ok" }))
  .listen({
    port: process.env.PORT ? parseInt(process.env.PORT) : 8085,
    hostname: "localhost",
  });

console.log(
  `🦊 Elysia is running at ${app.server?.hostname}:${app.server?.port}`,
);

export default app;
