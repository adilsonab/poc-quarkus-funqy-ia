# Quickstart: Validar a migração

## Requisitos

- VS Code com Dev Containers e Docker Desktop/Docker Engine funcionando no
  host.
- Terminal do host com Docker Compose v2, aberto na raiz do repositório.
- Não use um banco ou credenciais de produção; as credenciais do Compose são
  fictícias e locais.

## Antes de trocar PostgreSQL 17 por 18

Faça estes passos **antes** de aplicar a nova configuração (recriar o serviço
ou reconstruir o Dev Container). A simples edição dos arquivos Compose não
altera containers existentes. Não execute `docker compose down -v`.

1. Pare `quarkus:dev`, mas mantenha o PostgreSQL 17 em execução.
2. Crie o dump dentro do container PostgreSQL 17 e copie-o para a raiz do
   repositório:

   ```sh
   docker compose -f .devcontainer/compose.yaml exec -T database sh -c "pg_dump -Fc -U poc_quarkus_funqy_ia -d poc_quarkus_funqy_ia > /tmp/poc-quarkus-funqy-ia-pg17.dump"
   docker compose -f .devcontainer/compose.yaml cp database:/tmp/poc-quarkus-funqy-ia-pg17.dump ./poc-quarkus-funqy-ia-pg17.dump
   ```

3. Capture as tarefas para comparar após a restauração:

   ```sh
   docker compose -f .devcontainer/compose.yaml exec -T database psql -X -U poc_quarkus_funqy_ia -d poc_quarkus_funqy_ia -c "COPY (SELECT id, title, created_at FROM tasks ORDER BY id) TO '/tmp/poc-quarkus-funqy-ia-pg17-tasks.csv' WITH CSV HEADER"
   docker compose -f .devcontainer/compose.yaml cp database:/tmp/poc-quarkus-funqy-ia-pg17-tasks.csv ./poc-quarkus-funqy-ia-pg17-tasks.csv
   ```

4. Confirme que os dois arquivos apareceram e que o dump não está vazio. Se
   qualquer comando falhar, pare: não remova nem substitua o volume antigo.

O Compose publica esses arquivos na raiz e `.gitignore` os exclui. Eles são
dados locais: não os anexe a issues, não os envie ao GitHub e não os inclua em
commits.

## Subir o PostgreSQL 18 e restaurar

1. Atualize os arquivos conforme esta feature e no VS Code escolha
   **Dev Containers: Rebuild and Reopen in Container**. O Compose deve criar
   o volume novo `postgres-data-v18`; a aplicação não inicia automaticamente.
2. No terminal do host, copie o dump para o container novo e restaure-o:

   ```sh
   docker compose -f .devcontainer/compose.yaml cp ./poc-quarkus-funqy-ia-pg17.dump database:/tmp/poc-quarkus-funqy-ia-pg17.dump
   docker compose -f .devcontainer/compose.yaml exec -T database pg_restore --exit-on-error --clean --if-exists --no-owner --username=poc_quarkus_funqy_ia --dbname=poc_quarkus_funqy_ia /tmp/poc-quarkus-funqy-ia-pg17.dump
   ```

3. Exporte as tarefas do PostgreSQL 18:

   ```sh
   docker compose -f .devcontainer/compose.yaml exec -T database psql -X -U poc_quarkus_funqy_ia -d poc_quarkus_funqy_ia -c "COPY (SELECT id, title, created_at FROM tasks ORDER BY id) TO '/tmp/poc-quarkus-funqy-ia-pg18-tasks.csv' WITH CSV HEADER"
   docker compose -f .devcontainer/compose.yaml cp database:/tmp/poc-quarkus-funqy-ia-pg18-tasks.csv ./poc-quarkus-funqy-ia-pg18-tasks.csv
   ```
4. Compare os CSVs no VS Code (Command Palette: **File: Compare Active File
   With...**) ou linha a linha. O conteúdo e a quantidade de tarefas devem
   coincidir. Só depois inicie a aplicação com `./mvnw quarkus:dev`.
5. Se o restore ou a comparação falhar, pare a aplicação e investigue; não
   descarte o backup nem o volume PG17.

Depois que a aplicação inicia, o Liquibase deve reconhecer os changesets já
restaurados. A migração de servidor não cria, por si só, um novo changeset.

## Validar Java, Quarkus e Funqy

Dentro do Dev Container reconstruído:

```sh
java -version
./mvnw -version
./mvnw test
./mvnw package
```

Confirme que os comandos mostram Java 25 e Maven 3.10.2. A suíte deve passar;
depois, com a aplicação em execução, confirme:

```sh
curl http://localhost:8080/listTasks
curl -X POST http://localhost:8080/createTask \
  -H 'Content-Type: application/json' \
  -d '{"title":"Validar migração"}'
```

O primeiro comando deve retornar uma lista JSON; o segundo, uma tarefa com
identificador gerado. Não registre a migração como validada até terminar os
testes e a comparação de dados no container real.

## Limitação conhecida do ambiente de implementação

No ambiente em que esta feature foi preparada não há Docker nem Podman e o
Java disponível é 21. Portanto, os passos de reconstrução, dump/restore e
execução de testes precisam ser realizados após o usuário abrir um Dev
Container funcional. O MCP Quarkus pôde verificar a atualização em dry-run,
mas não substitui esses testes.
