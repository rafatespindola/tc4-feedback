package br.com.fiap.tc4.recebe;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.fiap.tc4.core.Avaliacao;
import br.com.fiap.tc4.core.Urgencia;

class MetricasAvaliacaoTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void geraEmbeddedMetricFormat() throws Exception {
        Avaliacao a = new Avaliacao("id", "x", 2, Urgencia.CRITICA, Instant.parse("2026-10-28T12:00:00Z"));

        JsonNode emf = mapper.readTree(mapper.writeValueAsString(MetricasAvaliacao.emf(a)));

        JsonNode diretiva = emf.at("/_aws/CloudWatchMetrics/0");
        assertThat(emf.at("/_aws/Timestamp").asLong()).isEqualTo(Instant.parse("2026-10-28T12:00:00Z").toEpochMilli());
        assertThat(diretiva.get("Namespace").asText()).isEqualTo("TC4Feedback");
        assertThat(diretiva.get("Dimensions").toString()).isEqualTo("[[\"Urgencia\"],[]]");
        assertThat(diretiva.at("/Metrics/0/Name").asText()).isEqualTo("Avaliacoes");
        assertThat(diretiva.at("/Metrics/1/Name").asText()).isEqualTo("Nota");
        assertThat(emf.get("Urgencia").asText()).isEqualTo("CRITICA");
        assertThat(emf.get("Avaliacoes").asInt()).isEqualTo(1);
        assertThat(emf.get("Nota").asInt()).isEqualTo(2);
    }
}
