# Tasks: Migração segura da stack

**Input**: Design documents from `specs/001-stack-upgrade/`

**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`,
`contracts/funqy-http.md`, `quickstart.md`

**Organization**: Tasks are grouped by user story so migration, database
preservation, and learning documentation can be reviewed independently.

## Phase 1: Setup

**Purpose**: Protect any local export before creating database backup files.

- [X] T001 Add local backup ignore rules to `.gitignore` and `.dockerignore` before generating any PostgreSQL dump or task CSV exports
- [ ] T002 Create a custom-format PostgreSQL 17 backup and pre-migration task export using the host Docker Compose commands in `specs/001-stack-upgrade/quickstart.md`, before applying/recreating the database service from `.devcontainer/compose.yaml` (blocked here: host Docker unavailable)

## Phase 2: Foundational

**Purpose**: Confirm that rollback data exists before implementation begins.

- [ ] T003 Confirm both PG17 backup files are present and non-empty, and verify that the original `postgres-data` volume remains untouched before applying `.devcontainer/compose.yaml` (depends on T002; blocked here: host Docker unavailable)

**Checkpoint**: The old PostgreSQL volume and a verified logical backup are
available; user-story work may proceed.

## Phase 3: User Story 1 - Usar a aplicação na stack atualizada (Priority: P1)

**Goal**: Upgrade Java, Quarkus, and Maven while preserving the existing
Funqy task behavior.

**Independent Test**: In a rebuilt Java 25 Dev Container, confirm Maven
3.10.2, run `./mvnw test` and `./mvnw package`, and retain the HTTP/persistence
contract in `specs/001-stack-upgrade/contracts/funqy-http.md`.

### Implementation for User Story 1

- [X] T004 Apply the reviewed BOM version change confirmed by the Quarkus Agent MCP dry-run and confirm Quarkus BOM, plugin, and extensions resolve to 3.40.1.2 in `pom.xml`; review the result against `specs/001-stack-upgrade/research.md`
- [X] T005 Set `maven.compiler.release` to 25 in `pom.xml` and use the verified Maven 3.10.2 Eclipse Temurin 25 Noble image in `.devcontainer/Dockerfile`
- [X] T006 Set the Maven Wrapper distribution URL to Maven 3.10.2 and the verified SHA-256 while retaining Wrapper 3.3.4 in `.mvn/wrapper/maven-wrapper.properties`
- [ ] T007 Run the existing persistence and Funqy HTTP contract checks in `src/test/java/dev/poc/funqy/TaskServiceTest.java` with `./mvnw test` and `./mvnw package` inside the rebuilt Dev Container; change tests only if a real compatibility regression is found (blocked here: Java 25 Dev Container unavailable)

**Checkpoint**: Application and test suite work on the target Java/Quarkus
baseline without changing the externally visible function contract.

## Phase 4: User Story 2 - Migrar o banco local sem perder tarefas (Priority: P1)

**Goal**: Start PostgreSQL 18 on an isolated volume, restore the PG17 backup,
and verify the existing task data.

**Independent Test**: Restore the verified dump before starting Quarkus and
compare pre/post exports for `Task.id`, `Task.title`, and `Task.created_at` as
defined in `specs/001-stack-upgrade/data-model.md`.

### Implementation for User Story 2

- [X] T008 Configure the local database for `postgres:18.7-bookworm`, the PostgreSQL 18 mount `/var/lib/postgresql`, and the new `postgres-data-v18` volume in `.devcontainer/compose.yaml`; do not recreate the service until T003 is complete
- [ ] T009 Restore the verified dump into the new PostgreSQL 18 database and compare `Task.id`, `Task.title`, and `Task.created_at` according to their definitions in `specs/001-stack-upgrade/data-model.md`, using the host commands in `specs/001-stack-upgrade/quickstart.md` (blocked here: no Docker/Podman runtime or PG17 backup)
- [ ] T010 Start the application only after restore, confirm Liquibase recognizes the restored changelog, and verify both routes in `specs/001-stack-upgrade/contracts/funqy-http.md` from the Dev Container (blocked here: no Docker/Podman runtime)

**Checkpoint**: The app uses PG18 and all tasks from the prior PG17 database
remain readable; the old volume remains available as a local fallback.

## Phase 5: User Story 3 - Aprender e repetir os três prompts (Priority: P2)

**Goal**: Explain the three-prompt learning flow and the target migration in
plain Portuguese without rewriting the historic AS-IS.

**Independent Test**: Follow the README and confirm it presents all three
prompts in order, their starting state, action, and review result; confirm the
AS-IS snapshot is unchanged.

### Implementation for User Story 3

- [X] T011 Update the stack summary, three-prompt walkthrough, approved target versions, PostgreSQL 17-to-18 backup/restore procedure, limitations, and validation status in `README.md`
- [X] T012 Update the migration prompt to require the verified PostgreSQL backup before Compose changes and retain the Spec Kit, Rules, Skills, MCP, and Funqy contract safeguards in `prompts/03-migrar-stack.md`
- [X] T013 [P] Align the project technology baseline and data-migration safeguards in `.github/copilot-instructions.md` and `.specify/memory/constitution.md`
- [X] T014 [P] Align the Java/Quarkus baseline and verification steps in `.agents/skills/quarkus-funqy/SKILL.md`
- [X] T015 [P] Align the PostgreSQL 18 volume, backup/restore, Liquibase, and local-credentials guidance in `.agents/skills/liquibase-postgresql/SKILL.md`
- [X] T016 Verify `.specify/memory/as-is.md` is byte-for-byte unchanged and that links in `README.md` point to the original snapshot and the new TO-BE artifacts

**Checkpoint**: A newcomer can explain the purpose and order of all three
prompts, and the original AS-IS remains a historical record.

## Phase 6: Polish & Cross-Cutting Validation

**Purpose**: Verify implementation scope, instructions, and honest reporting.

- [X] T017 Run `git -c core.whitespace=cr-at-eol diff --check`, review the complete changes against `.specify/memory/constitution.md`, and record which Dev Container tests or database migration checks remain blocked in `specs/001-stack-upgrade/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: T001 must complete before T002 creates local database
  files. T002 must complete before the new database service is applied.
