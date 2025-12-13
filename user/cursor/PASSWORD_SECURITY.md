# Configuração de Segurança de Senhas

## Algoritmo de Hash: Argon2id

Utilizamos **Argon2id** para hashing de senhas, que é considerado o algoritmo mais seguro atualmente disponível.

### Por que Argon2id?

- ✅ **Resistente a ataques com GPU/ASIC**: Muito mais difícil de quebrar com hardware especializado
- ✅ **Memória intensivo**: Requer muita memória RAM, dificultando ataques paralelos
- ✅ **Recomendado pela OWASP**: Considerado o estado da arte em password hashing
- ✅ **Adaptável**: Pode aumentar a dificuldade ao longo do tempo conforme hardware melhora

### Comparação com outros algoritmos:

| Algoritmo | Segurança | Resistência a GPU | Velocidade |
|-----------|-----------|-------------------|------------|
| MD5/SHA1 | ❌ Muito baixa | ❌ Nenhuma | ⚡ Muito rápido |
| bcrypt | ✅ Boa | ⚠️ Média | ⚡ Rápido |
| scrypt | ✅ Muito boa | ✅ Boa | 🐌 Médio |
| **Argon2id** | ✅ **Excelente** | ✅ **Excelente** | 🐌 Médio |

### Parâmetros Configurados

```typescript
{
  algorithm: "argon2id",
  memoryCost: 65536,  // 64 MB de memória RAM necessária
  timeCost: 3,        // 3 iterações
}
```

**Explicação dos parâmetros:**
- `memoryCost: 65536` (64 MB): A quantidade de memória necessária para calcular o hash
  - Maior = mais seguro, mas requer mais RAM
  - Recomendado: 64 MB é um bom equilíbrio
- `timeCost: 3`: Número de iterações do algoritmo
  - Maior = mais seguro, mas mais lento
  - Recomendado: 3 é suficiente para a maioria dos casos

### Ajustando os Parâmetros

Se precisar aumentar a segurança (em servidores mais potentes):

```typescript
{
  algorithm: "argon2id",
  memoryCost: 131072,  // 128 MB (mais seguro)
  timeCost: 4,         // Mais iterações (mais seguro)
}
```

⚠️ **Atenção**: Aumentar esses valores também aumenta o tempo de resposta do sign-up e sign-in!

## Estrutura do Banco de Dados

### Tabela `users`
- Campo `password`: **NULL permitido** ✅
- O better-auth não armazena senha aqui
- Usado apenas para dados do usuário (email, name, phone, etc.)

### Tabela `accounts`
- Campo `password`: **Hash Argon2id armazenado aqui** ✅
- ProviderId: `"credential"`
- Relacionamento: `userId` → `users.id`

## Implementação

### Hash (Sign-Up)
```typescript
hash: async (password: string) => {
  return await Bun.password.hash(password, {
    algorithm: "argon2id",
    memoryCost: 65536,
    timeCost: 3,
  });
}
```

### Verificação (Sign-In)
```typescript
verify: async ({ password, hash }) => {
  return await Bun.password.verify(password, hash);
}
```

## Migração de Senhas Existentes

Se você já tem senhas hasheadas com bcrypt e quer migrar para Argon2id:

1. Quando o usuário fizer login, verifique se é bcrypt
2. Se for, valide com bcrypt
3. Re-hashie com Argon2id
4. Salve o novo hash

```typescript
// Exemplo de migração gradual
verify: async ({ password, hash }) => {
  // Verificar se é hash antigo (bcrypt) ou novo (argon2id)
  if (hash.startsWith('$2')) {
    // Hash antigo (bcrypt) - validar e migrar
    const isValid = await bcrypt.compare(password, hash);
    if (isValid) {
      // Re-hashiar com Argon2id
      const newHash = await Bun.password.hash(password, {
        algorithm: "argon2id",
        memoryCost: 65536,
        timeCost: 3,
      });
      // Salvar novo hash no banco
      // await updateAccountHash(userId, newHash);
    }
    return isValid;
  }
  
  // Hash novo (argon2id)
  return await Bun.password.verify(password, hash);
}
```

## Boas Práticas

1. ✅ **Sempre use async/await** para hashing (operações assíncronas)
2. ✅ **Nunca armazene senhas em texto plano**
3. ✅ **Use salt automático** (o Bun faz isso automaticamente)
4. ✅ **Nunca reutilize hashes** (cada hash é único)
5. ✅ **Valide força de senha no frontend** antes de enviar
6. ✅ **Limite tentativas de login** para prevenir brute-force
7. ✅ **Use HTTPS** sempre (senhas trafegam criptografadas)

## Referências

- [OWASP Password Storage Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html)
- [Argon2 Specification](https://github.com/P-H-C/phc-winner-argon2)
- [Bun Password API](https://bun.sh/docs/api/crypto#bun-password)

