# 01 — Recriar a POC

Atue como engenheiro(a) de software e recrie, em um repositório vazio, esta POC
de backend com o mesmo escopo e comportamento. Este é o passo 01 de um exercício
de três etapas: gerar a aplicação e seu harness; no passo 02 instalar o Spec
Kit e registrar o AS-IS; no passo 03 especificar e executar uma migração TO-BE.
Use como referência os arquivos fornecidos junto com esta solicitação,
especialmente `README.md`, `.github/copilot-instructions.md`,
`.agents/skills/`, `.vscode/mcp.json` e `.devcontainer/`. Se algum deles não
estiver disponível, peça o conteúdo ou registre a limitação; não invente
detalhes do projeto.

## Resultado esperado

- Java 21, Quarkus 3.33.4, Funqy HTTP, PostgreSQL 17 e Liquibase.
- Duas funções Funqy HTTP: `listTasks` retorna tarefas em ordem de id; `createTask`
  recebe JSON com `title`, valida e persiste a tarefa, retornando `id` e `title`.
  Mantenha esse contrato compatível com o cliente Angular existente. Não crie
  endpoints REST nem simule sucesso em caso de falha de persistência.
- Persistência PostgreSQL com Liquibase; inclua a migration inicial e configure
  o datasource pelo ambiente, com valores fictícios somente para uso local.
- Dev Container com Maven Wrapper, PostgreSQL em Compose e porta do banco
  vinculada somente a localhost. Não exponha credenciais reais nem reutilize
  credenciais de desenvolvimento em ambientes compartilhados.
- Testes cobrindo persistência, validação e contrato HTTP/CORS pertinente.
- Harness do projeto: regras em `.github/copilot-instructions.md`, skills
  `quarkus-funqy` e `liquibase-postgresql` em `.agents/skills/`, e configuração
  MCP em `.vscode/mcp.json` para o Quarkus Agent MCP. Mantenha versão e checksum
  do MCP consistentes entre sua configuração e a imagem do Dev Container.
- Documentação para aprendizado dos três passos: inclua a pasta `prompts/` com
  os prompts 01, 02 e 03 e um `README.md` que explique a sequência. O prompt 02
  deverá instalar/inicializar o Spec Kit e criar o snapshot AS-IS em
  `.specify/memory/as-is.md`; o prompt 03 deverá usar esse snapshot para uma
  especificação TO-BE separada em `specs/`.

## Restrições

- Funqy HTTP é uma escolha intencional desta POC e está em preview no Quarkus.
  Preserve-a e documente limitações ou incompatibilidades; não substitua por
  REST sem solicitação explícita.
- Use o Maven Wrapper e rode comandos Java dentro do Dev Container.
- Não altere changesets Liquibase já existentes para evoluir o schema; adicione
  novos changesets.
- Valide entradas, propague falhas de persistência e teste o comportamento.
- Siga as regras e skills fornecidas, sem enfraquecê-las para facilitar a
  geração. Configure somente MCPs necessários; não habilite acesso amplo ao
  Docker do host.
- Não inclua dados reais ou sigilosos em código, prompts ou consultas externas.

## Execução

1. Inspecione as referências e identifique o contrato e as decisões que devem
   ser preservadas.
2. Apresente um resumo breve do que vai criar e de qualquer informação ausente;
   prossiga com decisões razoáveis sem mudar o escopo.
3. Crie os arquivos da aplicação, testes, documentação, Dev Container, regras,
   skills e configuração MCP. Inclua os três prompts como material didático;
   não finja que o Spec Kit já foi instalado nem que o AS-IS já foi capturado:
   esses são os passos 02 e 03 do exercício.
4. Verifique a compatibilidade das APIs e dependências com documentação oficial
   quando necessário. Não invente opções de configuração do MCP; explique
   limitações do ambiente.
5. Valide dentro do Dev Container com `./mvnw test` e `./mvnw package`.
6. Ao concluir, explique que este passo produz o projeto e o harness (Rules,
   Agent Skills e MCP), mas não instala o Spec Kit nem captura o AS-IS. Liste os
   arquivos principais, decisões, comandos executados e resultados dos testes.
   Não declare validações que não foram executadas.
