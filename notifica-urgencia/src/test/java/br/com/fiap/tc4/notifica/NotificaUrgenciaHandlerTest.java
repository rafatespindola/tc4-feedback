package br.com.fiap.tc4.notifica;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class NotificaUrgenciaHandlerTest {

    @Inject
    NotificaUrgenciaHandler handler;

    @Inject
    ObjectMapper mapper;

    @InjectMock
    PublicadorAlerta publicador;

    private StreamEvent eventoDeExemplo() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/evento-stream.json")) {
            return mapper.readValue(in, StreamEvent.class);
        }
    }

    @Test
    void desserializaEventoRealDoStream() throws IOException {
        StreamEvent evento = eventoDeExemplo();

        assertThat(evento.registros()).hasSize(2);
        StreamEvent.Registro r = evento.registros().get(0);
        assertThat(r.eventName()).isEqualTo("INSERT");
        assertThat(r.dynamodb().sequenceNumber()).isEqualTo("111");
        assertThat(r.dynamodb().newImage().get("nota")).isEqualTo(Map.of("N", "2"));
    }

    @Test
    void publicaUmAlertaPorAvaliacaoCritica() throws IOException {
        BatchResponse resposta = handler.handleRequest(eventoDeExemplo(), null);

        ArgumentCaptor<Alerta> captor = ArgumentCaptor.forClass(Alerta.class);
        verify(publicador, times(2)).publicar(captor.capture());
        assertThat(captor.getAllValues().get(0).mensagem()).contains("Áudio da aula cortando");
        assertThat(captor.getAllValues().get(1).mensagem()).contains("Professor não apareceu");
        assertThat(resposta.batchItemFailures()).isEmpty();
    }

    @Test
    void reportaApenasORegistroQueFalhou() throws IOException {
        doThrow(new RuntimeException("SNS indisponível"))
                .when(publicador).publicar(argThat(a -> a.mensagem().contains("Professor não apareceu")));

        BatchResponse resposta = handler.handleRequest(eventoDeExemplo(), null);

        assertThat(resposta.batchItemFailures()).containsExactly(new BatchResponse.Falha("222"));
    }

    @Test
    void ignoraAvaliacaoNaoCritica() {
        StreamEvent evento = new StreamEvent(List.of(new StreamEvent.Registro("e1", "INSERT",
                new StreamEvent.Dados("1", AvaliacaoImageTest.image("9", "BAIXA")))));

        BatchResponse resposta = handler.handleRequest(evento, null);

        verify(publicador, never()).publicar(any());
        assertThat(resposta.batchItemFailures()).isEmpty();
    }

    @Test
    void descartaRegistroMalformadoSemReprocessar() {
        StreamEvent evento = new StreamEvent(List.of(new StreamEvent.Registro("e1", "INSERT",
                new StreamEvent.Dados("1", Map.of("id", Map.of("S", "x"))))));

        BatchResponse resposta = handler.handleRequest(evento, null);

        verify(publicador, never()).publicar(any());
        assertThat(resposta.batchItemFailures()).isEmpty();
    }

    @Test
    void eventoVazio() {
        assertThat(handler.handleRequest(new StreamEvent(null), null).batchItemFailures()).isEmpty();
    }
}
