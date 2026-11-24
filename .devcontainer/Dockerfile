FROM maven:3.10.2-eclipse-temurin-25-noble

RUN apt-get update \
    && apt-get install --no-install-recommends -y curl unzip \
    && rm -rf /var/lib/apt/lists/*

ARG QUARKUS_AGENT_MCP_VERSION=1.2.15

RUN curl -fsSL \
      "https://repo.maven.apache.org/maven2/io/quarkus/quarkus-agent-mcp/${QUARKUS_AGENT_MCP_VERSION}/quarkus-agent-mcp-${QUARKUS_AGENT_MCP_VERSION}-runner.jar" \
      -o /usr/local/lib/quarkus-agent-mcp-runner.jar \
    && printf '%s  %s\n' \
      "3d4bbf3370c419a5389be7e18c2d6137dfcc92f33e0ff13d40db1c04ba6473d8" \
      /usr/local/lib/quarkus-agent-mcp-runner.jar \
      | sha256sum -c -

WORKDIR /workspace
