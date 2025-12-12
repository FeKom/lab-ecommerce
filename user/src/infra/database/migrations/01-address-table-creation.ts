import { Kysely, sql } from "kysely";
export async function up(db: Kysely<unknown>): Promise<void> {
    await db.schema
    .createTable("address")
    .addColumn("id", "varchar(36)", (col) => col.primaryKey())
    .addColumn("user_id", "uuid", (col) => col.references("users.id").onDelete("cascade"))
    .addColumn("name", "varchar", (col) => col.notNull())
    .addColumn("number", "integer", (col) => col.notNull())
    .addColumn("street", "varchar", (col) => col.notNull())
    .addColumn("state", "varchar", (col) => col.notNull())
    .addColumn("zip_code", "varchar", (col) => col.notNull())
    .addColumn("country", "varchar", (col) => col.notNull())
    .addColumn("complement", "text")
    .addColumn("active", "boolean", (col) => col.defaultTo(true).notNull())
    .addColumn("created_at", "timestamp", (col) =>
        col.defaultTo(sql`now()`).notNull()
      )
      .addColumn("updated_at", "timestamp", (col) =>
        col.defaultTo(sql`now()`).notNull()
      )
      .execute();




}