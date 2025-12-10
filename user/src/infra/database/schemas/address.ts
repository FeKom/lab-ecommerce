import { pgTable, varchar, timestamp, boolean } from "drizzle-orm/pg-core";
import { usersTable } from "./users";
export const addressTable = pgTable("address", {
  id: varchar({ length: 36 }).primaryKey(),
  userId: varchar("user_id", { length: 36 })
    .notNull()
    .references(() => usersTable.id, {
      onDelete: "cascade",
    }),
  name: varchar({ length: 255 }).notNull(),
  street: varchar({ length: 255 }).notNull(),
  city: varchar({ length: 255 }).notNull(),
  state: varchar({ length: 255 }).notNull(),
  zipCode: varchar({ length: 10 }).notNull(),
  country: varchar({ length: 255 }).notNull(),
  complement: varchar({ length: 255 }),
  active: boolean().default(true).notNull(),
  createdAt: timestamp().defaultNow().notNull(),
  updatedAt: timestamp().defaultNow(),
});
