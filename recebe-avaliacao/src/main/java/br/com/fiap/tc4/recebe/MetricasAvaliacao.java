package br.com.fiap.tc4.recebe;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.fiap.tc4.core.Avaliacao;

/**
 * Publica métricas no CloudWatch via Embedded Metric Format (EMF): uma linha JSON no stdout
 * que o Lambda converte em métrica, sem chamada de API nem permissão extra.
 *
 * <p>Namespace {@code TC4Feedback}: {@code Avaliacoes} (contagem) e {@code Nota}, por
 * {@code Urgencia} e no total.
 */
@ApplicationScoped
public class MetricasAvaliacao {

    static final String NAMESPACE = "TC4Feedback";

    private final ObjectMapper mapper;
    private final PrintStream saida = System.out;

    public MetricasAvaliacao(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public void registrar(Avaliacao avaliacao) {
        try {
            saida.println(mapper.writeValueAsString(emf(avaliacao)));
        } catch (JsonProcessingException e) {
            // métrica nunca deve derrubar o recebimento
        }
    }

    static Map<String, Object> emf(Avaliacao a) {
        return Map.of(
                "_aws", Map.of(
                        "Timestamp", a.dataEnvio().toEpochMilli(),
                        "CloudWatchMetrics", List.of(Map.of(
                                "Namespace", NAMESPACE,
                                "Dimensions", List.of(List.of("Urgencia"), List.of()),
                                "Metrics", List.of(
                                        Map.of("Name", "Avaliacoes", "Unit", "Count"),
                                        Map.of("Name", "Nota", "Unit", "None"))))),
                "Urgencia", a.urgencia().name(),
                "Avaliacoes", 1,
                "Nota", a.nota());
    }
}
