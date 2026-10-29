package br.com.fiap.tc4.relatorio;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import br.com.fiap.tc4.core.Avaliacao;
import br.com.fiap.tc4.core.Urgencia;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;

/** Uma {@code Query} por dia na partition key {@code dia} — sem Scan. */
@ApplicationScoped
public class DynamoDbAvaliacaoConsulta implements AvaliacaoConsulta {

    private final DynamoDbClient dynamoDb;
    private final String tabela;

    public DynamoDbAvaliacaoConsulta(DynamoDbClient dynamoDb,
            @ConfigProperty(name = "avaliacoes.tabela") String tabela) {
        this.dynamoDb = dynamoDb;
        this.tabela = tabela;
    }

    @Override
    public List<Avaliacao> doDia(LocalDate dia) {
        QueryRequest request = QueryRequest.builder()
                .tableName(tabela)
                .keyConditionExpression("dia = :dia")
                .expressionAttributeValues(Map.of(":dia", AttributeValue.fromS(dia.toString())))
                .build();
        // o paginator segue LastEvaluatedKey quando o dia passa de 1 MB
        return dynamoDb.queryPaginator(request).items().stream()
                .map(DynamoDbAvaliacaoConsulta::avaliacao)
                .toList();
    }

    static Avaliacao avaliacao(Map<String, AttributeValue> item) {
        return new Avaliacao(
                item.get("id").s(),
                item.get("descricao").s(),
                Integer.parseInt(item.get("nota").n()),
                Urgencia.valueOf(item.get("urgencia").s()),
                Instant.parse(item.get("dataEnvio").s()));
    }
}
