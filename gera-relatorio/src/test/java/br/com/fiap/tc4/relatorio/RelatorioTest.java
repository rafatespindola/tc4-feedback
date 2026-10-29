package br.com.fiap.tc4.relatorio;

import static br.com.fiap.tc4.relatorio.Fixtures.avaliacao;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.com.fiap.tc4.core.Urgencia;
import br.com.fiap.tc4.relatorio.Relatorio.PorDia;
import br.com.fiap.tc4.relatorio.Relatorio.PorUrgencia;

class RelatorioTest {

    private final Semana semana = new Semana(LocalDate.of(2026, 10, 26));
    private final Instant agora = Instant.parse("2026-11-02T11:00:00Z");

    @Test
    void agregaMediaPorDiaEPorUrgencia() {
        Relatorio r = Relatorio.gerar(semana, List.of(
                avaliacao("c", "2026-10-28T15:00:00Z", 9),
                avaliacao("a", "2026-10-26T12:00:00Z", 2),
                avaliacao("b", "2026-10-26T13:00:00Z", 5),
                // 01:00 UTC de 02/11 = 22:00 de 01/11 em Brasília → domingo
                avaliacao("d", "2026-11-02T01:00:00Z", 10)), agora);

        assertThat(r.inicio()).isEqualTo(LocalDate.of(2026, 10, 26));
        assertThat(r.fim()).isEqualTo(LocalDate.of(2026, 11, 1));
        assertThat(r.geradoEm()).isEqualTo(agora);
        assertThat(r.total()).isEqualTo(4);
        assertThat(r.media()).isEqualByComparingTo(new BigDecimal("6.50"));
        assertThat(r.porDia()).containsExactly(
                new PorDia(LocalDate.of(2026, 10, 26), 2),
                new PorDia(LocalDate.of(2026, 10, 27), 0),
                new PorDia(LocalDate.of(2026, 10, 28), 1),
                new PorDia(LocalDate.of(2026, 10, 29), 0),
                new PorDia(LocalDate.of(2026, 10, 30), 0),
                new PorDia(LocalDate.of(2026, 10, 31), 0),
                new PorDia(LocalDate.of(2026, 11, 1), 1));
        assertThat(r.porUrgencia()).containsExactly(
                new PorUrgencia(Urgencia.CRITICA, 1),
                new PorUrgencia(Urgencia.MEDIA, 1),
                new PorUrgencia(Urgencia.BAIXA, 2));
        assertThat(r.avaliacoes()).extracting(Relatorio.Item::id).containsExactly("a", "b", "c", "d");
    }

    @Test
    void mediaComDuasCasas() {
        Relatorio r = Relatorio.gerar(semana, List.of(
                avaliacao("a", "2026-10-26T12:00:00Z", 1),
                avaliacao("b", "2026-10-26T12:00:01Z", 2),
                avaliacao("c", "2026-10-26T12:00:02Z", 2)), agora);

        assertThat(r.media()).isEqualTo(new BigDecimal("1.67"));
    }

    @Test
    void semanaSemAvaliacoes() {
        Relatorio r = Relatorio.gerar(semana, List.of(), agora);

        assertThat(r.total()).isZero();
        assertThat(r.media()).isNull();
        assertThat(r.porDia()).hasSize(7).allMatch(d -> d.quantidade() == 0);
        assertThat(r.porUrgencia()).hasSize(3).allMatch(u -> u.quantidade() == 0);
        assertThat(r.avaliacoes()).isEmpty();
    }
}
