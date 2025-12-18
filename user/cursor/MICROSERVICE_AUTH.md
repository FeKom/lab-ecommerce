# Autenticação entre Microserviços

Este documento explica como outros microserviços (como o **catalog service**) podem verificar se um usuário está autenticado.

## 🔐 Como Funciona

O Better-Auth usa **cookies de sessão** para autenticação. Quando um usuário faz login, o better-auth:
1. Cria uma sessão no banco de dados
2. Gera um token de sessão
3. Define um cookie HTTP-only no navegador

## 📡 Verificando Autenticação em Outros Microserviços

### Opção 1: Verificar Sessão via API do User Service (Recomendado)

O catalog service pode fazer uma chamada HTTP para o user service para verificar se o usuário está autenticado:

```java
// Catalog Service (Java/Spring Boot)
@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    @Autowired
    private RestTemplate restTemplate;
    
    private static final String USER_SERVICE_URL = "http://user-service:8085";
    
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
        @Valid @RequestBody CreateProductRequest request,
        HttpServletRequest httpRequest
    ) {
        // 1. Extrair cookies da requisição
        String cookieHeader = httpRequest.getHeader("Cookie");
        
        // 2. Verificar sessão no user service
        HttpHeaders headers = new HttpHeaders();
        headers.set("Cookie", cookieHeader);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                USER_SERVICE_URL + "/api/auth/session",
                HttpMethod.GET,
                entity,
                Map.class
            );
            
            // 3. Se a sessão for válida, prosseguir
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> session = response.getBody();
                String userId = (String) ((Map) session.get("user")).get("id");
                
                // Criar produto associado ao usuário
                return createProductForUser(request, userId);
            }
        } catch (HttpClientErrorException.Unauthorized e) {
            return ResponseEntity.status(401).build();
        }
        
        return ResponseEntity.status(401).build();
    }
}
```

### Opção 2: Criar um Endpoint de Validação de Token

Criar um endpoint no user service que valida tokens/sessões:

```typescript
// user/src/domain/user/controller/user.controller.ts
.get("/validate-session", async (context) => {
  const session = await auth.api.getSession({
    headers: context.headers,
  });

  if (!session) {
    context.status(401);
    return { valid: false };
  }

  return {
    valid: true,
    userId: session.user.id,
    user: {
      id: session.user.id,
      email: session.user.email,
      name: session.user.name,
      role: session.user.role,
    },
  };
})
```

Então o catalog service chama:

```java
// Verificar sessão
ResponseEntity<SessionValidation> response = restTemplate.exchange(
    USER_SERVICE_URL + "/api/users/validate-session",
    HttpMethod.GET,
    entity,
    SessionValidation.class
);

if (response.getBody().isValid()) {
    String userId = response.getBody().getUserId();
    // Prosseguir com criação do produto
}
```

### Opção 3: Usar JWT Tokens (Alternativa)

Se preferir usar JWT em vez de cookies, você pode configurar o better-auth para emitir JWT:

```typescript
// user/src/lib/auth.ts
export const auth = betterAuth({
  // ... outras configurações
  session: {
    expiresIn: 60 * 60 * 24 * 7,
    updateAge: 60 * 60 * 24,
    cookieCache: {
      enabled: true,
      maxAge: 60 * 60 * 24 * 7,
    },
  },
  // Adicionar plugin JWT se necessário
});
```

## 🔧 Implementação Completa para Catalog Service

### 1. Criar um Service de Autenticação

```java
@Service
public class AuthService {
    
    @Value("${user.service.url:http://user-service:8085}")
    private String userServiceUrl;
    
    @Autowired
    private RestTemplate restTemplate;
    
    public Optional<UserInfo> validateSession(String cookieHeader) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Cookie", cookieHeader);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<SessionResponse> response = restTemplate.exchange(
                userServiceUrl + "/api/auth/session",
                HttpMethod.GET,
                entity,
                SessionResponse.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                SessionResponse session = response.getBody();
                return Optional.of(session.getUser());
            }
        } catch (HttpClientErrorException.Unauthorized e) {
            // Sessão inválida
        }
        
        return Optional.empty();
    }
    
    public record SessionResponse(UserInfo user) {}
    public record UserInfo(String id, String email, String name, String role) {}
}
```

