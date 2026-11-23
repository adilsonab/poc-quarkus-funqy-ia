# 03 — Migrar a stack

Use o Spec Kit instalado neste repositório para especificar, planejar e
implementar uma migração compatível da stack. Esta POC parte de Java 21,
Quarkus 3.33.4, Funqy HTTP, PostgreSQL 17 e Liquibase. A atualização pretendida
é para versões mais novas e mutuamente compatíveis, não necessariamente para
o maior número de versão disponível.

## Antes de propor a migração

1. Leia `README.md`, as regras em `.github/copilot-instructions.md`, as skills
   em `.agents/skills/`, o `pom.xml`, o Maven Wrapper, o Dev Container, o Compose,
   as migrations e os testes. Registre a baseline efetivamente encontrada.
2. Consulte fontes oficiais atuais para Java, Quarkus, Funqy/Funqy HTTP,
   PostgreSQL, Liquibase, Maven e imagens do Dev Container. Para cada alvo,
   registre versão, suporte, compatibilidade e URL da fonte. Não use como fato
   versões presumidas nem recomendações de blog.
3. Verifique se Funqy HTTP continua disponível e em que estado de suporte está.
   Preserve a abordagem Funqy desta POC. Se houver breaking changes ou uma
   incompatibilidade que exija outra arquitetura, pare e apresente alternativas
   e impactos; não migre para REST sem autorização explícita.
4. Antes de editar, use o fluxo Spec Kit instalado para produzir/revisar
   especificação, esclarecer dúvidas, plano e tarefas. Separe mudanças por
   compatibilidade quando isso reduzir risco. Apresente versões-alvo e riscos
   para revisão antes de alterações destrutivas ou difíceis de reverter.

## Requisitos para implementação

- Atualize coordenadamente JDK/compiler, Quarkus BOM e plugins, Maven Wrapper,
  dependências, imagem do Dev Container, PostgreSQL e demais tecnologias
  afetadas. Não atualize componentes sem justificar a compatibilidade.
- Preserve `listTasks` e `createTask`, seus payloads e a integração Angular.
  Valide entradas e propague falhas reais de persistência; não introduza
  respostas de sucesso simuladas.
- Preserve dados e histórico Liquibase. Nunca edite changesets já aplicados;
  crie novos changesets apenas se a migração exigir mudança de schema.
- Atualize regras, skills e README apenas quando as decisões de compatibilidade
  ou procedimentos realmente mudarem. Registre claramente que Funqy HTTP é
  preview ou documente eventual mudança oficial de status.
- Mantenha credenciais fictícias e locais. Não inclua segredos em arquivos,
  prompts, logs compartilhados ou consultas externas.
- Adicione ou ajuste testes para mudanças comportamentais e de compatibilidade.
  Execute `./mvnw test` e `./mvnw package` dentro do Dev Container, além das
  verificações adicionais justificadas pelo plano.

Ao final, resuma versões de origem e destino, fontes oficiais consultadas,
arquivos alterados, riscos/limitações, comandos de validação e seus resultados.
Se a validação não puder ser executada, explique o bloqueio e não afirme que a
migração está validada.
