# Arquitetura — TC4 Feedback

Plataforma serverless na AWS (`us-east-2`) para receber avaliações de aulas, notificar admins sobre itens críticos e gerar relatório semanal.

## Diagrama

```mermaid
flowchart LR
    A[Estudante] -->|POST /avaliacao<br/>x-api-key| B[API Gateway<br/>REST + API Key + throttling]
    B --> C[λ recebe-avaliacao]
    C -->|grava| D[(DynamoDB<br/>Avaliacoes)]
    D -->|Stream filtrado<br/>urgencia = CRITICA| E[λ notifica-urgencia]
    E -->|publica| F[SNS<br/>alertas-criticos]
    F -->|e-mail| G[Administradores]

    H[EventBridge Scheduler<br/>seg 08:00 BRT] --> I[λ gera-relatorio]
    I -->|lê 7 dias| D
    I -->|salva| J[(S3<br/>relatorios)]
    I -->|publica| K[SNS<br/>relatorio-semanal]
    K -->|e-mail| G

    C & E & I -.logs/métricas/traces.-> L[CloudWatch + X-Ray]
    L -->|alarme de erro| F
```

## Funções (responsabilidade única)

| Função | Gatilho | Responsabilidade |
|---|---|---|
| `recebe-avaliacao` | API Gateway `POST /avaliacao` | Validar entrada, classificar urgência, persistir |
| `notifica-urgencia` | DynamoDB Stream (filtro `CRITICA`) | Montar e enviar o alerta (descrição, urgência, data de envio) |
| `gera-relatorio` | EventBridge Scheduler (semanal) | Agregar a semana: média, qtd por dia, qtd por urgência, lista de avaliações |

A função de recebimento **não sabe** que existe notificação: o desacoplamento é feito pelo Stream com filtro de evento.

## Regra de urgência

| Nota | Urgência |
|---|---|
| 0–3 | CRITICA (dispara alerta) |
| 4–6 | MEDIA |
| 7–10 | BAIXA |

## Modelo de dados (DynamoDB `Avaliacoes`)

| Atributo | Tipo | Exemplo |
|---|---|---|
| `dia` (PK) | String (dia no horário de Brasília) | `2026-10-27` |
| `criadoEm#id` (SK) | String | `2026-10-27T14:03:11Z#9f1c…` |
| `id` | String (UUID) | |
| `descricao` | String | |
| `nota` | Number | `2` |
| `urgencia` | String | `CRITICA` |
| `dataEnvio` | String (ISO-8601) | |

PK por dia → o relatório faz 7 `Query` (uma por dia), sem `Scan`.

## Contrato de entrada

```http
POST /avaliacao
x-api-key: <chave>
Content-Type: application/json

{ "descricao": "Áudio da aula cortando", "nota": 2 }
```

Respostas: `201` (criado), `400` (nota fora de 0–10 ou descrição vazia), `403` (sem API key).

## Perfis de acesso

| Perfil | Pode | Canal | Controle de acesso |
|---|---|---|---|
| **Estudante** | Avaliar aulas (descrição + nota 0–10) | `POST /avaliacao` no API Gateway | API Key + usage plan `tc4-feedback-estudantes` (throttling); só tem acesso a esse endpoint, não lê dados |
| **Administrador** | Receber alertas de avaliações críticas | E-mail via SNS `alertas-criticos` (imediato, nota 0–3) | Inscrição no tópico (e-mail em `/tc4-feedback/admin-email`) |
| | Receber o relatório semanal | E-mail via SNS `relatorio-semanal` (segunda 08:00 BRT) | Inscrição no tópico |
| | Consultar o histórico de relatórios | S3 `relatorios/AAAA/semana-AAAA-MM-DD.json` (média, qtd por dia, qtd por urgência, lista de avaliações) | Team member do projeto (console AWS); bucket privado |
| | Acompanhar análises e a saúde do sistema | Dashboard CloudWatch `tc4-feedback` (avaliações por urgência, invocações, erros, duração) | Team member do projeto (console AWS) |

A separação é estrutural: o estudante só alcança a API de escrita; leitura de avaliações, relatórios e métricas fica restrita a quem tem acesso ao projeto AWS ou está inscrito nos tópicos SNS.

## Segurança e governança

- **IAM**: uma role por função, permissão mínima (ex.: `recebe-avaliacao` só faz `PutItem` na tabela).
- **Acesso humano**: administradores são convidados por e-mail como *team members* em AWS Settings (sem usuários IAM); as permissões de acesso humano são gerenciadas pela AWS.
- **API**: API Key + usage plan (throttling) no API Gateway.
- **Dados**: criptografia em repouso (DynamoDB, S3, SNS com KMS); S3 com bloqueio de acesso público. O tópico `alertas-criticos` usa chave KMS própria (`alias/tc4-alertas`) para aceitar publicações dos alarmes do CloudWatch.
- **CI/CD**: AWS CodePipeline acionado por push na `main` via CodeConnections (sem chaves de acesso); CloudFormation com role de deploy de permissão restrita. GitHub Actions roda os testes. (OIDC do GitHub não é possível: a SCP do projeto bloqueia `iam:*Provider*`.)
- **Custo**: AWS Budgets com alerta.

## Monitoramento

- CloudWatch Logs (retenção 14 dias) e logs estruturados em JSON.
- Métricas de negócio via Embedded Metric Format (namespace `TC4Feedback`: `Avaliacoes` e `Nota` por `Urgencia`).
- X-Ray tracing nas 3 funções.
- Alarme: `Errors > 0` em qualquer função → SNS `alertas-criticos`.
- Dashboard CloudWatch: invocações, erros, duração, avaliações por urgência.

## Stack

- Java 21 + Quarkus (`quarkus-amazon-lambda`, `quarkus-amazon-lambda-rest`)
- Lambda em JVM com **SnapStart** (reduz cold start sem precisar de build nativo)
- IaC: AWS SAM (`template.yaml` único)
- CI/CD: CodePipeline → CodeBuild (`mvn verify` + `sam package`) → CloudFormation; GitHub Actions para testes

## Estrutura do repositório

```
tc4-feedback/
├── pom.xml                      # parent (módulos + versões)
├── core/                        # domínio: Avaliacao, Urgencia, regra de classificação
├── recebe-avaliacao/            # λ HTTP
├── notifica-urgencia/           # λ DynamoDB Stream
├── gera-relatorio/              # λ agendada
├── template.yaml                # infra SAM (tabela, SNS, S3, API, scheduler, alarmes)
├── buildspec.yml                # CodeBuild
├── cicd/pipeline.yaml           # CodePipeline + CodeBuild + roles (stack tc4-feedback-cicd)
├── .github/workflows/ci.yml     # testes em push/PR
├── docs/
│   ├── ARQUITETURA.md
│   ├── RelatorioTecnicoTC4Fase4.html / .pdf   # relatório técnico da entrega
│   └── GITHUB_VIDEO_ALUNOS.txt                # alunos, RMs e links
└── README.md
```
