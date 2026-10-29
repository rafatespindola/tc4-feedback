package br.com.fiap.tc4.relatorio;

import static br.com.fiap.tc4.relatorio.Fixtures.avaliacao;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.test.InjectMock;
import io.quarkus.test.Mock;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class GeraRelatorioHandlerTest {

    /** Segunda-feira 02/11/2026 08:00 em Brasília (11:00 UTC). */
    static final Instant AGORA = Instant.parse("2026-11-02T11:00:00Z");

    @Mock
    static class RelogioFixo {
        @Produces
        @Singleton
        Clock clock() {
            return Clock.fixed(AGORA, ZoneOffset.UTC);
        }
    }

    @Inject
    GeraRelatorioHandler handler;

    @Inject
    ObjectMapper mapper;

    @InjectMock
    AvaliacaoConsulta consulta;

    @InjectMock
    ArmazenamentoRelatorio armazenamento;

    @InjectMock
    PublicadorRelatorio publicador;

    @BeforeEach
    void dados() {
        when(consulta.doDia(any())).thenReturn(List.of());
        when(consulta.doDia(LocalDate.of(2026, 10, 26))).thenReturn(List.of(avaliacao("a", "2026-10-26T12:00:00Z", 2)));
        when(consulta.doDia(LocalDate.of(2026, 10, 30))).thenReturn(List.of(avaliacao("b", "2026-10-30T12:00:00Z", 9)));
        when(armazenamento.salvar(any())).thenReturn("bucket/relatorios/2026/semana-2026-10-26.json");
    }

    @Test
    void execucaoAgendadaUsaSemanaAnterior() {
        Resultado resultado = handler.handleRequest(new Execucao(null), null);

        assertThat(resultado).isEqualTo(new Resultado(LocalDate.of(2026, 10, 26), LocalDate.of(2026, 11, 1), 2,
                new BigDecimal("5.50"), "bucket/relatorios/2026/semana-2026-10-26.json"));

        // uma Query por dia, de segunda a domingo
        verify(consulta, times(7)).doDia(any());
        new Semana(LocalDate.of(2026, 10, 26)).dias().forEach(d -> verify(consulta).doDia(d));

        InOrder ordem = inOrder(armazenamento, publicador);
        ArgumentCaptor<Relatorio> relatorio = ArgumentCaptor.forClass(Relatorio.class);
        ordem.verify(armazenamento).salvar(relatorio.capture());
        ArgumentCaptor<EmailRelatorio> email = ArgumentCaptor.forClass(EmailRelatorio.class);
        ordem.verify(publicador).publicar(email.capture());

        assertThat(relatorio.getValue().geradoEm()).isEqualTo(AGORA);
        assertThat(email.getValue().assunto()).isEqualTo("[TC4] Relatorio semanal 26/10/2026 a 01/11/2026");
    }

    @Test
    void reprocessaSemanaInformada() {
        Resultado resultado = handler.handleRequest(new Execucao("2026-10-19"), null);

        assertThat(resultado.inicio()).isEqualTo(LocalDate.of(2026, 10, 19));
        verify(consulta).doDia(LocalDate.of(2026, 10, 25));
        verify(consulta, never()).doDia(LocalDate.of(2026, 10, 26));
    }

    @Test
    void rejeitaInicioQueNaoEhSegunda() {
        assertThatIllegalArgumentException().isThrownBy(() -> handler.handleRequest(new Execucao("2026-10-20"), null));
        verify(publicador, never()).publicar(any());
    }

    @Test
    void entradaDoAgendamentoEhObjetoVazio() throws Exception {
        assertThat(mapper.readValue("{}", Execucao.class).inicio()).isNull();
    }

    @Test
    void jsonDoRelatorioUsaDatasIso() throws Exception {
        Relatorio r = Relatorio.gerar(new Semana(LocalDate.of(2026, 10, 26)),
                List.of(avaliacao("a", "2026-10-26T12:00:00Z", 2)), AGORA);

        String json = mapper.writeValueAsString(r);

        assertThat(json)
                .contains("\"inicio\":\"2026-10-26\"")
                .contains("\"geradoEm\":\"2026-11-02T11:00:00Z\"")
                .contains("\"media\":2.00")
                .contains("{\"dia\":\"2026-10-26\",\"quantidade\":1}")
                .contains("{\"urgencia\":\"CRITICA\",\"quantidade\":1}");
        assertThat(S3ArmazenamentoRelatorio.chave(r)).isEqualTo("relatorios/2026/semana-2026-10-26.json");
    }
}
