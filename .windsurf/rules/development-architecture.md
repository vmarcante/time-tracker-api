---
trigger: always_on
---
# Development Architecture Rules

## Context

Java 21 + Spring Boot 4 project using **Hexagonal Architecture / Clean Architecture / Ports and Adapters**.

This project separates business rules from frameworks, infrastructure, and delivery mechanisms.

## Layered Architecture

The project has four layers, ordered from innermost (most stable) to outermost (most volatile):

```text
Domain
  ↑
Application
  ↑
Infrastructure
  ↑
Interfaces (Presentation)
```

### Dependency rules

- **Domain** depends on nothing. No Spring, no Jakarta, no framework.
- **Application** depends only on **Domain**.
- **Infrastructure** depends on **Domain** and **Application**. It implements the ports they define.
- **Interfaces** depends only on **Application**. It calls use cases through `Port/In` interfaces.
- **Shared** contains utilities usable by any layer, but must not violate dependency rules.

## Package Structure

```text
com.vmarcante.grao_grao_digital
├── domain/
│   └── [context]/
│       ├── model/          # Entities with identity and lifecycle
│       ├── vo/             # Value objects (immutable, no identity)
│       ├── event/          # Domain events
│       ├── repository/     # Repository interfaces (ports)
│       └── service/        # Domain services (preferably interfaces)
│   └── exception/          # Domain-specific exceptions
├── application/
│   └── [context]/
│       ├── port/
│       │   ├── in/         # Input ports (use case interfaces)
│       │   └── out/        # Output ports (driven interfaces)
│       ├── use_cases/      # Concrete use case implementations
│       ├── dto/            # Use case request/response DTOs
│       ├── mapper/         # DTO ↔ Domain mappers
│       ├── assembler/      # Complex object assembly
│       ├── applier/        # Apply DTOs to entities (PATCH/PUT)
│       ├── policy/         # Stateless reusable business rules
│       └── handler/        # Event/action handlers
├── infrastructure/
│   └── [context]/
│       ├── persistence/    # Repository adapters
│       ├── mapper/         # Persistence ↔ Domain mappers
│       ├── integration/    # External API clients and adapters
│       ├── security/       # Auth adapters
│       └── config/         # Framework configurations
└── interfaces/
    └── [context]/
        ├── controllers/    # REST, GraphQL, CLI, etc.
        ├── dtos/           # API payload DTOs (may differ from application DTOs)
        └── exception/      # GlobalExceptionHandler
```

## Naming Conventions

### Classes that represent behavior

Use a verb followed by the business context.

```text
<Verbo><Contexto><Sufixo>

InserirProdutoUseCase
BuscarUsuarioPorIdUseCase
AtualizarPerfilPolicy
GarantirConfiguracaoValidaOrquestrador
```

### CRUD prefixes

| Prefix | Operation | Example |
|--------|-----------|---------|
| `Inserir*` | Create | `InserirProdutoUseCase` |
| `Atualizar*` | Update | `AtualizarProdutoUseCase` |
| `Remover*` | Delete | `RemoverUsuarioUseCase` |
| `Upsert*` | Insert or Update | `UpsertConfiguracaoUseCase` |
| `Buscar*` | Read | `BuscarUsuarioPorIdUseCase` |
| `Garantir*` | Ensure | `GarantirLimiteCreditoValidoPolicy` |

### Adapters

Always use `*Adapter` instead of `*Impl` for infrastructure implementations of ports.

```text
EmailNotificacaoAdapter       → implements NotificarClientePort
ProdutoJpaRepositoryAdapter   → implements ProdutoRepository
PagamentoGatewayAdapter       → implements ProcessarPagamentoPort
```

### Events and handlers

```text
PedidoCriadoEvent             → domain event
PedidoCriadoEventHandler      → reacts to event
NotificarClienteEventHandler  → reacts to event
```

### Semantic separation: Command vs Query

- `Buscar*` → Query: no side-effects, potentially cacheable.
- `Inserir*`, `Atualizar*`, `Remover*`, `Upsert*` → Command: mutates state.

## Dependency Injection

- Always use **constructor injection**.
- Never use field injection (`@Autowired` on fields).

```java
@Service
public class InserirPedidoUseCaseImpl implements InserirPedidoUseCase {
    private final PedidoRepository pedidoRepository;

    public InserirPedidoUseCaseImpl(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }
}
```

## Transactional Scope

- `@Transactional` belongs to the **use case** (application layer), not the repository.
- The use case defines the transactional boundary.

```java
// Correct
@Service
public class InserirPedidoUseCaseImpl implements InserirPedidoUseCase {
    @Transactional
    public InserirPedidoResponse execute(InserirPedidoRequest request) { ... }
}
```

```java
// Wrong
@Repository
public class PedidoRepositoryAdapter implements PedidoRepository {
    @Transactional
    public void save(Pedido pedido) { ... }
}
```

## Domain Services vs Application Policies

