# Feature Specification: Migração segura da stack

**Feature Branch**: `001-stack-upgrade`

**Created**: 2026-11-24

**Status**: In progress

**Input**: Migrate this existing Quarkus Funqy task application to the approved Java 25 LTS, Quarkus 3.40.1.2 LTS, PostgreSQL 18.7, and Maven 3.10.2 baseline while preserving function contracts and local database data.

## User Scenarios & Testing

### User Story 1 - Usar a aplicação na stack atualizada (Priority: P1)

Como pessoa que mantém a POC, quero executar a aplicação na stack aprovada
sem mudar o comportamento das funções de tarefas.

**Why this priority**: A migração só tem valor se a aplicação continuar
funcionando para o frontend existente.

**Independent Test**: Executar a compilação e os testes existentes e confirmar
que as funções de listar e criar tarefas continuam aceitando e devolvendo os
mesmos dados JSON.

**Acceptance Scenarios**:

1. **Given** uma instalação limpa do projeto, **When** a pessoa abre o Dev
   Container e inicia a aplicação, **Then** ela pode listar tarefas e criar uma
   tarefa pela interface Funqy HTTP existente.
2. **Given** uma entrada inválida ou uma falha real de persistência, **When** a
   função correspondente é chamada, **Then** a aplicação continua rejeitando
   a entrada ou propagando a falha sem simular sucesso.

### User Story 2 - Migrar o banco local sem perder tarefas (Priority: P1)

Como pessoa desenvolvedora, quero atualizar o PostgreSQL de desenvolvimento
sem abrir o diretório de dados da versão antiga com a nova versão e sem perder
as tarefas já persistidas.

**Why this priority**: PostgreSQL 17 e 18 usam diretórios de dados diferentes;
uma atualização direta da imagem não é uma migração segura de dados.

**Independent Test**: Criar tarefas no PostgreSQL 17, fazer e verificar um
backup, restaurá-lo em um volume PostgreSQL 18 separado e comparar as tarefas
antes e depois da restauração.

**Acceptance Scenarios**:

1. **Given** um banco PostgreSQL 17 em execução, **When** a pessoa segue o
   procedimento documentado de backup, **Then** o arquivo de backup é criado
   fora do diretório de dados do container e o volume original permanece
   intacto.
2. **Given** um volume PostgreSQL 18 novo e um backup válido, **When** o backup
   é restaurado antes de iniciar a aplicação, **Then** as tarefas existentes
   ficam disponíveis e o Liquibase reconhece o histórico já aplicado.
3. **Given** que o backup falha ou não existe, **When** a pessoa tenta
   prosseguir, **Then** as instruções dizem para parar e não apagar nem
   substituir o volume PostgreSQL 17.

### User Story 3 - Aprender e repetir os três prompts (Priority: P2)

Como colega sem experiência prévia com Spec Kit, Rules, Agent Skills ou MCP,
quero seguir o README para entender a função de cada prompt e repetir a
instalação, captura AS-IS e migração TO-BE.

**Why this priority**: A POC também é material de aprendizado, não apenas uma
mudança de versões.

**Independent Test**: Uma pessoa seguindo o README identifica o que executar
em cada etapa, onde revisar os artefatos e como saber se a migração foi
validada, sem sobrescrever o AS-IS histórico.

**Acceptance Scenarios**:

1. **Given** o repositório antes da migração, **When** a pessoa segue os passos
   01 e 02, **Then** entende que o prompt 01 recria a POC e o prompt 02 instala
   o Spec Kit e registra a baseline AS-IS.
2. **Given** os artefatos AS-IS e TO-BE, **When** a pessoa segue o passo 03,
   **Then** consegue distinguir pesquisa, especificação, plano, tarefas,
   implementação, validação e convergência.
3. **Given** uma etapa que não pôde ser validada, **When** a pessoa lê o
   relatório, **Then** encontra a limitação real e não uma afirmação de sucesso
   não comprovada.

### Edge Cases

- Um backup PostgreSQL 17 não existe, está vazio ou falha ao ser criado.
- O volume PostgreSQL 17 permanece no host enquanto o volume PostgreSQL 18 é
  inicializado separadamente.
- A aplicação ou o MCP de busca de documentação não pode ser iniciado porque
  Docker ou Podman não estão disponíveis.
- Uma dependência gerenciada pelo novo BOM exige mudança de código ou de
  configuração para manter o contrato Funqy.
