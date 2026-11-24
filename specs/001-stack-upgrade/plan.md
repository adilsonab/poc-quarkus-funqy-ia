# Implementation Plan: Migração segura da stack

**Branch**: `001-stack-upgrade` | **Date**: 2026-11-24 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `specs/001-stack-upgrade/spec.md`

## Summary

Atualizar a POC Java 21/Quarkus 3.33.4/PostgreSQL 17/Maven 3.9.16 para a
baseline aprovada Java 25 LTS/Quarkus 3.40.1.2 LTS/PostgreSQL 18.7/Maven
3.10.2, mantendo Funqy HTTP, o contrato Angular e o schema Liquibase. Usar o
Quarkus Agent MCP em dry-run para verificar a atualização, aplicar apenas a
mudança revisada de BOM, atualizar a imagem Java/Maven e o Maven Wrapper, e
criar um volume PostgreSQL 18 separado. A troca de major do banco é
documentada como dump/restore explícito, não como reuso do volume antigo.

## Technical Context

**Language/Version**: Java 25 LTS (Eclipse Temurin), compilação com
`maven.compiler.release=25`

**Primary Dependencies**: Quarkus BOM, plugin e extensões 3.40.1.2; Maven
Compiler Plugin 3.15.0; Surefire/Failsafe 3.5.4; Funqy HTTP; Liquibase;
PostgreSQL JDBC gerenciado pelo BOM

**Storage**: PostgreSQL 18.7, schema existente gerenciado pelo Liquibase; novo
volume nomeado `postgres-data-v18`

**Testing**: `./mvnw test`, `./mvnw package`, verificações do contrato Funqy
HTTP e ensaio manual documentado de dump/restore PostgreSQL 17→18

**Target Platform**: Dev Container Linux no VS Code; imagem Maven oficial com
Eclipse Temurin 25 baseada em Ubuntu Noble; PostgreSQL oficial Bookworm

**Project Type**: Backend Java Quarkus, uma aplicação

**Performance Goals**: Sem alteração de desempenho ou capacidade esperada
para a POC

**Constraints**: Preservar Funqy HTTP (preview), nomes e JSON de funções,
validação e falhas; não alterar changesets aplicados; não abrir o volume PG17
com PG18; não habilitar Docker socket; comandos Java e testes no Dev
Container; atualizar regras e skills relacionadas

**Scale/Scope**: Uma tabela pequena de tarefas e dois contratos Funqy; não
inclui dados de produção, deploy compartilhado, troca arquitetural para REST,
ou atualização do Specify CLI/Quarkus Agent MCP sem nova evidência oficial

## Constitution Check

| Gate | Status | Evidência / decisão |
|---|---|---|
| Contrato Funqy/Angular preservado | PASS | FR-002; contrato descrito em `contracts/funqy-http.md` |
| Validação e falhas reais preservadas | PASS | FR-003; testes existentes mantidos |
| Dados e histórico Liquibase protegidos | PASS | FR-004–FR-006; dump/restore e volume separado |
| Java e validações executados no Dev Container | PASS WITH ENVIRONMENT LIMIT | Processo definido; ambiente atual não tem Docker/Podman e roda Java 21, então não pode validar ainda |
| AS-IS histórico imutável | PASS | `.specify/memory/as-is.md` não será editado |
| Stack atualizada com evidência | PASS | Alvos confirmados em fontes oficiais; migração explicitamente solicitada e aprovada |
| Rules, skills, README e constituição alinhados | PASS | Atualizações previstas para os arquivos diretamente afetados |

Nenhuma violação de princípio requer exceção. O limite do ambiente é uma
limitação de validação, não uma permissão para afirmar sucesso.

## Research Summary

As decisões e fontes verificáveis estão em [research.md](./research.md).
Pontos centrais:

- Quarkus 3.40.1.2 é o patch LTS atual; Java 25 é suportado desde Quarkus
  3.31. O `quarkus:update` MCP em dry-run confirmou atualização de 3.33.4 para
  3.40.1.2 e extensões alinhadas ao BOM.
- O guia de atualização do Quarkus classifica o comando como experimental e
  exige leitura dos guias de migração e revisão manual do diff.
- Maven 3.10.2 é release estável; atualizar o Wrapper para distribuir essa
  versão e fixar o SHA-256 oficial. Manter wrapper scripts 3.3.4.
