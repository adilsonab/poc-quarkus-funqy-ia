# Instruções do projeto

- Esta POC usa Java 25 LTS, Quarkus 3.40.1.2 LTS, Funqy HTTP e PostgreSQL
  18.7; o Maven Wrapper distribui Apache Maven 3.10.2.
- Funqy HTTP está em preview no Quarkus. Preserve essa escolha nesta POC e
  registre limitações ou mudanças de compatibilidade; não troque por REST sem
  solicitação explícita.
- Use o Maven Wrapper e execute comandos Java dentro do Dev Container.
- O PostgreSQL do compose é somente para desenvolvimento local; não exponha
  credenciais reais nem use esses valores em ambientes compartilhados.
- PostgreSQL 17→18 é uma migração major: antes de recriar o serviço Compose,
  faça e verifique um dump/restore, use o volume novo `postgres-data-v18` em
  `/var/lib/postgresql` e mantenha intacto o volume antigo. Nunca recomende
  `docker compose down -v` para essa migração.
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
- Trate `.specify/memory/as-is.md` como histórico; registre futuras migrações
  TO-BE em novas pastas dentro de `specs/` e não reescreva o snapshot original.