### 2. Criar um Interceptor/Filtro

```java
@Component
public class AuthInterceptor implements HandlerInterceptor {
    
    @Autowired
    private AuthService authService;
    
    @Override
    public boolean preHandle(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler
    ) throws Exception {
        
        // Ignorar endpoints públicos
        if (request.getRequestURI().startsWith("/api/products/public")) {
            return true;
        }
        
        String cookieHeader = request.getHeader("Cookie");
        if (cookieHeader == null) {
            response.setStatus(401);
            return false;
        }
        
        Optional<UserInfo> user = authService.validateSession(cookieHeader);
        if (user.isEmpty()) {
            response.setStatus(401);
            return false;
        }
        
        // Adicionar informações do usuário na requisição
        request.setAttribute("userId", user.get().id());
        request.setAttribute("userRole", user.get().role());
        
        return true;
    }
}
```

### 3. Registrar o Interceptor

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Autowired
    private AuthInterceptor authInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
            .addPathPatterns("/api/products/**")
            .excludePathPatterns("/api/products/public/**");
    }
}
```

### 4. Usar no Controller

```java
@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
        @Valid @RequestBody CreateProductRequest request,
        HttpServletRequest httpRequest
    ) {
        String userId = (String) httpRequest.getAttribute("userId");
        String userRole = (String) httpRequest.getAttribute("userRole");
        
        // Verificar se o usuário tem permissão (ex: apenas sellers podem criar)
        if (!"seller".equals(userRole) && !"admin".equals(userRole)) {
            return ResponseEntity.status(403).build();
        }
        
        // Criar produto associado ao userId
        Product product = service.createProduct(request, userId);
        return ResponseEntity.ok(ProductResponse.from(product));
    }
}
```

## 📋 Resumo dos Endpoints Disponíveis

### User Service (Better-Auth)

| Endpoint | Método | Descrição |
|----------|--------|-----------|
| `/api/auth/sign-up/email` | POST | Criar conta |
| `/api/auth/sign-in/email` | POST | Fazer login |
| `/api/auth/sign-out` | POST | Fazer logout |
| `/api/auth/session` | GET | Obter sessão atual |
| `/api/users/me` | GET | Obter informações do usuário |
| `/api/users/logout` | POST | Logout (wrapper) |

### Headers Necessários

Para verificar autenticação, o catalog service precisa **repassar os cookies** da requisição original:

```http
GET /api/auth/session HTTP/1.1
Host: user-service:8085
Cookie: better-auth.session_token=abc123...
```

## 🔒 Segurança

1. ✅ **Cookies HTTP-only**: Previne acesso via JavaScript
2. ✅ **HTTPS em produção**: Protege cookies em trânsito
3. ✅ **Validação de sessão**: Verifica se a sessão ainda é válida
4. ✅ **Timeout de sessão**: Sessões expiram automaticamente
5. ✅ **CORS configurado**: Apenas origens permitidas

## 🚀 Exemplo Completo: Frontend → Catalog Service

```typescript
// Frontend (Angular/React)
const authClient = createAuthClient({
  baseURL: "http://localhost:3000"
});

// Fazer login
await authClient.signIn.email({
  email: "user@example.com",
  password: "senha123"
});

// Criar produto (cookies são enviados automaticamente)
const response = await fetch("http://localhost:8080/api/products", {
  method: "POST",
  headers: {
    "Content-Type": "application/json",
  },
  credentials: "include", // ← Importante: inclui cookies
  body: JSON.stringify({
    name: "Produto",
    price: 99.99,
    // ...
  })
});
```

O catalog service recebe os cookies e valida com o user service!