- PostgreSQL 18.7 é a versão minor escolhida; uma atualização de major exige
  dump/restore ou `pg_upgrade`. A imagem oficial 18 usa `PGDATA` em
  `/var/lib/postgresql/18/docker` e mount em `/var/lib/postgresql`.
- Docker/Podman não estão disponíveis no ambiente atual. O MCP não pôde
  iniciar a busca indexada de documentação, mas o dry-run do atualizador
  Quarkus funcionou. Não instalar ou expor um daemon apenas para contornar
  essa restrição.

## Project Structure

### Documentation (this feature)

```text
specs/001-stack-upgrade/
├── spec.md
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── funqy-http.md
├── checklists/
│   └── requirements.md
└── tasks.md
```

### Source Code (repository root)

```text
.devcontainer/
  Dockerfile
  compose.yaml
.mvn/wrapper/
  maven-wrapper.properties
.github/
  copilot-instructions.md
  skills/speckit-*/
.agents/skills/
  quarkus-funqy/SKILL.md
  liquibase-postgresql/SKILL.md
.specify/memory/
  as-is.md
  constitution.md
prompts/
  01-recriar-poc.md
  02-instalar-speckit.md
  03-migrar-stack.md
src/main/java/dev/poc/funqy/
src/main/resources/db/changelog/db.changelog-master.xml
src/test/java/dev/poc/funqy/TaskServiceTest.java
README.md
pom.xml
```

**Structure Decision**: Preservar a estrutura de backend existente. A
implementação altera os manifests de build/containers, orientações e testes
apenas se houver regressão confirmada; não criar uma nova camada REST nem um
changeset por mera mudança de major PostgreSQL.

## Design and Compatibility Decisions

1. **Quarkus update preview, then inspect**: usar o MCP Quarkus
   `quarkus_update` em dry-run, que confirmou o destino 3.40.1.2, e aplicar
   somente a mudança de BOM revisada. Não executar receitas OpenRewrite em
   modo de alteração enquanto o ambiente Java 25/Dev Container não puder ser
   iniciado. Revisar os guias 3.34–3.40; o updater é experimental e parcial.
2. **Build tool alignment**: declarar release Java 25, atualizar a imagem base
   para Maven 3.10.2/Eclipse Temurin 25 e configurar o Wrapper para Maven
   3.10.2 com SHA-256 verificado. Manter Wrapper 3.3.4 e plugins compilador/
   testes atuais, pois não há incompatibilidade demonstrada.
3. **Database major change**: usar `postgres:18.7-bookworm` com volume
   `postgres-data-v18:/var/lib/postgresql`; manter credenciais fictícias de
   desenvolvimento. Fazer backup lógico do banco PG17 e restaurá-lo no banco
   PG18 antes de iniciar a aplicação. O volume anterior não deve ser anexado
   nem removido.
4. **No schema change**: Liquibase mantém o changelog atual; revisar o
   histórico restaurado e criar changeset apenas se uma necessidade de schema
   for comprovada.
5. **Compatibility**: conservar Funqy HTTP preview, rotas JSON, CORS e regras
   de validação. A migração de dependências é verificada pela suíte de testes e
   pelo contrato existente.
6. **Tooling**: conservar Quarkus Agent MCP 1.2.15 e Spec Kit 1.4.4; nenhum
   release posterior foi aprovado para esta mudança. Registrar que a busca de
   docs via MCP requer um banco Docker/Podman indisponível neste ambiente.

## Validation and Operational Notes

- Antes de aplicar/recriar o serviço Compose em um clone com PostgreSQL 17,
  fazer e copiar o dump para o host; capturar uma listagem de tarefas para
  comparação. Os comandos completos estão em [quickstart.md](./quickstart.md)
  e no README.
- Não usar `docker compose down -v`, não apagar volumes e não iniciar a
  aplicação antes de restaurar o dump.
- Reconstruir/Reabrir o Dev Container para obter Java 25 e PostgreSQL 18.7.
- Executar Wrapper `-version`, `./mvnw test` e `./mvnw package` dentro do
  container; testar ambas as funções HTTP e conferir a comparação do backup.
- No ambiente atual não foi possível reconstruir containers nem rodar
  validação Java; o relatório deve manter esse status até execução pelo usuário
  em Dev Container atualizado.

## Complexity Tracking

Não há violações da constituição ou novas camadas de arquitetura que precisem
de justificativa adicional.
