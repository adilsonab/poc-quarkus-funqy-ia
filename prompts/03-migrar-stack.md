# 03 — Migrar a stack

Este é o passo 03 do tutorial e parte do AS-IS capturado pelo prompt 02. Use o
Spec Kit instalado neste repositório para transformar a baseline atual em um
TO-BE pesquisado, revisável e implementável. Não presuma que as versões citadas
neste prompt ainda são a baseline: leia `.specify/memory/as-is.md` e confirme
os arquivos da revisão atual. Na captura inicial, a POC usava Java 21,
Quarkus 3.33.4, Funqy HTTP, PostgreSQL 17 e Liquibase.

## Antes de propor a migração

1. Leia `.specify/memory/as-is.md`, `README.md`, a constituição em
   `.specify/memory/constitution.md`, as regras em
   `.github/copilot-instructions.md`, as skills em `.agents/skills/`, o
   `pom.xml`, o Maven Wrapper, o Dev Container, o Compose, as migrations e os
   testes. Confirme a baseline AS-IS contra o working tree; registre qualquer
   divergência antes de especificar.
2. Consulte fontes oficiais atuais para Java, Quarkus, Funqy/Funqy HTTP,
   PostgreSQL, Liquibase, Maven e imagens do Dev Container. Para cada alvo,
   registre versão, suporte, compatibilidade e URL da fonte. Não use como fato
   versões presumidas nem recomendações de blog.
3. Verifique se Funqy HTTP continua disponível e em que estado de suporte está.
   Preserve a abordagem Funqy desta POC. Se houver breaking changes ou uma
   incompatibilidade que exija outra arquitetura, pare e apresente alternativas
   e impactos; não migre para REST sem autorização explícita.
4. Antes de editar, use `/speckit-specify` para criar uma especificação TO-BE
   em uma nova pasta `specs/` — nunca sobrescreva `.specify/memory/as-is.md`.
   Após pesquisa e revisão dos alvos, execute `/speckit-clarify` se necessário,
   `/speckit-plan`, `/speckit-tasks` e `/speckit-analyze`. Separe mudanças por
   compatibilidade quando isso reduzir risco. Apresente versões-alvo e riscos
   para revisão antes de alterações destrutivas ou difíceis de reverter.
5. Se o destino trocar a major do PostgreSQL, inclua no plano um backup
   lógico e uma verificação dos dados antes de recriar o serviço Compose.
   Explique que o backup precisa ser feito no terminal do host quando o Dev
   Container não tem acesso ao Docker. Use um volume novo para a major nova,
   preserve o volume antigo e não recomende `docker compose down -v`. Não
   inicie a aplicação no banco novo antes de restaurar o backup.
6. Se o Quarkus Agent MCP estiver disponível, use `quarkus_update` primeiro em
   `dryRun=true`, revise o patch e as notas experimentais do atualizador, e
   só aplique as receitas depois de aprovar as mudanças. Leia os guias oficiais
   de migração do Quarkus; o atualizador não substitui essa revisão. Se a busca
   de documentação via MCP estiver indisponível, registre a causa e consulte
   as fontes oficiais sem habilitar o socket Docker do host.

## Requisitos para implementação

- Atualize coordenadamente JDK/compiler, Quarkus BOM e plugins, Maven Wrapper,
  dependências, imagem do Dev Container, PostgreSQL e demais tecnologias
  afetadas. Não atualize componentes sem justificar a compatibilidade.
- Preserve `listTasks` e `createTask`, seus payloads e a integração Angular.
  Valide entradas e propague falhas reais de persistência; não introduza
  respostas de sucesso simuladas.
- Preserve dados e histórico Liquibase. Nunca edite changesets já aplicados;
  crie novos changesets apenas se a migração exigir mudança de schema.
- Ao migrar entre majors do PostgreSQL, preserve os dados com dump/restore
  validado e volumes separados. Uma troca de tag da imagem não migra o formato
  interno do diretório de dados.
- Atualize regras, skills e README apenas quando as decisões de compatibilidade
  ou procedimentos realmente mudarem. Registre claramente que Funqy HTTP é
  preview ou documente eventual mudança oficial de status.
- Preserve o snapshot AS-IS original como evidência histórica. Registre os
  destinos e decisões na especificação/plano TO-BE e, se necessário, capture a
  baseline resultante em um novo snapshot após a migração.
- Mantenha credenciais fictícias e locais. Não inclua segredos em arquivos,
  prompts, logs compartilhados ou consultas externas.
- Adicione ou ajuste testes para mudanças comportamentais e de compatibilidade.
  Execute `./mvnw test` e `./mvnw package` dentro do Dev Container, além das
  verificações adicionais justificadas pelo plano.

Ao final, resuma versões de origem e destino, link para o AS-IS preservado e
para os artefatos TO-BE, fontes oficiais consultadas, arquivos alterados,
riscos/limitações, comandos de validação e seus resultados. Se a validação não
puder ser executada, explique o bloqueio e não afirme que a migração está
validada.
