# POC Quarkus Funqy IA

Backend separado para o frontend Angular do repositório irmão
[`poc-angular-ia`](../poc-angular-ia). O MVP persiste tarefas no PostgreSQL;
não há chamada a LLM em runtime.

## Stack atual (TO-BE da migração)

- Java 25 LTS (Eclipse Temurin)
- Quarkus 3.40.1.2 LTS com Funqy HTTP
- PostgreSQL 18.7
- Apache Maven 3.10.2, distribuído pelo Maven Wrapper 3.3.4
- Liquibase para versionar o schema
- Quarkus Agent MCP 1.2.15, com versão e checksum fixados

O alvo foi escolhido com pesquisa em fontes oficiais em 24/11/2026. Java 25 é
LTS; Quarkus 3.40 é LTS com manutenção comunitária anunciada até 30/09/2027;
PostgreSQL 18 é suportado até 14/11/2030. O resumo detalhado e as fontes estão
em [research.md](specs/001-stack-upgrade/research.md).

O snapshot da stack anterior (Java 21, Quarkus 3.33.4, PostgreSQL 17 e Maven
3.9.16) continua preservado em
[`.specify/memory/as-is.md`](.specify/memory/as-is.md). Ele é histórico; não o
edite para refletir o alvo.

**Atenção:** Funqy e o binding HTTP são classificados como preview pela
documentação do Quarkus. Funqy HTTP é intencional nesta POC, mas é uma
abstração minimalista e não substitui uma API REST completa. Preserve essa
limitação ao avaliar o resultado da migração.

