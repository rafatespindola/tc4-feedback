package br.com.fiap.tc4.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class UrgenciaTest {

    @ParameterizedTest(name = "nota {0} -> {1}")
    @CsvSource({
        "0, CRITICA", "1, CRITICA", "2, CRITICA", "3, CRITICA",
        "4, MEDIA", "5, MEDIA", "6, MEDIA",
        "7, BAIXA", "8, BAIXA", "9, BAIXA", "10, BAIXA"
    })
    void classificaPelaNota(int nota, Urgencia esperada) {
        assertThat(Urgencia.classificar(nota)).isEqualTo(esperada);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 11, Integer.MIN_VALUE, Integer.MAX_VALUE})
    void rejeitaNotaForaDaFaixa(int nota) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> Urgencia.classificar(nota))
                .withMessageContaining("entre 0 e 10");
    }
}
