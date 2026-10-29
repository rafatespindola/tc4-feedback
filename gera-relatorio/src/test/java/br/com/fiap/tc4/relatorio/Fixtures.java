package br.com.fiap.tc4.relatorio;

import java.time.Instant;

import br.com.fiap.tc4.core.Avaliacao;
import br.com.fiap.tc4.core.Urgencia;

final class Fixtures {

    private Fixtures() {
    }

    static Avaliacao avaliacao(String id, String dataEnvio, int nota) {
        return new Avaliacao(id, "descrição " + id, nota, Urgencia.classificar(nota), Instant.parse(dataEnvio));
    }
}
