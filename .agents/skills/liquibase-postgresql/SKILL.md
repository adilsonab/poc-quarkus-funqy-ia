---
name: liquibase-postgresql
description: Manage PostgreSQL schema changes in this Quarkus project with safe, versioned Liquibase changelogs.
---

# Liquibase and PostgreSQL

Use this skill when changing persisted data, PostgreSQL configuration, or
database startup and migration behavior.

## Project constraints

- The local development database uses PostgreSQL 18.7 in the
  `postgres-data-v18` volume mounted at `/var/lib/postgresql`.
- PostgreSQL 17 data directories are not compatible with PostgreSQL 18. Before
  recreating the service for a major upgrade, make and verify a logical
  dump/restore; keep the prior volume intact and never use `docker compose
  down -v`.
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
3. For an existing PG17 development volume, follow the README backup/restore
   procedure before rebuilding the Dev Container. Do not start Quarkus until
   the dump has been restored to the new PG18 volume.
4. Start the PostgreSQL service and let Quarkus run Liquibase at startup.
5. Verify startup, the Liquibase history, restored tasks, and affected
   repository tests.
6. Run `./mvnw test` and `./mvnw package` inside the Dev Container.

Consult the official [Liquibase guide](https://quarkus.io/guides/liquibase)
and [PostgreSQL versioning policy](https://www.postgresql.org/support/versioning/)
for migration and support details.
