package br.com.fiap.tc4.relatorio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.time.LocalDate;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

class SemanaTest {

    @ParameterizedTest(name = "{0} -> semana de {1}")
    @CsvSource({
        "2026-11-02, 2026-10-26",   // segunda (dia da execução agendada)
        "2026-11-01, 2026-10-19",   // domingo: semana atual ainda não fechou
        "2026-11-04, 2026-10-26",   // quarta
        "2026-01-05, 2025-12-29"    // virada de ano
    })
    void semanaAnteriorFechada(LocalDate hoje, LocalDate inicioEsperado) {
        Semana s = Semana.anteriorA(hoje);

        assertThat(s.inicio()).isEqualTo(inicioEsperado);
        assertThat(s.fim()).isEqualTo(inicioEsperado.plusDays(6));
    }

    @Test
    void seteDiasDeSegundaADomingo() {
        assertThat(new Semana(LocalDate.of(2026, 10, 26)).dias())
                .hasSize(7)
                .startsWith(LocalDate.of(2026, 10, 26))
                .endsWith(LocalDate.of(2026, 11, 1));
    }

    @Test
    void exigeSegundaFeira() {
        assertThatIllegalArgumentException().isThrownBy(() -> new Semana(LocalDate.of(2026, 10, 27)));
    }
}
