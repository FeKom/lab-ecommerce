import { db } from "../../../infra/database/db";
import { NewAddress, Address } from "../../../infra/database/types";
import { uuidv7 } from "uuidv7";

export class AddressRepository {
  async create(address: Omit<NewAddress, "id">): Promise<Address> {
    const newAddress = await db
      .insertInto("address")
      .values({
        id: uuidv7(),
        user_id: address.user_id,
        name: address.name,
        number: address.number,
        street: address.street,
        state: address.state,
        zip_code: address.zip_code,
        country: address.country,
        complement: address.complement ?? null,
        active: address.active ?? true,
      })
      .returningAll()
      .executeTakeFirstOrThrow();

    return newAddress;
  }

  async findById(id: string): Promise<Address | undefined> {
    return await db
      .selectFrom("address")
      .selectAll()
      .where("id", "=", id)
      .where("active", "=", true)
      .executeTakeFirst();
  }

  async findByUserId(userId: string): Promise<Address[]> {
    return await db
      .selectFrom("address")
      .selectAll()
      .where("user_id", "=", userId)
      .where("active", "=", true)
      .orderBy("created_at", "desc")
      .execute();
  }

  async update(id: string, userId: string, updates: Partial<Omit<NewAddress, "id" | "user_id" | "created_at">>): Promise<Address> {
    const updated = await db
      .updateTable("address")
      .set({
        ...updates,
      })
      .where("id", "=", id)
      .where("user_id", "=", userId)
      .where("active", "=", true)
      .returningAll()
      .executeTakeFirstOrThrow();

    return updated;
  }

  async delete(id: string, userId: string): Promise<void> {
    // Soft delete - marca como inativo
    await db
      .updateTable("address")
      .set({
        active: false,
      })
      .where("id", "=", id)
      .where("user_id", "=", userId)
      .execute();
  }
}

