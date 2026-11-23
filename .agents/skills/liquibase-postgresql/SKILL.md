---
name: liquibase-postgresql
description: Manage PostgreSQL schema changes in this Quarkus project with safe, versioned Liquibase changelogs.
---

# Liquibase and PostgreSQL

Use this skill when changing persisted data, PostgreSQL configuration, or
database startup and migration behavior.

## Project constraints

- The local development database uses PostgreSQL 17, pinned to the 17 major
  line to support a later major-version migration exercise.
- Use Liquibase changesets under `src/main/resources/db/changelog/`.
- Never edit a changeset that may already have run; add a new changeset for
  each schema evolution.
- Keep PostgreSQL credentials local to the development compose configuration;
  never add real credentials or production values.
- Keep schema operations reversible where practical and preserve existing
  task data during upgrades.

## Workflow

1. Inspect the current changelog and persistence queries before changing the
   schema.
2. Add a new changeset with a unique id and author; include a rollback when
   it is safe and meaningful.
3. Start the PostgreSQL service and let Quarkus run Liquibase at startup.
4. Verify startup, the Liquibase history, and affected repository tests.
5. Run `./mvnw test` and `./mvnw package` inside the Dev Container.

Consult the official [Liquibase guide](https://quarkus.io/guides/liquibase)
and [PostgreSQL versioning policy](https://www.postgresql.org/support/versioning/)
for migration and support details.
