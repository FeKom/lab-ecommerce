import { Kysely, PostgresDialect } from "kysely";
import { Database } from "./types";
import { pool } from "./pool"; 

export const dialect = new PostgresDialect({ pool });

export const db = new Kysely<Database>({
  dialect,
});
