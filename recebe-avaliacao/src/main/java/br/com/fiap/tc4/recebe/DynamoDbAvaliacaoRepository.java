package br.com.fiap.tc4.recebe;

import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import br.com.fiap.tc4.core.Avaliacao;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

@ApplicationScoped
public class DynamoDbAvaliacaoRepository implements AvaliacaoRepository {

    private final DynamoDbClient dynamoDb;
    private final String tabela;

    public DynamoDbAvaliacaoRepository(DynamoDbClient dynamoDb,
            @ConfigProperty(name = "avaliacoes.tabela") String tabela) {
        this.dynamoDb = dynamoDb;
        this.tabela = tabela;
    }

    @Override
    public void salvar(Avaliacao avaliacao) {
        dynamoDb.putItem(PutItemRequest.builder()
                .tableName(tabela)
                .item(item(avaliacao))
                // id é UUID novo: a condição só protege contra sobrescrita acidental
                .conditionExpression("attribute_not_exists(#sk)")
                .expressionAttributeNames(Map.of("#sk", "criadoEm#id"))
                .build());
    }

    static Map<String, AttributeValue> item(Avaliacao a) {
        return Map.of(
                "dia", s(a.dia()),
                "criadoEm#id", s(a.criadoEmId()),
                "id", s(a.id()),
                "descricao", s(a.descricao()),
                "nota", AttributeValue.fromN(Integer.toString(a.nota())),
                "urgencia", s(a.urgencia().name()),
                "dataEnvio", s(a.dataEnvio().toString()));
    }

    private static AttributeValue s(String valor) {
        return AttributeValue.fromS(valor);
    }
}