| Aspect | Domain Service | Application Policy |
|--------|---------------|---------------------|
| Location | `domain/[context]/service/` | `application/[context]/policy/` |
| Scope | Business contract; part of domain model | Rule applied inside use cases |
| Reuse | Via domain interfaces | Injected directly into use cases |
| State | Preferably stateless | Always stateless |
| Repositories | May use domain ports | Never accesses repositories directly |

## Anti-patterns

### Business logic in controller

Controllers must only delegate to use cases. No validation, no repository calls, no domain logic.

```java
// Wrong
@PostMapping("/pedidos")
public ResponseEntity<?> criarPedido(@RequestBody PedidoRequest request) {
    if (request.getValor() <= 0) throw new IllegalArgumentException("Valor invalido");
    // ...
}

// Correct
@PostMapping("/pedidos")
public ResponseEntity<PedidoResponse> criarPedido(@RequestBody PedidoRequest request) {
    PedidoResponse response = inserirPedidoUseCase.execute(mapper.toRequest(request));
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}
```

### Exposing domain entity as API DTO

Use cases return DTOs. Controllers never expose domain entities.

```java
// Wrong
@GetMapping("/pedidos/{id}")
public ResponseEntity<Pedido> buscarPedido(@PathVariable Long id) { ... }

// Correct
@GetMapping("/pedidos/{id}")
public ResponseEntity<BuscarPedidoResponse> buscarPedido(@PathVariable Long id) { ... }
```

### Use case calling another use case

Use cases must not depend on other use cases. Use the domain port directly.

```java
// Wrong
public class AtualizarPedidoUseCaseImpl {
    private final BuscarPedidoUseCaseImpl buscarPedidoUseCase;
}

// Correct
public class AtualizarPedidoUseCaseImpl {
    private final PedidoRepository pedidoRepository;
}
```

### Policy accessing repository

Policies must be stateless and receive resolved data, not fetch it.

```java
// Wrong
public class VerificarLimiteCreditoPolicy {
    private final ClienteRepository clienteRepository; // not allowed here
}

// Correct
public class VerificarLimiteCreditoPolicy {
    public boolean execute(BigDecimal limiteDisponivel, BigDecimal valorSolicitado) { ... }
}
```

### Static classes

Static classes or records should be avoided. Use normal classes definition on its own files, even if it's a simple class with only one method or property.

```java
// Wrong
public class AnotherClass {

    public InnerClass calculate(int a, int b) {
        return new InnerClass(a + b);
    }

    public static class InnerResultClassDTO {
        public Integer result;
        
        public InnerResultClassDTO(Integer result) {
            this.result = result;
        }
    }
}

// Correct
import ExternalResultClassDTO;

public class AnotherClass {
    public ExternalResultClassDTO calculate(int a, int b) {
        return new ExternalResultClassDTO(a + b);
    }
}

public class ExternalResultClassDTO {
    public Integer result;
    
    public ExternalResultClassDTO(Integer result) {
        this.result = result;
    }
}
```

## Recommended Creation Order

1. Domain Models and Exceptions
2. Domain Value Objects
3. Domain Events
4. Domain Repository interfaces (ports)
5. Domain Service interfaces
6. Application Use Cases
   - Define Port/In
   - Define Port/Out if needed
   - Define Request/Response DTOs
   - Implement use case
   - Create mappers
   - Create assemblers/appliers if needed
7. Infrastructure
   - Repository adapters
   - External integration adapters
8. Interfaces / Presentation
   - Controllers, API DTOs, GlobalExceptionHandler
9. Shared
   - Utilities and constants
10. Tests
    - Unit tests for domain, policies, mappers, assemblers, services
    - Integration tests for repositories with Testcontainers
    - Slice tests for controllers with `@WebMvcTest`
    - Architecture tests with ArchUnit

## Architecture Tests with ArchUnit

Use ArchUnit to enforce dependency rules automatically.

```java
@AnalyzeClasses(packages = "com.vmarcante.grao_grao_digital")
class ArchitectureTest {

    @ArchTest
    static final ArchRule domainShouldNotDependOnOtherLayers =
        classes().that().resideInAPackage("..domain..")
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage("..domain..", "java..", "jakarta..");

    @ArchTest
    static final ArchRule applicationShouldNotDependOnInfrastructure =
        classes().that().resideInAPackage("..application..")
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage("..application..", "..domain..", "java..", "jakarta..");
}
```

## General Best Practices

- Follow SOLID principles.
- Keep domain independent of frameworks.
- Respect layer boundaries.
- Prefer constructor injection.
- Use domain events to decouple side-effects.
- Document APIs with OpenAPI/Swagger.
- Never use `@Transactional` in repositories.
- Never expose domain entities in API responses.
- Never make one use case call another use case directly.
- Keep policies stateless and free of I/O.
- Never use `var` in tests.
- Never use `var` in production code.
- Never use inline import, always use import statements on top of the file.
