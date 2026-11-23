# Instruções do projeto

- Esta POC usa Java 21, Quarkus 3.33.4, Funqy HTTP e PostgreSQL 17.
- Funqy HTTP está em preview no Quarkus. Preserve essa escolha nesta POC e
  registre limitações ou mudanças de compatibilidade; não troque por REST sem
  solicitação explícita.
- Use o Maven Wrapper e execute comandos Java dentro do Dev Container.
- O PostgreSQL do compose é somente para desenvolvimento local; não exponha
  credenciais reais nem use esses valores em ambientes compartilhados.
- Versione mudanças de schema como novos changelogs Liquibase. Não altere
  changesets já aplicados.
- Mantenha o contrato das funções `listTasks` e `createTask` alinhado com o
  cliente Angular. Valide entrada e propague falhas de persistência sem
  respostas de sucesso simuladas.
- Cubra mudanças de comportamento com testes e execute `./mvnw test` e
  `./mvnw package` no Dev Container.
- Use instruções e exemplos fictícios; não inclua dados reais ou sigilosos em
  prompts ou consultas externas.
- Ao usar o Spec Kit, mantenha `.specify/memory/constitution.md` alinhada com
  estas instruções e com as skills do projeto. Uma migração explicitamente
  solicitada pode atualizar a baseline tecnológica após verificação de
  compatibilidade e atualização da documentação afetada.
