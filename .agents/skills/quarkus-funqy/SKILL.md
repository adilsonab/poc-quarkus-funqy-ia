---
name: quarkus-funqy
description: Implement and validate Quarkus Funqy functions exposed over HTTP in this Java 21 and Quarkus 3.33.4 migration POC.
---

# Quarkus Funqy

Use this skill when adding or changing Funqy functions, their input/output
types, persistence calls, or the Angular-facing HTTP contract.

## Project constraints

- Keep Java 21 and Quarkus 3.33.4 unless a migration is explicitly requested.
- Use `quarkus-funqy-http` and the `@Funq` API. Funqy and its HTTP binding are
  preview features; preserve this as an intentional POC choice.
- Keep function methods portable: put HTTP-independent business rules and
  persistence in services rather than depending on HTTP request APIs.
- Use JSON-compatible Java bean types with a no-argument constructor for
  Funqy input and output.
- Keep the `listTasks` and `createTask` names and payload shape aligned with
  the Angular client unless both sides are changed together.
- Validate inputs and surface database failures; do not silently substitute
  empty lists or success-shaped responses.

## Workflow

1. Read the existing function, service, DTOs, and relevant tests before editing.
2. Use the existing datasource and Liquibase schema; avoid introducing a
   second persistence mechanism.
3. Add focused tests for the changed function behavior or persistence path.
4. Run `./mvnw test` and `./mvnw package` inside the Dev Container.
5. For HTTP behavior changes, verify the function route and JSON contract
   against the running service.

Consult the official [Funqy basics](https://quarkus.io/guides/funqy) and
[Funqy HTTP binding](https://quarkus.io/guides/funqy-http) guides when APIs
or compatibility details are uncertain.
