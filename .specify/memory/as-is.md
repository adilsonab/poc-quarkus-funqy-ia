# AS-IS: Stack da POC Quarkus Funqy IA

Este documento registra a baseline observada antes da próxima migração. Ele é
uma fotografia de referência, não uma especificação retroativa de todas as
funcionalidades do sistema nem uma proposta TO-BE.

## Metadados da captura

- Capturado em: 2026-11-24
- Revisão de origem: `76c416e` (`feat: initialize Quarkus Funqy POC with Spec Kit`)
- Specify CLI usado para inicializar este projeto: 1.4.4
- Integração Spec Kit: GitHub Copilot, modo skills, scripts Bash
- Evidência principal: arquivos versionados nesta revisão. Versões observadas
  em runtime referem-se somente ao ambiente disponível durante a captura.

## Stack e versões observadas

| Componente | AS-IS | Evidência |
| --- | --- | --- |
| Java | Release de compilação 21; runtime observado Eclipse Temurin 21.0.12.1 | `pom.xml`; `.devcontainer/Dockerfile`; `java -version` durante a captura |
| Quarkus | BOM e plugins Quarkus 3.33.4 | `pom.xml` |
| Funqy | Extensão `quarkus-funqy-http` gerenciada pelo BOM Quarkus 3.33.4 | `pom.xml`; `src/main/java/dev/poc/funqy/TaskFunctions.java` |
| Maven | Wrapper Apache Maven 3.9.16; Maven Wrapper 3.3.4 | `.mvn/wrapper/maven-wrapper.properties`; `./mvnw -version` durante a captura |
| PostgreSQL | Imagem `postgres:17-bookworm`; major 17, sem patch de imagem fixado | `.devcontainer/compose.yaml` |
| Liquibase | Extensão `quarkus-liquibase`, gerenciada pelo BOM Quarkus | `pom.xml`; `src/main/resources/application.properties` |
| Quarkus Agent MCP | Runner 1.2.15 com SHA-256 fixado na imagem | `.devcontainer/Dockerfile`; `.vscode/mcp.json` |
| Spec Kit | Specify CLI 1.4.4, integração Copilot em modo skills | `.specify/init-options.json`; `.specify/integration.json` |

O build configura também `maven-compiler-plugin` 3.15.0 e Surefire/Failsafe
3.5.4. O compilador Java usa `release` 21. O projeto não fixa versões
individuais para Liquibase nem para o driver JDBC; elas são gerenciadas pelo
BOM Quarkus. Não trate versões transitivas observadas em `target/` como
declaração independente da baseline.

## Aplicação e comportamento existentes

- Backend Java com duas funções Funqy HTTP: `listTasks` e `createTask`.
- `listTasks` retorna tarefas em ordem de id. `createTask` recebe `{ "title":
  "..." }`, valida o título e persiste no PostgreSQL.
- O schema inicial de `tasks` é criado por Liquibase. A evolução deve usar
  novos changesets e preservar os changesets já aplicados.
- CORS está limitado às origens locais `http://localhost:4200` e
  `http://127.0.0.1:4200`.
- Não há chamada a LLM em runtime. Funqy HTTP é uma escolha intencional da POC
  e está em preview segundo a documentação registrada no README.

Evidências: `src/main/java/dev/poc/funqy/`, `src/main/resources/` e
`src/test/java/dev/poc/funqy/TaskServiceTest.java`.

## Harness e execução local

- Rules: `.github/copilot-instructions.md`.
- Agent Skills do projeto: `.agents/skills/quarkus-funqy/SKILL.md` e
  `.agents/skills/liquibase-postgresql/SKILL.md`.
- Spec Kit skills: `.github/skills/speckit-*/SKILL.md`; scaffold, templates,
  scripts e constituição: `.specify/`.
- MCP: servidor `quarkus-agent` configurado em `.vscode/mcp.json`; runner
  baixado e verificado pelo Dockerfile do Dev Container.
- Dev Container usa `maven:3.9.16-eclipse-temurin-21`. O Compose inicia o
  workspace e PostgreSQL 17. A porta PostgreSQL é publicada somente em
  `127.0.0.1:5432`.
- As credenciais do Compose são fictícias e somente para desenvolvimento local;
  não devem ser reutilizadas em outros ambientes.
- Verificações Java devem usar `./mvnw test` e `./mvnw package` dentro do Dev
  Container.

## Limites desta captura

- O tag `17-bookworm` fixa a major do PostgreSQL, mas não fixa a revisão da
  imagem nem a versão patch do servidor.
- O runtime Java informado é o observado no ambiente desta captura; a imagem
  Docker especifica a linha Eclipse Temurin 21, não esse patch exato.
- O Specify CLI 1.4.4 foi usado para gerar a integração. O CLI não está
  instalado persistentemente no projeto.
- Este documento não declara testes como aprovados. Registre resultados de
  validação no commit ou relatório da migração que os executar.

## Uso na próxima migração

Leia este snapshot antes de pesquisar e propor versões-alvo. Confirme a
baseline no código atual e consulte documentação oficial para suporte e
compatibilidade. Crie uma especificação TO-BE separada com Spec Kit em
`specs/`; não altere este AS-IS para fazê-lo parecer com o destino. Se for
necessário registrar uma baseline posterior, preserve este arquivo e crie um
novo snapshot com data e revisão de origem explícitas.
