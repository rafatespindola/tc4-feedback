package br.com.fiap.tc4.relatorio;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.Test;

import br.com.fiap.tc4.core.Avaliacao;
import br.com.fiap.tc4.core.Urgencia;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

class DynamoDbAvaliacaoConsultaTest {

    @Test
    void converteItemDaTabela() {
        Avaliacao a = DynamoDbAvaliacaoConsulta.avaliacao(Map.of(
                "dia", AttributeValue.fromS("2026-10-27"),
                "criadoEm#id", AttributeValue.fromS("2026-10-27T23:12:29Z#abc"),
                "id", AttributeValue.fromS("abc"),
                "descricao", AttributeValue.fromS("Áudio da aula cortando"),
                "nota", AttributeValue.fromN("2"),
                "urgencia", AttributeValue.fromS("CRITICA"),
                "dataEnvio", AttributeValue.fromS("2026-10-27T23:12:29Z")));

        assertThat(a).isEqualTo(new Avaliacao("abc", "Áudio da aula cortando", 2, Urgencia.CRITICA,
                Instant.parse("2026-10-27T23:12:29Z")));
    }
}
