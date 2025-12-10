import {
  pgTable,
  varchar,
  integer,
  timestamp,
  boolean,
} from "drizzle-orm/pg-core";

export const usersTable = pgTable("users", {
  id: varchar({ length: 36 }).primaryKey(),
  email: varchar({ length: 255 }).notNull().unique(),
  name: varchar({ length: 255 }).notNull(),
  age: integer(),
  phone: varchar({ length: 20 }).notNull(),
  passwordHash: varchar({ length: 255 }).notNull(),
  role: varchar({ length: 20 }).default("user"),
  active: boolean().default(true).notNull(),
  productId: varchar("product_id", { length: 36 }).array(),
  createdAt: timestamp("created_at").defaultNow().notNull(),
  updatedAt: timestamp("updated_at").defaultNow().notNull(),
});
