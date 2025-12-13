# Como o Better-Auth Gerencia Senhas

## 🔐 Fluxo de Sign-Up

Quando você faz sign-up, você **passa a senha em texto plano** normalmente. O better-auth gerencia tudo internamente:

### 1. No Sign-Up (você passa a senha em texto plano)

```typescript
// ✅ FRONTEND - Você passa a senha normalmente
await authClient.signUp.email({
  email: "user@example.com",
  password: "minhasenha123", // ← Senha em texto plano
  name: "João",
  phone: "11999999999"
});

// ✅ BACKEND - Se usar auth.api, também passa em texto plano
await auth.api.signUp.email({
  body: {
    email: "user@example.com",
    password: "minhasenha123", // ← Senha em texto plano
    name: "João",
    phone: "11999999999"
  }
});
```

### 2. O que o Better-Auth faz internamente:

```
1. Recebe: { email, password: "minhasenha123", name, phone }
   ↓
2. Cria registro na tabela `users`:
   - id: "uuid..."
   - email: "user@example.com"
   - name: "João"
   - phone: "11999999999"
   - password: NULL (ou valor padrão) ← NÃO armazena senha aqui!
   ↓
3. Faz hash da senha usando sua função configurada:
   - hash = Bun.password.hash("minhasenha123")
   - Resultado: "$2b$10$abc123..." (hash bcrypt)
   ↓
4. Cria registro na tabela `accounts`:
   - id: "uuid..."
   - userId: "uuid do usuário"
   - providerId: "credential"
   - password: "$2b$10$abc123..." ← Hash da senha armazenado aqui!
```

### 3. Estrutura das Tabelas

```sql
-- Tabela users (SEM senha)
CREATE TABLE users (
  id UUID PRIMARY KEY,
  email VARCHAR NOT NULL,
  name VARCHAR NOT NULL,
  phone VARCHAR NOT NULL,
  password VARCHAR NULL,  -- ← Pode ser NULL, não é usado!
  ...
);

-- Tabela accounts (COM hash da senha)
CREATE TABLE accounts (
  id UUID PRIMARY KEY,
  userId UUID REFERENCES users(id),
  providerId VARCHAR NOT NULL,  -- "credential" para email/password
  password VARCHAR NOT NULL,    -- ← Hash da senha aqui!
  ...
);
```

## 🔑 Fluxo de Sign-In

No sign-in, você também passa a senha em texto plano:

```typescript
// ✅ FRONTEND
await authClient.signIn.email({
  email: "user@example.com",
  password: "minhasenha123" // ← Senha em texto plano
});

// ✅ BACKEND
await auth.api.signIn.email({
  body: {
    email: "user@example.com",
    password: "minhasenha123" // ← Senha em texto plano
  }
});
```

### O que o Better-Auth faz no Sign-In:

```
1. Recebe: { email, password: "minhasenha123" }
   ↓
2. Busca o usuário na tabela `users` pelo email
   ↓
3. Busca o registro na tabela `accounts` com:
   - userId = id do usuário
   - providerId = "credential"
   ↓
4. Compara a senha usando sua função `verify`:
   - Bun.password.verify("minhasenha123", hash_da_tabela_accounts)
   - Se match → cria sessão
   - Se não match → retorna erro
```

## 📝 Resumo

| Ação | O que você passa | Onde o Better-Auth armazena |
|------|------------------|----------------------------|
| **Sign-Up** | Senha em texto plano | Hash na tabela `accounts` |
| **Sign-In** | Senha em texto plano | Compara com hash em `accounts` |
| **Tabela `users`** | - | **NÃO armazena senha** (pode ser NULL) |
| **Tabela `accounts`** | - | **Armazena hash da senha** |

## ⚠️ Importante

- ✅ **Sempre passe a senha em texto plano** no sign-up e sign-in
- ✅ O better-auth faz o hash automaticamente usando `Bun.password.hash()`
- ✅ A senha nunca é armazenada em texto plano no banco
- ✅ O hash é armazenado na tabela `accounts`, não em `users`
- ✅ A tabela `users` pode ter `password` como NULL (não é usada)

## 🔧 Sua Configuração Atual

```typescript
emailAndPassword: {
  enabled: true,
  password: {
    hash: (password: string) => Bun.password.hash(password),  // ← Hash no sign-up
    verify: ({password, hash}) => Bun.password.verify(password, hash), // ← Verifica no sign-in
  },
}
```

Isso significa:
- No sign-up: `Bun.password.hash()` é chamado automaticamente
- No sign-in: `Bun.password.verify()` é chamado automaticamente
- Você não precisa fazer hash manualmente!

