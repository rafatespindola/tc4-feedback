package br.com.fiap.tc4.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class AvaliacaoTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-10-27T14:03:11.789Z"), ZoneOffset.UTC);

    @Test
    void novaClassificaEGeraCampos() {
        Avaliacao a = Avaliacao.nova("  Áudio da aula cortando ", 2, clock);

        assertThat(a.id()).hasSize(36);
        assertThat(a.descricao()).isEqualTo("Áudio da aula cortando");
        assertThat(a.nota()).isEqualTo(2);
        assertThat(a.urgencia()).isEqualTo(Urgencia.CRITICA);
        assertThat(a.critica()).isTrue();
        assertThat(a.dataEnvio()).isEqualTo(Instant.parse("2026-10-27T14:03:11Z"));
    }

    @Test
    void chavesDaTabela() {
        Avaliacao a = Avaliacao.nova("Ótima aula", 9, clock);

        assertThat(a.dia()).isEqualTo("2026-10-27");
        assertThat(a.criadoEmId()).isEqualTo("2026-10-27T14:03:11Z#" + a.id());
        assertThat(a.critica()).isFalse();
    }

    @Test
    void diaUsaHorarioDeBrasilia() {
        // 01:30 UTC de 28/10 = 22:30 de 27/10 em Brasília
        Clock noitePorBrasilia = Clock.fixed(Instant.parse("2026-10-28T01:30:00Z"), ZoneOffset.UTC);
        Avaliacao a = Avaliacao.nova("x", 5, noitePorBrasilia);

        assertThat(a.dia()).isEqualTo("2026-10-27");
        assertThat(a.diaLocal()).isEqualTo(LocalDate.of(2026, 10, 27));
        // a sort key continua em UTC (instante absoluto)
        assertThat(a.criadoEmId()).startsWith("2026-10-28T01:30:00Z#");
    }

    @Test
    void viradaDoDiaEmBrasilia() {
        Clock meiaNoiteBrt = Clock.fixed(Instant.parse("2026-10-28T03:00:00Z"), ZoneOffset.UTC);
        assertThat(Avaliacao.nova("x", 5, meiaNoiteBrt).dia()).isEqualTo("2026-10-28");
    }

    @Test
    void idsSaoUnicos() {
        assertThat(Avaliacao.nova("x", 5, clock).id()).isNotEqualTo(Avaliacao.nova("x", 5, clock).id());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void rejeitaDescricaoVazia(String descricao) {
        assertThatIllegalArgumentException().isThrownBy(() -> Avaliacao.nova(descricao, 5, clock));
    }

    @Test
    void rejeitaNotaInvalida() {
        assertThatIllegalArgumentException().isThrownBy(() -> Avaliacao.nova("x", 11, clock));
    }

    @Test
    void rejeitaUrgenciaInconsistente() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Avaliacao("id", "x", 9, Urgencia.CRITICA, Instant.now()));
    }
}
