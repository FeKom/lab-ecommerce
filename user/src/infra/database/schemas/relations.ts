import { relations } from "drizzle-orm";
import { usersTable } from "./users";
import { addressTable } from "./address";
export const usersRelations = relations(usersTable, ({ one, many }) => ({
  address: one(addressTable, {
    fields: [usersTable.id], // 👈 OPCIONAL: se não tiver addressId na usersTable
    references: [addressTable.userId],
  }),
  addresses: many(addressTable), // Para 1:N
}));

export const addressesRelations = relations(addressTable, ({ one }) => ({
  user: one(usersTable, {
    fields: [addressTable.userId],
    references: [usersTable.id],
  }),
}));