- **Foundational (Phase 2)**: T003 depends on T001–T002 and blocks
  PostgreSQL service recreation, not the declarative Compose-file edit.
- **User Story 1 (Phase 3)**: Independent of T001–T003 because it changes the
  Java/Quarkus/Maven build, not database data or its volume.
- **User Story 2 (Phase 4)**: T008 can be prepared after T002; applying it
  requires T003. T009 depends on T003, T008, and T002; T010 depends on T009.
- **User Story 3 (Phase 5)**: Documentation can proceed after the researched
  target baseline is fixed. T011 and T012 have distinct files; T013–T015 have
  distinct files and can run in parallel.
- **Polish (Phase 6)**: Depends on completion of implementation tasks.

### User Story Dependencies

- **US1 (P1)**: Independent of PostgreSQL backup/restore tasks T001–T003 and
  US2's database service recreation.
- **US2 (P1)**: Depends on T001–T003 and its ordered image/restore tasks; no
  dependency on changes to the task API.
- **US3 (P2)**: Depends on the approved baseline and feature design documents;
  can be developed alongside US1/US2 after backup instructions are finalized.

### Parallel Opportunities

- T004 (Quarkus update) is independent of T001–T003. T008 may be prepared
  separately, but applying/recreating its database service must wait for T003;
  the actual restore T009 also requires the new image.
- T013, T014, and T015 touch distinct instruction/skill files and can run in
  parallel.
- T011 and T012 touch distinct documentation files and can run in parallel
  after the exact restore procedure in `quickstart.md` is settled.

## Parallel Example: Documentation Alignment

```text
Task T013: Align project rules and constitution in .github/copilot-instructions.md and .specify/memory/constitution.md
Task T014: Align Funqy skill in .agents/skills/quarkus-funqy/SKILL.md
Task T015: Align database skill in .agents/skills/liquibase-postgresql/SKILL.md
```

## Implementation Strategy

### MVP First

1. Complete T001–T003: ignore, back up, and verify PostgreSQL 17 data.
2. Complete T004–T007: upgrade the app build/runtime and run its tests.
3. **Stop and validate US1** before rebuilding the database service.

### Incremental Delivery

1. Protect PG17 data before editing the Compose volume.
2. Upgrade Java/Quarkus/Maven and preserve the function contract.
3. Move PostgreSQL to its new volume, restore, and compare task exports.
4. Update learning documentation and all directly related guidance.
5. Run the final diff review and report any validation blocked by the
   environment.

## Notes

- Every task uses the required checkbox, sequential ID, optional `[P]`, story
  label for user-story work, and an exact file path.
- No new behavior test is required unless the existing suite exposes a
  compatibility regression; the existing `TaskServiceTest` remains the
  contract evidence.
- The PostgreSQL restore is an operational quickstart verification, not a
  Liquibase schema change.
- T002–T003 and T007/T009–T010 remain unchecked because this environment has no
  Docker/Podman runtime and only Java 21; run them after opening the Java 25
  Dev Container and before using the PostgreSQL 18 service.
