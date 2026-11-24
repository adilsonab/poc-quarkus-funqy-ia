# POC Quarkus Funqy IA

Backend separado para o frontend Angular do repositório irmão
[`poc-angular-ia`](../poc-angular-ia). O MVP persiste tarefas no PostgreSQL;
não há chamada a LLM em runtime.

## Stack e objetivo de migração

- Java 21
- Quarkus 3.33.4 LTS com Funqy HTTP
- PostgreSQL 17
- Liquibase para versionar o schema

As versões são deliberadamente anteriores às linhas mais novas para permitir
uma POC futura de migração. Quarkus 3.33 é uma linha LTS com manutenção
comunitária anunciada até 25/03/2027; PostgreSQL 17 tem suporte anunciado até
08/11/2029. Use os patches mais recentes dessas linhas ao atualizar o projeto.

**Atenção:** Funqy e o binding HTTP são classificados como preview pela
documentação do Quarkus. Funqy HTTP é intencional nesta POC, mas é uma
abstração minimalista e não substitui uma API REST completa. Preserve essa
limitação ao avaliar o resultado da migração.

## Executar pelo Dev Container

1. Abra esta pasta no VS Code.
2. Execute **Dev Containers: Reopen in Container**. O Compose inicia o serviço
   de desenvolvimento e PostgreSQL 17; o `postCreateCommand` compila o projeto.
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
ambientes compartilhados. Os dados ficam no volume `postgres-data` e persistem
ao parar o Dev Container. O Liquibase aplica as migrações na inicialização.

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
4. **Implementar e fechar:** após revisar/aprovar o plano e as tarefas, rode
   `/speckit-implement` e `/speckit-converge`. Use as skills de Funqy e
   Liquibase/PostgreSQL e as ferramentas MCP autorizadas. Preserve o contrato
   `listTasks`/`createTask`, não altere changesets aplicados e execute
   `./mvnw test` e `./mvnw package` no Dev Container. Registre resultados e
   limitações; não atualize o snapshot AS-IS histórico.

### Estado deste repositório

O passo 01 está representado pela própria POC e seu harness. O Spec Kit 1.4.4
foi inicializado pelo passo 02, e o AS-IS atual está registrado em
[`.specify/memory/as-is.md`](.specify/memory/as-is.md). O TO-BE da próxima
migração ainda **não** foi especificado; esse será o próximo exercício com o
prompt 03 e `/speckit-specify`.

O fluxo e a sintaxe acima correspondem à integração Copilot do Spec Kit 1.4.4.
Confirme a documentação oficial ao atualizar a versão, pois nomes e
disponibilidade dos comandos podem mudar.

## Referências oficiais

- [Quarkus releases](https://quarkus.io/releases/)
- [Funqy](https://quarkus.io/guides/funqy)
- [Funqy HTTP binding](https://quarkus.io/guides/funqy-http)
- [Liquibase no Quarkus](https://quarkus.io/guides/liquibase)
- [Política de versões PostgreSQL](https://www.postgresql.org/support/versioning/)
