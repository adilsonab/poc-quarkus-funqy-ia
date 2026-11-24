# Research: Migração segura da stack

Data da verificação: 2026-11-24. As versões aprovadas pelo usuário são o
escopo deste exercício; se este prompt for repetido no futuro, pesquisar
novamente em vez de tratar estes valores como permanentes.

## Decisões de destino

### Java 25 LTS

- **Decision**: usar Eclipse Temurin 25 na imagem do Dev Container e definir o
  alvo de compilação Java em 25.
- **Rationale**: Java 25 é uma versão LTS, com disponibilidade do Temurin
  indicada pelo Adoptium até pelo menos setembro de 2031. Quarkus 3.31 já
  declarou suporte completo a Java 25.
- **Alternatives considered**: permanecer no Java 21 (mantém suporte longo,
  mas não atende a migração aprovada); escolher Java 27 (não LTS); escolher
  Java 26 (não LTS).
- **Sources**:
  - [Adoptium support roadmap](https://adoptium.net/support/)
  - [Quarkus 3.31: Full Java 25 support](https://quarkus.io/blog/quarkus-3-31-released/)

### Quarkus 3.40.1.2 LTS

- **Decision**: atualizar BOM, plugin e extensões Quarkus para 3.40.1.2.
- **Rationale**: a página oficial de releases identifica Quarkus 3.40 como
  LTS, com manutenção comunitária até 30/09/2027, e lista 3.40.1.2 como o
  micro-release atual em 09/11/2026. A mesma página indicava Quarkus 4.0 como
  planejado para 25/11/2026, portanto futuro na data desta pesquisa.
- **MCP evidence**: `quarkus_update` em `dryRun=true`, projeto `/workspace`,
  stream `3.40`, identificou `3.33.4 -> 3.40.1.2`; Funqy HTTP, JDBC
  PostgreSQL, Liquibase e ArC foram mantidos alinhados ao BOM. O comando
  gerou `target/rewrite/rewrite.patch`; o dry-run não editou código de produção.
- **Alternatives considered**: manter 3.33 LTS (suporte até 25/03/2027, mas
  não cumpre o destino aprovado); avançar para Quarkus 4 (não era release
  disponível na data da pesquisa).
- **Sources**:
  - [Quarkus release history and support](https://quarkus.io/releases/)
  - [Quarkus 3.40 migration guide](https://github.com/quarkusio/quarkus/wiki/Migration-Guide-3.40)
  - [Quarkus update guide](https://quarkus.io/guides/update-quarkus)

### Maven 3.10.2 e Maven Wrapper 3.3.4

- **Decision**: configurar Maven Wrapper para baixar Maven 3.10.2, manter os
  scripts/implementação do Wrapper 3.3.4 e registrar o SHA-256 da distribuição
  oficial: `7d69999d218e025cf1c14691ba748a09706e47999bd505009403cef9d757c2df`.
- **Rationale**: Maven Central lista 3.10.2 como release estável da linha 3;
  Maven 4 aparecia como RC7. A página oficial do Maven descreve 3.10.2 como
  compatível com Java 8 ou superior, o que inclui Java 25. O checksum foi
  calculado a partir do ZIP oficial do Maven Central.
- **Compatibility note**: Maven 3.10.x reforça a validação de POM, muda a
  ordem de classpath e a validação de origem de credenciais; o POM atual não
  contém entradas duplicadas aparentes, mas o build deve ser executado.
- **Alternatives considered**: manter Maven 3.9.16 (não cumpre o alvo);
  Maven 4.0.0-rc-7 (release candidate, não baseline estável).
- **Sources**:
  - [Maven 3.10.2 release notes](https://maven.apache.org/docs/3.10.2/release-notes.html)
  - [Maven Wrapper documentation](https://maven.apache.org/wrapper/)
  - [Maven Central metadata](https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/maven-metadata.xml)
  - [Apache Maven 3.10.2 distribution](https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.10.2/apache-maven-3.10.2-bin.zip)

### PostgreSQL 18.7

- **Decision**: usar a imagem oficial `postgres:18.7-bookworm` e criar um
  volume novo, `postgres-data-v18`, montado em `/var/lib/postgresql`.
- **Rationale**: PostgreSQL 18.7 era o minor atual da major 18 e suportado
  até 14/11/2030. O usuário aprovou major 18, embora a major 19.0 tivesse
  acabado de sair. A imagem oficial 18 define `PGDATA` como
  `/var/lib/postgresql/18/docker` e alterou o volume declarado para
  `/var/lib/postgresql`; PostgreSQL 17 usava `/var/lib/postgresql/data`.
- **Migration method**: `pg_dump`/`pg_restore` para a base de desenvolvimento.
  PostgreSQL exige dump/restore ou `pg_upgrade` entre majors; filesystem
  backup não é compatível. O projeto usa uma única base e a imagem inicializa
  com o mesmo usuário fictício, portanto um dump da base restaurado com
  `--no-owner` atende ao cenário da POC. Manter o volume PG17 sem alteração
  como retorno local.
- **Release note**: PostgreSQL 18.7 não exige dump/restore de quem já está em
  18.x; isso não elimina a etapa necessária na transição 17→18.
- **Alternatives considered**: atualizar somente a tag da imagem mantendo o
  volume antigo (incompatível e arriscado); `pg_upgrade` (mais complexo para
  o fluxo de aprendizado e depende de ambas as ferramentas/versões); ficar no
  PostgreSQL 17 (não cumpre a decisão).
- **Sources**:
  - [PostgreSQL versioning policy](https://www.postgresql.org/support/versioning/)
  - [PostgreSQL 18 upgrade documentation](https://www.postgresql.org/docs/18/upgrading.html)
  - [PostgreSQL 18.7 release notes](https://www.postgresql.org/docs/18/release-18-7.html)
  - [Official PostgreSQL image documentation](https://github.com/docker-library/docs/blob/master/postgres/README.md#pgdata)
  - [Official PostgreSQL image tags](https://hub.docker.com/_/postgres)

### Quarkus Agent MCP, plugins Maven e Spec Kit

- **Decision**: manter Quarkus Agent MCP 1.2.15, checksum existente, Specify
  CLI baseline 1.4.4, Compiler Plugin 3.15.0 e Surefire/Failsafe 3.5.4.
- **Rationale**: o usuário aprovou manter MCP/Wrapper quando a pesquisa não
  confirmar necessidade de upgrade; não há incompatibilidade conhecida nesses
  itens. A metadata do Maven Central consultada em 24/11/2026 informa 1.2.15
  como `latest` e `release`. As versões das extensões Quarkus e driver JDBC
  continuam gerenciadas pelo BOM. Evita ampliar o escopo para uma atualização
  independente do harness.
- **Source**: [.vscode/mcp.json](../../.vscode/mcp.json),
  [.devcontainer/Dockerfile](../../.devcontainer/Dockerfile),
  [pom.xml](../../pom.xml), [Maven Central metadata](https://repo.maven.apache.org/maven2/io/quarkus/quarkus-agent-mcp/maven-metadata.xml),
  e [Spec Kit](https://github.com/github/spec-kit).

### Official Dev Container images

- **Decision**: use `maven:3.10.2-eclipse-temurin-25-noble` and
  `postgres:18.7-bookworm`.
- **Rationale**: both exact tags appeared in their respective Docker Official
  Image catalogs on the research date; selecting explicit distro suffixes
  avoids an ambiguous moving OS alias.
- **Sources**:
  - [Maven image tags](https://hub.docker.com/_/maven/tags?name=3.10.2-eclipse-temurin-25-noble)
  - [PostgreSQL image tags](https://hub.docker.com/_/postgres)

## Quarkus migration-guide review

O caminho direto de 3.33 LTS a 3.40 LTS foi verificado nos guias
intermediários 3.34–3.39 e no 3.40. Deve-se sempre ler também o guia da
baseline de origem e o da versão final:

| Guide | Findings relevant to this repository |
|---|---|
| [3.33](https://github.com/quarkusio/quarkus/wiki/Migration-Guide-3.33) | Continuação direta de 3.32; sem migração manual indicada para a linha. |
| [3.34](https://github.com/quarkusio/quarkus/wiki/Migration-Guide-3.34) | Registro de métricas duplicadas passa a falhar; o projeto não usa esse código. |
| [3.35](https://github.com/quarkusio/quarkus/wiki/Migration-Guide-3.35) | Mudanças REST, Hibernate e Dev Services; não são extensões usadas pela POC. |
| [3.36](https://github.com/quarkusio/quarkus/wiki/Migration-Guide-3.36) | Alterações Panache, OTel e TLS registry; extensões não presentes no POM. |
| [3.37](https://github.com/quarkusio/quarkus/wiki/Migration-Guide-3.37) | Mudanças Hibernate/Quarkus Data; a POC usa JDBC direto, não Hibernate/Panache. |
| [3.38](https://github.com/quarkusio/quarkus/wiki/Migration-Guide-3.38) | Mudanças para testes de integração, OTel e validação de Host; projeto configura `quarkus.http.host=0.0.0.0` e não usa as extensões citadas. |
| [3.39](https://github.com/quarkusio/quarkus/wiki/Migration-Guide-3.39) | Mudanças Quarkus Data e Avro; nenhuma dessas extensões está no projeto. |
| [3.40](https://github.com/quarkusio/quarkus/wiki/Migration-Guide-3.40) | Continuação direta de 3.39; guia não espera mudanças manuais adicionais para 3.40. |

Funqy HTTP permanece a escolha intencional e é descrito no projeto como
preview. Nenhuma fonte consultada indicou que esta atualização exija substituir
Funqy por REST. A execução real da suíte continuará sendo o critério para
confirmar compatibilidade.

## MCP and environment findings

- Quarkus Agent MCP `quarkus_update` dry-run funcionou e confirmou a versão
  destino.
- `quarkus_searchDocs` não pôde iniciar porque a busca indexada requer Docker
  ou Podman e nenhum dos dois existe no ambiente atual. Não foi habilitado
  socket Docker nem instalado daemon.
- `quarkus_skills` não encontrou skills adicionais para as extensões
  consultadas. Continuam valendo as Agent Skills locais do repositório.
- A validação de build/runtime e o dump/restore real permanecem pendentes até
  o usuário reconstruir o Dev Container com Docker disponível.

## Maven distribution checksum

O SHA-256 informado foi obtido executando `sha256sum` no arquivo baixado de
`repo.maven.apache.org`, e será usado como `distributionSha256Sum` do Wrapper.
O arquivo temporário de pesquisa não é parte do repositório.
