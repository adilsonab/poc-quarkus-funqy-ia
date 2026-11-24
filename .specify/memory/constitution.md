# POC Quarkus Funqy IA Constitution

## Core Principles

### I. Preserve the Function Contract

Keep the `listTasks` and `createTask` names, payloads, and JSON-compatible
outputs aligned with the Angular client. Keep HTTP-independent business rules
in services. Do not replace Funqy HTTP with REST unless the user explicitly
approves that architectural change.

### II. Validate Inputs and Surface Failures

Reject invalid input explicitly and propagate persistence failures. Never
return empty or success-shaped fallback data to conceal an error. Add focused
tests for changed behavior and boundary conditions.

### III. Evolve the Database Safely

Use Liquibase for schema evolution. Never edit a changeset that may already
have run; add a new changeset and preserve existing data. Include rollback
behavior when it is safe and meaningful.

### IV. Verify Changes with Evidence

Run relevant tests and report their actual results. For Java changes, run
`./mvnw test` and `./mvnw package` inside the Dev Container. Do not claim a
build, test, migration, or compatibility check passed unless it was executed.

### V. Keep Changes Secure and Reviewable

Use fictional local-only credentials, never share secrets, and do not enable
broad host capabilities such as the Docker socket without explicit need and
approval. Make scoped changes, preserve unrelated work, and document material
compatibility risks.

## Technology and Security Constraints

- The current baseline is Java 21, Quarkus 3.33.4, Funqy HTTP, PostgreSQL 17,
  Liquibase, and the Maven Wrapper.
- Funqy HTTP is an intentional preview choice in this POC. Preserve it and
  document compatibility limitations. A user-approved migration may revise
  the baseline, but must first verify supported versions and compatibility
  against official sources and retain the function contract unless separately
  approved.
- Run Java commands in the Dev Container. PostgreSQL Compose credentials and
  ports are for local development only.
- Keep rules, this constitution, skills, README, and implementation aligned.
  Update directly affected guidance when an approved migration changes a
  compatibility constraint.

## Development Workflow and Quality Gates

- Inspect existing code, tests, client-facing contracts, and relevant
  migrations before changing behavior.
- Use the Funqy and Liquibase/PostgreSQL skills for their respective areas;
  review and authorize MCP tool use before invoking tools.
- Use Spec Kit artifacts to define bounded work before implementation. Review
  the specification, plan, and tasks for contract compatibility and migration
  risk.
- Treat `.specify/memory/as-is.md` as a historical snapshot. Create TO-BE
  migration artifacts separately under `specs/`; never rewrite a prior AS-IS
  snapshot to match the migration destination. Record a new dated snapshot
  when a later baseline needs to be captured.
- Add or update focused tests for behavior changes. For Java changes, execute
  `./mvnw test` and `./mvnw package` inside the Dev Container.
- Review the final diff and state any unverified assumptions or limitations.

## Governance

This constitution reinforces, and does not silently replace, the project
instructions in `.github/copilot-instructions.md` or the procedures in
`.agents/skills/`. If guidance conflicts, surface the conflict and resolve it
explicitly; do not weaken existing constraints to make a plan pass. Amend this
constitution when an approved architectural or technology decision changes
its principles, and update directly related guidance in the same change.
Review plans and implementation diffs against these principles.

**Version**: 1.1.0 | **Ratified**: 2026-11-23 | **Last Amended**: 2026-11-24
