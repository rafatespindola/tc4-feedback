# TC4 Feedback

Plataforma serverless (AWS, `us-east-2`) para receber avaliações de aulas, alertar sobre itens críticos e gerar relatório semanal. Arquitetura completa em [docs/ARQUITETURA.md](docs/ARQUITETURA.md).

## Status

| Módulo | Situação |
|---|---|
| `core` | ✅ `Avaliacao`, `Urgencia` e regra de classificação |
| `recebe-avaliacao` | ✅ `POST /avaliacao` → DynamoDB |
| `notifica-urgencia` | ⏳ próxima etapa |
| `gera-relatorio` | ⏳ próxima etapa |

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
sam deploy          # usa samconfig.toml (stack tc4-feedback, us-east-2)
```

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

## Regra de urgência

| Nota | Urgência |
|---|---|
| 0–3 | `CRITICA` |
| 4–6 | `MEDIA` |
| 7–10 | `BAIXA` |