- O Dev Container ainda está aberto com a imagem Java antiga durante a troca
  da base.

## Requirements

### Functional Requirements

- **FR-001**: A baseline de destino aprovada MUST usar Java 25 LTS, Quarkus
  3.40.1.2 LTS, PostgreSQL 18.7 e Apache Maven 3.10.2; manter o Maven Wrapper
  3.3.4 e o Quarkus Agent MCP 1.2.15 se a pesquisa oficial não confirmar uma
  versão mais nova compatível.
- **FR-002**: O sistema MUST preservar Funqy HTTP, os nomes `listTasks` e
  `createTask`, seus payloads JSON e o comportamento esperado pelo cliente
  Angular.
- **FR-003**: O sistema MUST continuar validando entradas e propagando falhas
  reais de persistência, sem respostas de sucesso simuladas.
- **FR-004**: A configuração local MUST usar uma imagem PostgreSQL 18.7 com
  volume novo no caminho de dados apropriado para PostgreSQL 18; MUST NOT
  conectar o diretório de dados PostgreSQL 17 ao servidor PostgreSQL 18.
- **FR-005**: A documentação MUST explicar backup e restauração de PostgreSQL
  17 para 18, exigir confirmação do backup antes de prosseguir e manter
  intacto o volume antigo. Não deve recomendar `docker compose down -v`.
- **FR-006**: A migração MUST manter as migrações Liquibase já aplicadas e os
  dados existentes; mudanças de schema, se necessárias, MUST ser novos
  changesets.
- **FR-007**: As Rules, as Agent Skills, a constituição, o prompt 03 e o README
  MUST refletir a baseline nova sem editar ou reescrever o snapshot
  `.specify/memory/as-is.md`.
- **FR-008**: A documentação MUST ensinar, em linguagem acessível, como os
  prompts 01, 02 e 03 se relacionam com Rules, Agent Skills, MCP e as fases do
  Spec Kit, e MUST diferenciar verificações executadas das bloqueadas.
- **FR-009**: A migração MUST ser validada com `./mvnw test` e
  `./mvnw package` dentro do Dev Container; qualquer verificação impossível
  deve ser relatada com seu motivo.

### Key Entities

- **Tarefa**: Registro persistido com identificador e título, exposto pelas
  funções `listTasks` e `createTask`.
- **Backup local**: Exportação do banco de desenvolvimento PostgreSQL 17,
  mantida fora do volume de dados e usada para restaurar tarefas no PostgreSQL
  18.
- **Snapshot AS-IS**: Registro histórico e imutável da baseline anterior à
  migração.
- **Artefatos TO-BE**: Especificação, pesquisa, plano, tarefas e roteiro de
  validação da migração, armazenados em uma nova feature Spec Kit.

## Success Criteria

### Measurable Outcomes

- **SC-001**: Todas as funções e formatos de dados observáveis antes da
  migração permanecem compatíveis com o frontend, confirmados por testes
  automatizados ou verificações de contrato.
- **SC-002**: O número e o conteúdo das tarefas de teste são iguais antes do
  backup PostgreSQL 17 e após a restauração PostgreSQL 18.
- **SC-003**: Os testes automatizados existentes terminam sem falhas na
  plataforma de destino.
- **SC-004**: O README descreve os três prompts em ordem e, para cada um,
  identifica o estado inicial, a ação e o resultado a revisar; o checklist
  dos três itens pode ser conferido diretamente no Markdown.
- **SC-005**: O snapshot AS-IS anterior permanece idêntico ao registrado antes
  da migração.

## Assumptions

- A escolha Java 25 LTS, Quarkus 3.40.1.2 LTS, PostgreSQL 18.7 e Maven 3.10.2
  foi confirmada pelo usuário para esta execução.
- O banco PostgreSQL no Compose é local para desenvolvimento e não contém
  dados de produção.
- O schema da aplicação não precisa mudar apenas por causa da atualização do
  servidor PostgreSQL; qualquer necessidade encontrada será tratada como uma
  evolução de schema separada e versionada com Liquibase.
- A migração de dados PostgreSQL 17 para 18 será feita por dump/restore. Não
  será usada atualização in-place nem `pg_upgrade` automatizado nesta POC.
- A disponibilidade do MCP de busca de documentação depende de Docker/Podman
  ou de um serviço de busca configurado; sua ausência não autoriza expor o
  socket Docker do host.
