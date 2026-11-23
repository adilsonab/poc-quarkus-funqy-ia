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

## Prompts de aprendizado e Spec Kit

A pasta [`prompts/`](prompts/) contém três prompts numerados para reproduzir
esta POC, instalar o Spec Kit e planejar uma atualização da stack:

1. [01-recriar-poc.md](prompts/01-recriar-poc.md) orienta a geração do projeto
   com o comportamento, as regras, as skills e a configuração de MCP descritos
   neste repositório.
2. [02-instalar-speckit.md](prompts/02-instalar-speckit.md) orienta a instalação
   oficial do Spec Kit e sua integração com o agente disponível no VS Code.
3. [03-migrar-stack.md](prompts/03-migrar-stack.md) transforma uma atualização
   de Java, Quarkus/Funqy e demais tecnologias em uma migração documentada e
   verificável.

Use-os em sequência, no repositório que será trabalhado. O Spec Kit complementa
as instruções, skills e MCPs; não os substitui. As instruções do projeto
continuam valendo para o agente, as skills fornecem procedimentos especializados
e o MCP oferece ferramentas. O Spec Kit ajuda a estruturar o trabalho em
constituição, especificação, plano, tarefas, implementação e convergência.

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

Fluxo sugerido depois da instalação:

1. No chat do GitHub Copilot no VS Code, execute `/speckit-constitution` uma
   vez para revisar a constituição inicial em
   [`.specify/memory/constitution.md`](.specify/memory/constitution.md). Ela
   reforça as regras de [Copilot](.github/copilot-instructions.md) e as skills
   de [Funqy](.agents/skills/quarkus-funqy/SKILL.md) e
   [Liquibase/PostgreSQL](.agents/skills/liquibase-postgresql/SKILL.md), sem
   contradizê-las.
2. Use o [prompt de migração](prompts/03-migrar-stack.md) como entrada para
   `/speckit-specify`. Depois revise, uma etapa por vez,
   `/speckit-clarify` (se necessário), `/speckit-plan`, `/speckit-tasks`,
   `/speckit-analyze`, `/speckit-implement` e `/speckit-converge`. Os comandos
   são executados no chat do agente, não no terminal.
3. Revise as versões-alvo e as fontes oficiais antes de aprovar o plano.
   Implemente as tarefas com as skills relevantes, preserve Funqy HTTP e sua
   condição de preview, salvo decisão explícita em contrário.
4. Revise o diff e os riscos de compatibilidade; execute `./mvnw test` e
   `./mvnw package` dentro do Dev Container. Registre resultados e limitações.

O fluxo e a sintaxe acima correspondem à integração Copilot do Spec Kit 1.4.4.
Confirme a documentação oficial ao atualizar a versão, pois nomes e
disponibilidade dos comandos podem mudar.

## Referências oficiais

- [Quarkus releases](https://quarkus.io/releases/)
- [Funqy](https://quarkus.io/guides/funqy)
- [Funqy HTTP binding](https://quarkus.io/guides/funqy-http)
- [Liquibase no Quarkus](https://quarkus.io/guides/liquibase)
- [Política de versões PostgreSQL](https://www.postgresql.org/support/versioning/)
