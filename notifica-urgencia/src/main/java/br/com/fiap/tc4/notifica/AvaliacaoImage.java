package br.com.fiap.tc4.notifica;

import java.time.Instant;
import java.util.Map;

import br.com.fiap.tc4.core.Avaliacao;
import br.com.fiap.tc4.core.Urgencia;

/** Converte a NewImage do stream (formato DynamoDB) em {@link Avaliacao}. */
final class AvaliacaoImage {

    private AvaliacaoImage() {
    }

    static Avaliacao paraAvaliacao(Map<String, Map<String, String>> image) {
        if (image == null) {
            throw new IllegalArgumentException("registro sem NewImage");
        }
        return new Avaliacao(
                texto(image, "id", "S"),
                texto(image, "descricao", "S"),
                Integer.parseInt(texto(image, "nota", "N")),
                Urgencia.valueOf(texto(image, "urgencia", "S")),
                Instant.parse(texto(image, "dataEnvio", "S")));
    }

    private static String texto(Map<String, Map<String, String>> image, String atributo, String tipo) {
        Map<String, String> valor = image.get(atributo);
        if (valor == null || valor.get(tipo) == null) {
            throw new IllegalArgumentException("atributo ausente ou com tipo inesperado: " + atributo);
        }
        return valor.get(tipo);
    }
}
