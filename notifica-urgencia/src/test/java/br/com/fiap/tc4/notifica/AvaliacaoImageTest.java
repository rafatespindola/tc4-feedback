package br.com.fiap.tc4.notifica;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import br.com.fiap.tc4.core.Avaliacao;
import br.com.fiap.tc4.core.Urgencia;

class AvaliacaoImageTest {

    static Map<String, Map<String, String>> image(String nota, String urgencia) {
        Map<String, Map<String, String>> m = new HashMap<>();
        m.put("id", Map.of("S", "abc"));
        m.put("descricao", Map.of("S", "Áudio da aula cortando"));
        m.put("nota", Map.of("N", nota));
        m.put("urgencia", Map.of("S", urgencia));
        m.put("dataEnvio", Map.of("S", "2026-10-27T23:12:29Z"));
        return m;
    }

    @Test
    void converteNewImage() {
        Avaliacao a = AvaliacaoImage.paraAvaliacao(image("2", "CRITICA"));

        assertThat(a.id()).isEqualTo("abc");
        assertThat(a.descricao()).isEqualTo("Áudio da aula cortando");
        assertThat(a.nota()).isEqualTo(2);
        assertThat(a.urgencia()).isEqualTo(Urgencia.CRITICA);
        assertThat(a.dataEnvio()).isEqualTo(Instant.parse("2026-10-27T23:12:29Z"));
    }

    @Test
    void rejeitaAtributoAusente() {
        Map<String, Map<String, String>> m = image("2", "CRITICA");
        m.remove("descricao");
        assertThatIllegalArgumentException().isThrownBy(() -> AvaliacaoImage.paraAvaliacao(m))
                .withMessageContaining("descricao");
    }

    @Test
    void rejeitaTipoErrado() {
        Map<String, Map<String, String>> m = image("2", "CRITICA");
        m.put("nota", Map.of("S", "2"));
        assertThatIllegalArgumentException().isThrownBy(() -> AvaliacaoImage.paraAvaliacao(m))
                .withMessageContaining("nota");
    }

    @Test
    void rejeitaSemImage() {
        assertThatIllegalArgumentException().isThrownBy(() -> AvaliacaoImage.paraAvaliacao(null));
    }

    @Test
    void rejeitaUrgenciaInconsistenteComNota() {
        assertThatIllegalArgumentException().isThrownBy(() -> AvaliacaoImage.paraAvaliacao(image("9", "CRITICA")));
    }
}
