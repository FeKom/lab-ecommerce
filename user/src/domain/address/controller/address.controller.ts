import Elysia from "elysia";
import { AddressRepository } from "../repository/address.repository";
import { auth } from "../../../lib/auth";

const addressRepository = new AddressRepository();

export const addressController = new Elysia({ prefix: "/api/addresses" })
  // Criar novo endereço (requer autenticação)
  .post("/", async (context) => {
    const session = await auth.api.getSession({
      headers: context.headers,
    });

    if (!session) {
      context.status(401);
      return { error: "Não autenticado" };
    }

    const body = context.body as {
      name: string;
      number: number;
      street: string;
      state: string;
      zip_code: string;
      country: string;
      complement?: string;
    };

    try {
      const address = await addressRepository.create({
        user_id: session.user.id,
        name: body.name,
        number: body.number,
        street: body.street,
        state: body.state,
        zip_code: body.zip_code,
        country: body.country,
        complement: body.complement,
      });

      context.status(201);
      return address;
    } catch (error: any) {
      context.status(400);
      return { error: error.message };
    }
  })

  // Listar todos os endereços do usuário (requer autenticação)
  .get("/", async (context) => {
    const session = await auth.api.getSession({
      headers: context.headers,
    });

    if (!session) {
      context.status(401);
      return { error: "Não autenticado" };
    }

    const addresses = await addressRepository.findByUserId(session.user.id);
    return { addresses };
  })

  // Obter endereço específico (requer autenticação)
  .get("/:id", async (context) => {
    const session = await auth.api.getSession({
      headers: context.headers,
    });

    if (!session) {
      return context.status(401).send({ error: "Não autenticado" });
    }

    const { id } = context.params;
    const address = await addressRepository.findById(id);

    if (!address) {
      context.status(404);
      return { error: "Endereço não encontrado" };
    }

    // Verificar se o endereço pertence ao usuário
    if (address.user_id !== session.user.id) {
      context.status(403);
      return { error: "Acesso negado" };
    }

    return { address };
  })

  // Atualizar endereço (requer autenticação)
  .put("/:id", async (context) => {
    const session = await auth.api.getSession({
      headers: context.headers,
    });

    if (!session) {
      return context.status(401).send({ error: "Não autenticado" });
    }

    const { id } = context.params;
    const body = context.body as {
      name?: string;
      number?: number;
      street?: string;
      state?: string;
      zip_code?: string;
      country?: string;
      complement?: string;
    };

    try {
      const address = await addressRepository.update(id, session.user.id, body);
      return { address };
    } catch (error: any) {
      if (error.message.includes("not found")) {
        context.status(404);
        return { error: "Endereço não encontrado" };
      }
      context.status(400);
      return { error: error.message };
    }
  })

  // Deletar endereço (requer autenticação)
  .delete("/:id", async (context) => {
    const session = await auth.api.getSession({
      headers: context.headers,
    });

    if (!session) {
      context.status(401);
      return { error: "Não autenticado" };
    }

    const { id } = context.params;

    try {
      await addressRepository.delete(id, session.user.id);
      context.status(204);
      return;
    } catch (error: any) {
      context.status(400);
      return { error: error.message };
    }
  });

