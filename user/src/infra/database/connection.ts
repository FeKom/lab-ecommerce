import { drizzle } from "drizzle-orm/bun-sql";
import { addressTable } from "./schemas/address";
import { usersTable } from "./schemas/users";
export const db = drizzle(Bun.env.DATABASE_URL!, {
  schema: {
    users: usersTable,
    addresses: addressTable,
  },
});
