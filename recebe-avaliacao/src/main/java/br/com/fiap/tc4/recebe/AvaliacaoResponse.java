package br.com.fiap.tc4.recebe;

import java.time.Instant;

import br.com.fiap.tc4.core.Avaliacao;
import br.com.fiap.tc4.core.Urgencia;

public record AvaliacaoResponse(String id, String descricao, int nota, Urgencia urgencia, Instant dataEnvio) {

    static AvaliacaoResponse de(Avaliacao a) {
        return new AvaliacaoResponse(a.id(), a.descricao(), a.nota(), a.urgencia(), a.dataEnvio());
    }
}