> **Antes de reconstruir:** se você já usava o PostgreSQL 17 neste projeto,
> não escolha **Rebuild and Reopen in Container** até fazer e verificar o
> backup descrito em [Preservar dados ao migrar PostgreSQL 17 para
> 18](#preservar-dados-ao-migrar-postgresql-17-para-18). A imagem PostgreSQL
> 18 usa um volume novo; o volume 17 deve permanecer intacto.

## Executar pelo Dev Container

1. Abra esta pasta no VS Code.
2. Execute **Dev Containers: Reopen in Container**. O Compose inicia o serviço
   de desenvolvimento e PostgreSQL 18.7; o `postCreateCommand` compila o projeto.
3. No terminal do container, execute:

   ```sh
   ./mvnw quarkus:dev
   ```

4. A porta 8080 é encaminhada pelo VS Code. O frontend Angular deve estar em
   execução na porta 4200.

O banco usa credenciais fictícias exclusivas para desenvolvimento local e
publica a porta 5432 somente em `localhost`, para acesso por ferramentas como
DBeaver. Configure a conexão com host `localhost`, porta `5432` e banco
`poc_quarkus_funqy_ia`; use o usuário e a senha definidos no Compose
(`poc_quarkus_funqy_ia` e `local-dev-only`). Não reutilize essas credenciais em
ambientes compartilhados. A configuração atual usa o volume
`postgres-data-v18`, montado no caminho compatível com PostgreSQL 18. Se você
tem dados no volume antigo `postgres-data`, siga primeiro o procedimento de
backup e restauração abaixo; nunca use `docker compose down -v`.

## Preservar dados ao migrar PostgreSQL 17 para 18

PostgreSQL 17 e 18 são versões **major** diferentes. Não se pode iniciar a
versão 18 diretamente sobre o diretório de dados da 17. A imagem oficial
também mudou o caminho do volume: a 17 usa `/var/lib/postgresql/data`; a 18
usa `/var/lib/postgresql` e mantém os dados em
`/var/lib/postgresql/18/docker`.

Se você tem o Dev Container antigo em execução, antes de escolher **Rebuild
and Reopen in Container**, pare `quarkus:dev` e faça o backup no terminal do
**host**, na raiz do repositório. O host precisa ter Docker Compose v2; o
terminal dentro deste Dev Container não recebe o socket Docker.

1. Gere um backup lógico PostgreSQL 17 e copie-o para a raiz do projeto:

   ```sh
   docker compose -f .devcontainer/compose.yaml exec -T database sh -c "pg_dump -Fc -U poc_quarkus_funqy_ia -d poc_quarkus_funqy_ia > /tmp/poc-quarkus-funqy-ia-pg17.dump"
   docker compose -f .devcontainer/compose.yaml cp database:/tmp/poc-quarkus-funqy-ia-pg17.dump ./poc-quarkus-funqy-ia-pg17.dump
   ```

2. Salve uma cópia das tarefas para comparar após a restauração:

   ```sh
   docker compose -f .devcontainer/compose.yaml exec -T database psql -X -U poc_quarkus_funqy_ia -d poc_quarkus_funqy_ia -c "COPY (SELECT id, title, created_at FROM tasks ORDER BY id) TO '/tmp/poc-quarkus-funqy-ia-pg17-tasks.csv' WITH CSV HEADER"
   docker compose -f .devcontainer/compose.yaml cp database:/tmp/poc-quarkus-funqy-ia-pg17-tasks.csv ./poc-quarkus-funqy-ia-pg17-tasks.csv
   ```

3. Confirme que os dois arquivos existem e que o dump não está vazio. Se
   algum comando falhar, pare e não recrie o serviço. Os arquivos são dados
   locais, ignorados pelo Git e pelo contexto Docker; nunca os publique nem
   adicione ao repositório.
4. Só então reconstrua/reabra o Dev Container. O serviço PostgreSQL 18 cria o
   volume novo `postgres-data-v18`. Antes de iniciar a aplicação, restaure o
   dump pelo terminal do host:

   ```sh
   docker compose -f .devcontainer/compose.yaml cp ./poc-quarkus-funqy-ia-pg17.dump database:/tmp/poc-quarkus-funqy-ia-pg17.dump
   docker compose -f .devcontainer/compose.yaml exec -T database pg_restore --exit-on-error --clean --if-exists --no-owner --username=poc_quarkus_funqy_ia --dbname=poc_quarkus_funqy_ia /tmp/poc-quarkus-funqy-ia-pg17.dump
   docker compose -f .devcontainer/compose.yaml exec -T database psql -X -U poc_quarkus_funqy_ia -d poc_quarkus_funqy_ia -c "COPY (SELECT id, title, created_at FROM tasks ORDER BY id) TO '/tmp/poc-quarkus-funqy-ia-pg18-tasks.csv' WITH CSV HEADER"
   docker compose -f .devcontainer/compose.yaml cp database:/tmp/poc-quarkus-funqy-ia-pg18-tasks.csv ./poc-quarkus-funqy-ia-pg18-tasks.csv
   ```

5. Compare os CSVs no VS Code usando **File: Compare Active File With...**.
   Quantidade e conteúdo devem coincidir. Só depois inicie
   `./mvnw quarkus:dev` dentro do Dev Container. O Liquibase deve reconhecer
   o histórico já restaurado. Se restauração/comparação falhar, não apague o
   dump nem o volume antigo. Não execute `docker compose down -v`.

Se você está clonando o projeto agora e não tem um banco PostgreSQL 17 local,
pode ignorar o backup antigo; o Compose cria um PostgreSQL 18 vazio e o
Liquibase cria o schema. Os comandos detalhados e a matriz de validação estão
em [quickstart.md](specs/001-stack-upgrade/quickstart.md).

## Contrato Funqy HTTP

Listar tarefas:

```http
GET http://localhost:8080/listTasks
```

Criar tarefa:

```http
POST http://localhost:8080/createTask
Content-Type: application/json

{"title":"Estudar Quarkus"}
```

As duas funções retornam JSON. O CORS está limitado às origens locais
`http://localhost:4200` e `http://127.0.0.1:4200`.

## Validar

Com o Dev Container e o PostgreSQL ativos:

```sh
./mvnw test
./mvnw package
```

## Harness de desenvolvimento

- **Rules:** [`.github/copilot-instructions.md`](.github/copilot-instructions.md)
  contém as convenções do projeto.
- **Agent Skills:** [Funqy](.agents/skills/quarkus-funqy/SKILL.md) e
  [Liquibase/PostgreSQL](.agents/skills/liquibase-postgresql/SKILL.md) orientam
  tarefas específicas.
- **MCP:** as instruções de ativação e os servidores configurados ficam em
  [`.vscode/mcp.json`](.vscode/mcp.json). Revise e autorize chamadas de
  ferramentas antes de executá-las. O `Quarkus Agent MCP` oferece busca de
  skills de extensões, ciclo de vida do app e acesso às ferramentas de
  desenvolvimento/teste do Quarkus. Ele está fixado na versão 1.2.15 e seu
  checksum é validado durante a construção da imagem. Consulte o [projeto
  oficial](https://github.com/quarkusio/quarkus-agent-mcp).

  O Dev Container não recebe acesso ao socket Docker do host. Por isso,
  `quarkus_searchDocs` requer um serviço de busca de documentação configurado
  separadamente; não habilite o socket Docker só para essa ferramenta, pois
  isso concederia controle amplo sobre o Docker do host.

  No VS Code, confirme `quarkus-agent` em **Chat: Open Customizations**. Uma
  solicitação útil para experimentar o harness é: “Use as skills de Quarkus e
  Funqy, confira o contrato das funções, execute os testes e explique os riscos
  de migração desta versão.”

## Tutorial: Rules, Skills, MCP e migração com Spec Kit

Este tutorial demonstra três etapas que podem ser repetidas por colegas:

1. [01-recriar-poc.md](prompts/01-recriar-poc.md) gera a POC em um repositório
   vazio, incluindo a aplicação e o harness: Rules, Agent Skills, configuração
   MCP, Dev Container e testes.
2. [02-instalar-speckit.md](prompts/02-instalar-speckit.md) inicializa o Spec
   Kit no projeto existente, alinha a constituição às regras e registra o
   estado atual (AS-IS) em
   [`.specify/memory/as-is.md`](.specify/memory/as-is.md), com revisão Git,
   versões e referências às evidências.
3. [03-migrar-stack.md](prompts/03-migrar-stack.md) usa o AS-IS como origem,
   pesquisa versões-alvo e cria uma especificação TO-BE separada com o fluxo
   Spec Kit antes de implementar a migração.

O AS-IS é uma fotografia factual da baseline, **não** uma especificação
retroativa de todas as funcionalidades. Preserve-o como registro histórico.
O TO-BE é o objetivo da próxima mudança e deve ficar em uma nova pasta dentro
de `specs/`; não edite o AS-IS para fazê-lo coincidir com o destino. Se uma
migração concluída precisar de uma nova baseline, capture-a em outro snapshot
datado.

O Spec Kit complementa as instruções, skills e MCPs; não os substitui. As
instruções do projeto definem convenções, as skills dão procedimentos
especializados e o MCP oferece ferramentas. O Spec Kit estrutura trabalho em
especificação, plano, tarefas, implementação e convergência.

Este repositório foi inicializado com o Specify CLI **1.4.4**, usando a
integração GitHub Copilot no modo skills. O scaffolding fica em
[`.specify/`](.specify/) e os comandos ficam em
[`.github/skills/`](.github/skills/). O CLI não foi instalado
persistentemente; para comandos administrativos, use `uvx` com a versão fixada
(requer `uv`, Python 3.11+ e Git):

```sh
uvx --from git+https://github.com/github/spec-kit.git@v1.4.4 specify integration status
```

Consulte o repositório oficial do [Spec Kit](https://github.com/github/spec-kit)
e a [documentação de instalação](https://github.github.com/spec-kit/installation.html)
para atualizar a versão ou escolher outra forma de instalar o CLI. Inicializar
em um projeto existente pode mesclar ou substituir arquivos em caminhos
gerenciados; revise os conflitos e o diff antes de confirmar mudanças.

### Como executar o fluxo

No chat do GitHub Copilot no VS Code, use os prompts e skills por etapa; não
execute os comandos `/speckit-*` no terminal.

1. **Criar a POC:** em um repositório vazio, forneça o prompt 01 e os arquivos
   de referência. Revise a aplicação, as Rules, as Agent Skills e a configuração
   MCP antes de aceitar o resultado. Esta etapa cria o ponto de partida; ela não
   instala o Spec Kit nem afirma que um AS-IS foi capturado.
2. **Instalar e registrar o AS-IS:** forneça o prompt 02 no repositório gerado.
   Confirme a versão instalada do Spec Kit e revise
   `.specify/memory/as-is.md` contra `pom.xml`, Wrapper, Dev Container, Compose,
   código, migrations, testes e harness. Confira a revisão Git, quais versões
   são declaradas versus observadas e quais validações não foram executadas.
   Execute `/speckit-constitution` para revisar a constituição se necessário.
   Não faça upgrade das dependências nessa etapa.
3. **Especificar o TO-BE:** antes de qualquer alteração, use o prompt 03 e o
   AS-IS como entrada de `/speckit-specify`. Verifique documentação oficial
   para os destinos, discuta riscos e confirme escolhas. Depois rode
   `/speckit-clarify` se necessário, `/speckit-plan`, `/speckit-tasks` e
   `/speckit-analyze`; revise os artefatos na nova pasta `specs/`.
4. **Implementar e fechar:** após revisar o plano e as tarefas, rode
   `/speckit-implement` e `/speckit-converge`. Use as skills de Funqy e
   Liquibase/PostgreSQL e as ferramentas MCP autorizadas. Para uma mudança
   major do PostgreSQL, faça o backup antes de recriar o serviço. Preserve o
   contrato `listTasks`/`createTask`, não altere changesets aplicados e execute
   `./mvnw test` e `./mvnw package` no Dev Container atualizado. Registre
   resultados e limitações; não atualize o snapshot AS-IS histórico.

### Estado deste repositório

O passo 01 está representado pela POC original e seu harness. O Spec Kit 1.4.4
foi inicializado pelo passo 02; a baseline anterior está registrada em
[`.specify/memory/as-is.md`](.specify/memory/as-is.md). O TO-BE da migração
está em [spec.md](specs/001-stack-upgrade/spec.md), com pesquisa, plano,
contratos, roteiro e tarefas em
[specs/001-stack-upgrade/](specs/001-stack-upgrade/).

O Quarkus Agent MCP foi usado para um `quarkus_update` em modo dry-run, que
confirmou o destino 3.40.1.2; a busca MCP de documentação não iniciou porque
Docker/Podman não estão disponíveis neste ambiente. O atualizador do Quarkus
usa OpenRewrite e é experimental: revise o diff e os guias oficiais antes de
aplicar qualquer receita. Não habilite o socket Docker do host apenas para
destravar a busca.

**Status da validação desta migração:** as versões e os arquivos de
configuração foram atualizados, mas não foi possível reconstruir o Dev
Container, executar os testes Java ou realizar dump/restore PostgreSQL neste
ambiente (Java 21 disponível; Docker e Podman ausentes). Siga o procedimento
acima em um host com Docker para preservar dados e então rode os comandos de
validação. Até lá, trate o destino como configurado, mas não como validado.

O fluxo e a sintaxe acima correspondem à integração Copilot do Spec Kit 1.4.4.
Confirme a documentação oficial ao atualizar a versão, pois nomes e
disponibilidade dos comandos podem mudar.

## Referências oficiais

- [Quarkus releases](https://quarkus.io/releases/)
- [Quarkus update (experimental)](https://quarkus.io/guides/update-quarkus)
- [Quarkus migration guides 3.34–3.40](https://github.com/quarkusio/quarkus/wiki/Migration-Guides)
- [Java 25 support in Quarkus](https://quarkus.io/blog/quarkus-3-31-released/)
- [Funqy](https://quarkus.io/guides/funqy)
- [Funqy HTTP binding](https://quarkus.io/guides/funqy-http)
- [Liquibase no Quarkus](https://quarkus.io/guides/liquibase)
- [Política de versões PostgreSQL](https://www.postgresql.org/support/versioning/)
- [Upgrade de cluster PostgreSQL](https://www.postgresql.org/docs/18/upgrading.html)
- [Imagem oficial PostgreSQL e `PGDATA`](https://github.com/docker-library/docs/blob/master/postgres/README.md#pgdata)
- [Apache Maven 3.10.2](https://maven.apache.org/docs/3.10.2/release-notes.html)
- [Eclipse Temurin support roadmap](https://adoptium.net/support/)
