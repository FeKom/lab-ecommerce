import { betterAuth } from "better-auth";
import { pool } from "../infra/database/pool";
import { uuidv7 } from "uuidv7";
import { openAPI } from "better-auth/plugins";


export const auth = betterAuth({
  plugins: [openAPI()],
  database: pool,
  emailAndPassword: {
    enabled: true,
    password: {
      hash: async (password: string)  =>  await Bun.password.hash(password, "argon2id"), 
      verify: async ({password, hash}: {password: string, hash: string}) => 
        await Bun.password.verify(password, hash),
    },
  },
  session: {
    modelName: "sessions",
    expiresIn: 60 * 60 * 24 * 7,
    updateAge: 60 * 60 * 24
  },
  account: {
    modelName: "accounts",
  },
  user: {
    modelName: "users",
    additionalFields: {
      phone: {
        type: "string",
        required: true,
        input: true,
      },
      active: {
        type: "boolean",
        required: true,
        defaultValue: true,
        input: false,
      },
      productsId: {
        type: "string[]",
        required: false,
        input: false,
        fieldName: "product_id",
      },
      role: {
        type: "string",
        required: true,
        defaultValue: "user",
        input: false,
      }
    }
  },
  advanced: {
    database: {
      generateId: () =>  uuidv7()
      },
    },
  logger: {
    level: "debug"
  },
  basePath: "/api/auth",
  baseURL: process.env.AUTH_BASE_URL,
  secret: process.env.AUTH_SECRET,
});


let _schema: ReturnType<typeof auth.api.generateOpenAPISchema>
const getSchema = async () => (_schema ??= auth.api.generateOpenAPISchema())

export const OpenAPI = {
    getPaths: () =>
        getSchema().then(({ paths }) => {
            const reference: typeof paths = Object.create(null)

            for (const path of Object.keys(paths)) {
                const key = "/api/auth" + path
                reference[key] = paths[path]

                for (const method of Object.keys(paths[path])) {
                    const operation = (reference[key] as any)[method]

                    operation.tags = ['Better Auth']
                }
            }

            return reference
        }) as Promise<any>,
    components: getSchema().then(({ components }) => components) as Promise<any>
} as const


//bun x @better-auth/cli@latest generate
//bun x @better-auth/cli@latest migrate
