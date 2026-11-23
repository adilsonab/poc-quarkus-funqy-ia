# 02 — Instalar Spec Kit

Prepare este repositório para usar o Spec Kit com o agente disponível no VS
Code, sem remover nem sobrescrever regras, skills, ferramentas MCP ou código já
existente.

1. Consulte a documentação e o repositório oficial
   [github/spec-kit](https://github.com/github/spec-kit) para descobrir o
   método de instalação, a versão recomendada, os comandos e a integração com
   VS Code/Copilot suportados no momento. Não suponha comandos ou opções com
   base em tutoriais antigos.
2. Antes de instalar, informe os pré-requisitos, o que será criado ou alterado
   e qualquer dependência necessária. Não instale ferramentas globalmente nem
   execute comandos privilegiados sem necessidade. Prefira uma execução
   descartável via `uvx` e fixe a versão oficial do Spec Kit usada.
3. Inicialize o Spec Kit neste repositório pela integração adequada. Para
   GitHub Copilot no VS Code, use `--integration copilot`; a configuração IDE
   não exige Copilot CLI. Como o projeto já contém arquivos, inspecione os
   caminhos gerados e possíveis conflitos antes de permitir mesclagem.
   Preserve `.github/copilot-instructions.md`, `.agents/skills/`,
   `.vscode/mcp.json` e `.devcontainer/`; integre instruções explicitamente,
   sem substituir conteúdo existente.
4. Configure a constituição do Spec Kit para reforçar as regras existentes:
   Java 21 e Quarkus 3.33.4 como baseline atual; Funqy HTTP intencional e em
   preview; contrato `listTasks`/`createTask` compatível com Angular; PostgreSQL
   e migrations Liquibase seguras; Maven Wrapper e testes no Dev Container;
   proteção de credenciais. A constituição não deve impedir uma migração
   solicitada explicitamente.
5. Não solicite nem transfira código, credenciais ou dados confidenciais a
   serviços externos. Use fontes públicas oficiais apenas para instruções e
   compatibilidade. Não adicione MCPs novos sem necessidade e autorização.
6. Verifique a instalação pela versão do CLI e pelo estado da integração, usando
   o mesmo `uvx` fixado. Informe a versão, os arquivos alterados, os comandos
   realmente executados e um exemplo de como iniciar o fluxo Spec Kit para uma
   especificação. Se algo depender de interação manual no VS Code, explique
   exatamente o passo que falta.

Não implemente uma migração de dependências nesta etapa. O objetivo aqui é
instalar e integrar o processo ao harness que já existe.
