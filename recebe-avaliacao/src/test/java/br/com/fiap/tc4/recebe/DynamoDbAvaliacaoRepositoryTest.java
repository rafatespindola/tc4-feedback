package br.com.fiap.tc4.recebe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import br.com.fiap.tc4.core.Avaliacao;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

class DynamoDbAvaliacaoRepositoryTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-10-27T14:03:11Z"), ZoneOffset.UTC);

    @Test
    void gravaItemComChavesDoModelo() {
        DynamoDbClient client = mock(DynamoDbClient.class);
        Avaliacao a = Avaliacao.nova("Áudio da aula cortando", 2, clock);

        new DynamoDbAvaliacaoRepository(client, "Avaliacoes").salvar(a);

        ArgumentCaptor<PutItemRequest> captor = ArgumentCaptor.forClass(PutItemRequest.class);
        verify(client).putItem(captor.capture());
        PutItemRequest req = captor.getValue();

        assertThat(req.tableName()).isEqualTo("Avaliacoes");
        assertThat(req.conditionExpression()).isEqualTo("attribute_not_exists(#sk)");
        assertThat(req.item())
                .containsEntry("dia", AttributeValue.fromS("2026-10-27"))
                .containsEntry("criadoEm#id", AttributeValue.fromS("2026-10-27T14:03:11Z#" + a.id()))
                .containsEntry("id", AttributeValue.fromS(a.id()))
                .containsEntry("descricao", AttributeValue.fromS("Áudio da aula cortando"))
                .containsEntry("nota", AttributeValue.fromN("2"))
                .containsEntry("urgencia", AttributeValue.fromS("CRITICA"))
                .containsEntry("dataEnvio", AttributeValue.fromS("2026-10-27T14:03:11Z"));
    }
}
