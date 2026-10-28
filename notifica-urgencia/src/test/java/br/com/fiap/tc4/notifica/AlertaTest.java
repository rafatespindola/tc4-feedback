package br.com.fiap.tc4.notifica;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import org.junit.jupiter.api.Test;

import br.com.fiap.tc4.core.Avaliacao;
import br.com.fiap.tc4.core.Urgencia;

class AlertaTest {

    private final Avaliacao avaliacao = new Avaliacao(
            "85c3f24c", "Áudio da aula cortando", 2, Urgencia.CRITICA, Instant.parse("2026-10-27T23:12:29Z"));

    @Test
    void mensagemTemDescricaoUrgenciaEDataDeEnvio() {
        Alerta alerta = Alerta.de(avaliacao);

        assertThat(alerta.mensagem())
                .contains("Descrição: Áudio da aula cortando")
                .contains("Urgência:  CRITICA")
                .contains("Nota:      2")
                .contains("27/10/2026 20:12:29 (horário de Brasília)")
                .contains("2026-10-27T23:12:29Z UTC")
                .contains("Id: 85c3f24c");
    }

    @Test
    void assuntoValidoParaSns() {
        String assunto = Alerta.de(avaliacao).assunto();

        assertThat(assunto).isEqualTo("[TC4] Avaliacao CRITICA recebida (nota 2)");
        assertThat(assunto.length()).isLessThan(100);
        assertThat(StandardCharsets.US_ASCII.newEncoder().canEncode(assunto)).isTrue();
    }
}
