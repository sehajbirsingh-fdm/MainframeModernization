# quickstart.md - Build and Demo Quickstart

## Prerequisites
- Java 21
- Maven
- VS Code
- GitHub Copilot

## Implementation with Copilot
1. Open repository in VS Code.
2. Review `.github/copilot-instructions.md`.
3. Open `specs/001-inqcust-customer-inquiry-modernization/spec.md`.
4. Use the prompts in `prompts/` in order.
5. Validate generated code against `supporting/copilot-quality-checklist.md`.

## Run tests

```bash
mvn clean test
```

## Run service

```bash
mvn spring-boot:run
```

## Demo API calls

Specific customer:

```bash
curl http://localhost:8080/api/v1/customers/123456/0000000001
```

Latest customer:

```bash
curl http://localhost:8080/api/v1/customers/123456/9999999999
```

Random customer:

```bash
curl http://localhost:8080/api/v1/customers/123456/0000000000
```

Not found:

```bash
curl http://localhost:8080/api/v1/customers/123456/0000009999
```

Invalid request:

```bash
curl http://localhost:8080/api/v1/customers/ABCDEF/0000000001
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```
