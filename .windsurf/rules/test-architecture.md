---
trigger: always_on
---
# Test Architecture Rules

## Context

Java 21 + Spring Boot 4 project using Hexagonal / Clean Architecture.

This project values real automated tests over coverage checklists. Tests must be specifications of behavior, not implementations.

## Rules for Writing Tests

### 1. Test pyramid

- Write mostly **unit tests** (fast, no Spring context).
- Write **integration tests** for repositories, clients, and adapters using Testcontainers.
- Write **contract / slice tests** for controllers using `@WebMvcTest` or `MockMvc`.
- Write very few **E2E tests** for critical flows.

### 2. Layer alignment

| Layer | Test type | Allowed dependencies |
|-------|-----------|----------------------|
| Domain | Pure unit | None. Only Java + test libs. |
| Application | Unit with mocked ports | Domain + port interfaces |
| Infrastructure | Unit + Integration | Domain + Application + frameworks |
| Interfaces | Slice / E2E | Application + API DTOs |

### 3. Naming

- Class name: `[ClassUnderTest]Test`.
- Method name: `given<State>_when<Action>_then<ExpectedOutcome>` or `should<ExpectedOutcome>When<Condition>`.
- Align with project verbs: `Inserir*`, `Buscar*`, `Atualizar*`, `Remover*`, `Garantir*`.

### 4. Variable declarations

**Never use `var` in tests.** Always use the real type.

```java
// Wrong
var input = CreateUserInputDTOBuilder.valid().build();

// Correct
CreateUserInputDTO input = CreateUserInputDTOBuilder.valid().build();
```

### 5. AAA pattern

Every test must have three visual blocks separated by blank lines:

```java
@Test
void givenValidInput_whenExecuting_thenShouldSaveUser() {
    // Arrange
    CreateUserInputDTO input = CreateUserInputDTOBuilder.valid().build();
    UserRepository repository = mock(UserRepository.class);
    when(repository.existsByEmail(any())).thenReturn(false);

    CreateUserUseCase useCase = new CreateUserUseCase(repository, passwordHasher);

    // Act
    User result = useCase.execute(input);

    // Assert
    assertThat(result).isNotNull();
    verify(repository).save(any(User.class));
}
```

### 6. Mocks and ports

- Mock **ports (interfaces)**, never adapters (implementations).
- Do not use `@MockBean` in domain tests.
- Do not use `Thread.sleep` or `System.out.println`.
- Use `verify` only for meaningful interactions.

### 7. Builders and fixtures

- Place shared builders in `src/test/java/.../shared/builders/`.
- Each builder must have a `valid()` method that returns a fully valid object.
- Provide semantic modifiers like `underage()`, `withInvalidEmail()`, `pendingConfirmation()`.

### 8. Command vs Query

- **Command tests**: verify side-effects (`save`, `delete`, events published).
- **Query tests**: verify returned data only. Never verify `save` or event publishing in query tests.

### 9. Architecture tests

- Use ArchUnit to enforce layer dependency rules.
- Domain must not depend on Application, Infrastructure, or Interfaces.
- Application must not depend on Infrastructure or Interfaces.
- Controllers must only depend on Application.

## Anti-patterns to Avoid

| Anti-pattern | Solution |
|--------------|----------|
| Testing getters/setters | Do not test |
| Generic test names (`test1`, `test2`) | Use GWT or Should_When |
| Loading Spring context for domain tests | Use plain Java |
| Mocking adapters instead of ports | Mock the port interface |
| Testing `@Transactional` in repositories | Test in the use case |
| Using real database in unit tests | Use fake or mock |
| Query tests verifying `save()` | Verify only return data |
| Importing classes inline | Use import statements on top of the file |
| Using `var` in tests | Use explicit types |

## Dependencies to Use

- JUnit 5
- AssertJ
- Mockito
- Testcontainers (PostgreSQL, Redis, SMTP when needed)
- ArchUnit (architecture tests)
- Awaitility (async assertions, never `Thread.sleep`)

## Before Committing

Even as a solo developer, run this short checklist before every commit to avoid pushing untested or low-quality code.

- [ ] New business logic has unit tests.
- [ ] New repository or external client has integration tests.
- [ ] New critical endpoint has contract tests.
- [ ] Tests follow AAA.
- [ ] Variables use explicit types (no `var`).
- [ ] Builders are in `shared/builders/`.
- [ ] Command and query tests are separated.
- [ ] ArchUnit tests pass.
- [ ] `./mvnw clean test` passes locally.

When a team or Pull Request workflow exists, adapt this to "Before submitting PR".
