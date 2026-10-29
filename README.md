# TC4 Feedback

Plataforma serverless (AWS, `us-east-2`) para receber avaliações de aulas, alertar sobre itens críticos e gerar relatório semanal. Arquitetura completa em [docs/ARQUITETURA.md](docs/ARQUITETURA.md).

## Status

| Módulo | Situação |
|---|---|
| `core` | ✅ `Avaliacao`, `Urgencia` e regra de classificação |
| `recebe-avaliacao` | ✅ `POST /avaliacao` → DynamoDB |
| `notifica-urgencia` | ✅ DynamoDB Stream (filtro `CRITICA`) → SNS `alertas-criticos` → e-mail |
| `gera-relatorio` | ✅ toda segunda 08:00 (Brasília) → JSON no S3 + e-mail via SNS `relatorio-semanal` |

## Pré-requisitos

Java 21, Maven 3.9+, AWS SAM CLI, AWS CLI com o perfil `tc4`.

## Build e testes

```bash
mvn verify          # testes + recebe-avaliacao/target/function.zip
```

## Deploy

```bash
aws login --profile tc4
mvn -DskipTests package
sam deploy --parameter-overrides AdminEmail=<email-do-admin>   # stack tc4-feedback, us-east-2
```

O e-mail dos administradores é passado no deploy (não fica no `samconfig.toml`, pois o repositório é público).
Na primeira vez, a AWS envia um e-mail de confirmação para cada tópico (`alertas-criticos` e `relatorio-semanal`) — as mensagens só chegam depois de clicar em **Confirm subscription**.

## Uso

```bash
URL=$(aws cloudformation describe-stacks --stack-name tc4-feedback --profile tc4 \
  --query "Stacks[0].Outputs[?OutputKey=='ApiUrl'].OutputValue" --output text)
KEY_ID=$(aws cloudformation describe-stacks --stack-name tc4-feedback --profile tc4 \
  --query "Stacks[0].Outputs[?OutputKey=='ApiKeyId'].OutputValue" --output text)
KEY=$(aws apigateway get-api-key --api-key "$KEY_ID" --include-value --profile tc4 --query value --output text)

curl -i -X POST "$URL" -H "x-api-key: $KEY" -H 'Content-Type: application/json' \
  -d '{"descricao":"Áudio da aula cortando","nota":2}'
```

| Status | Quando |
|---|---|
| `201` | criado — corpo com `id`, `urgencia`, `dataEnvio` |
| `400` | nota fora de 0–10 (ou não inteira), descrição vazia, JSON inválido |
| `403` | sem API key |

## Alertas críticos

Toda avaliação com nota 0–3 gera um e-mail para os administradores com descrição, urgência e data de envio
(horário de Brasília e UTC). O filtro fica na event source mapping do stream: avaliações não críticas nem invocam a função.
Se o SNS falhar, só o registro com falha é reprocessado (até 3 tentativas).

## Relatório semanal

Toda segunda-feira às 08:00 (Brasília) o EventBridge Scheduler executa `tc4-gera-relatorio`, que consolida a
**semana anterior fechada** (segunda a domingo): total, média das notas, quantidade por dia, quantidade por
urgência e a lista de avaliações. O JSON completo fica em `s3://<bucket>/relatorios/<ano>/semana-<segunda>.json`
e um resumo vai por e-mail.

O dia de cada avaliação (chave `dia` da tabela) é o dia no **horário de Brasília**.

Para gerar (ou regerar) o relatório de uma semana específica:

```bash
aws lambda invoke --function-name tc4-gera-relatorio:live --profile tc4 --region us-east-2 \
  --cli-binary-format raw-in-base64-out --payload '{"inicio":"2026-10-26"}' /dev/stdout
```

`inicio` precisa ser uma segunda-feira. Sem `inicio` (`{}`), usa a semana anterior.

## Monitoramento

- **Painel** CloudWatch `tc4-feedback` (link no output `DashboardUrl` da stack): avaliações por urgência,
  média das notas, invocações, erros e duração das 3 funções, requisições/erros/latência da API e estado dos alarmes.
- **Alarmes** `tc4-<função>-erros`: qualquer erro em uma função (`Errors > 0` em 5 min) envia e-mail pelo
  tópico `alertas-criticos`.
- **Métricas de negócio** (namespace `TC4Feedback`): `Avaliacoes` e `Nota` por `Urgencia`, publicadas pelo
  `recebe-avaliacao` em Embedded Metric Format (sem chamada de API).
- **Logs** em JSON, com retenção de 14 dias (`/aws/lambda/tc4-*`). Exemplo no Logs Insights:
  `fields @timestamp, level, message | filter servico = "recebe-avaliacao" | sort @timestamp desc`
- **X-Ray** ativo na API e nas 3 funções.

O tópico `alertas-criticos` usa uma chave KMS própria (`alias/tc4-alertas`, ~US$ 1/mês), exigida para que os
alarmes do CloudWatch consigam publicar num tópico criptografado.

## Regra de urgência

| Nota | Urgência |
|---|---|
| 0–3 | `CRITICA` |
| 4–6 | `MEDIA` |
| 7–10 | `BAIXA` |
