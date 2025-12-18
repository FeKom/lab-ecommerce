# Endpoints da API - User Service

## 🔐 Autenticação (Better-Auth - Automáticos)

| Endpoint | Método | Descrição | Autenticação |
|----------|--------|-----------|--------------|
| `/api/auth/sign-up/email` | POST | Criar nova conta | ❌ Não |
| `/api/auth/sign-in/email` | POST | Fazer login | ❌ Não |
| `/api/auth/sign-out` | POST | Fazer logout | ✅ Sim |
| `/api/auth/session` | GET | Obter sessão atual | ✅ Sim |

### Exemplo de Sign-Up

```bash
curl -X POST http://localhost:3000/api/auth/sign-up/email \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "senha123",
    "name": "João Silva",
    "phone": "11999999999"
  }'
```

### Exemplo de Sign-In

```bash
curl -X POST http://localhost:3000/api/auth/sign-in/email \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "senha123"
  }'
```

## 👤 Usuário

| Endpoint | Método | Descrição | Autenticação |
|----------|--------|-----------|--------------|
| `/api/users/me` | GET | Obter informações do usuário logado | ✅ Sim |
| `/api/users/logout` | POST | Fazer logout | ✅ Sim |

### Exemplo: Obter informações do usuário

```bash
curl -X GET http://localhost:3000/api/users/me \
  -H "Cookie: better-auth.session_token=abc123..."
```

**Resposta:**
```json
{
  "user": {
    "id": "019b1391-7aa2-7c4b-bc85-0e079de103c0",
    "email": "user@example.com",
    "name": "João Silva",
    "phone": "11999999999",
    "role": "user",
    "active": true,
    "email_verified": false
  }
}
```

## 📍 Endereços

| Endpoint | Método | Descrição | Autenticação |
|----------|--------|-----------|--------------|
| `/api/addresses` | POST | Criar novo endereço | ✅ Sim |
| `/api/addresses` | GET | Listar endereços do usuário | ✅ Sim |
| `/api/addresses/:id` | GET | Obter endereço específico | ✅ Sim |
| `/api/addresses/:id` | PUT | Atualizar endereço | ✅ Sim |
| `/api/addresses/:id` | DELETE | Deletar endereço | ✅ Sim |

### Exemplo: Criar endereço

```bash
curl -X POST http://localhost:3000/api/addresses \
  -H "Content-Type: application/json" \
  -H "Cookie: better-auth.session_token=abc123..." \
  -d '{
    "name": "Casa",
    "number": 123,
    "street": "Rua das Flores",
    "state": "SP",
    "zip_code": "01234-567",
    "country": "Brasil",
    "complement": "Apto 45"
  }'
```

**Resposta:**
```json
{
  "id": "019b1391-7aa2-7c4b-bc85-0e079de103c0",
  "user_id": "019b1391-7aa2-7c4b-bc85-0e079de103c0",
  "name": "Casa",
  "number": 123,
  "street": "Rua das Flores",
  "state": "SP",
  "zip_code": "01234-567",
  "country": "Brasil",
  "complement": "Apto 45",
  "active": true,
  "created_at": "2025-12-12T18:00:00Z",
  "updated_at": "2025-12-12T18:00:00Z"
}
```

### Exemplo: Listar endereços

```bash
curl -X GET http://localhost:3000/api/addresses \
  -H "Cookie: better-auth.session_token=abc123..."
```

**Resposta:**
```json
{
  "addresses": [
    {
      "id": "019b1391-7aa2-7c4b-bc85-0e079de103c0",
      "name": "Casa",
      "street": "Rua das Flores",
      // ... outros campos
    }
  ]
}
```

### Exemplo: Atualizar endereço

```bash
curl -X PUT http://localhost:3000/api/addresses/019b1391-7aa2-7c4b-bc85-0e079de103c0 \
  -H "Content-Type: application/json" \
  -H "Cookie: better-auth.session_token=abc123..." \
  -d '{
    "name": "Trabalho",
    "number": 456
  }'
```

### Exemplo: Deletar endereço

```bash
curl -X DELETE http://localhost:3000/api/addresses/019b1391-7aa2-7c4b-bc85-0e079de103c0 \
  -H "Cookie: better-auth.session_token=abc123..."
```

## 🔒 Autenticação

Todos os endpoints que requerem autenticação precisam do cookie de sessão:

```
Cookie: better-auth.session_token=abc123...
```

O cookie é definido automaticamente após o login via `/api/auth/sign-in/email`.

## 📚 Documentação OpenAPI

Acesse a documentação Swagger em:
```
http://localhost:3000/
```

Ou visualize os endpoints em:
```
http://localhost:3000/openapi
```

