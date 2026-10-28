package br.com.fiap.tc4.notifica;

import java.time.DateTimeException;
import java.util.ArrayList;
import java.util.List;

import org.jboss.logging.Logger;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import br.com.fiap.tc4.core.Avaliacao;

/**
 * Recebe do DynamoDB Stream apenas inserções com {@code urgencia = CRITICA} (filtro na
 * event source mapping) e publica um alerta por avaliação. Falhas são reportadas por
 * registro, para que só eles sejam reprocessados.
 */
public class NotificaUrgenciaHandler implements RequestHandler<StreamEvent, BatchResponse> {

    private static final Logger LOG = Logger.getLogger(NotificaUrgenciaHandler.class);

    private final PublicadorAlerta publicador;

    public NotificaUrgenciaHandler(PublicadorAlerta publicador) {
        this.publicador = publicador;
    }

    @Override
    public BatchResponse handleRequest(StreamEvent evento, Context context) {
        List<BatchResponse.Falha> falhas = new ArrayList<>();
        if (evento == null || evento.registros() == null) {
            return new BatchResponse(falhas);
        }
        for (StreamEvent.Registro registro : evento.registros()) {
            try {
                Avaliacao avaliacao = AvaliacaoImage.paraAvaliacao(registro.dynamodb().newImage());
                if (!avaliacao.critica()) {
                    LOG.warnf("registro ignorado (urgencia %s) eventID=%s", avaliacao.urgencia(), registro.eventID());
                    continue;
                }
                publicador.publicar(Alerta.de(avaliacao));
                LOG.infof("alerta publicado id=%s nota=%d", avaliacao.id(), avaliacao.nota());
            } catch (IllegalArgumentException | DateTimeException e) {
                // registro malformado: reprocessar não resolve, então só registra
                LOG.errorf(e, "registro invalido descartado eventID=%s", registro.eventID());
            } catch (RuntimeException e) {
                LOG.errorf(e, "falha ao publicar alerta eventID=%s", registro.eventID());
                falhas.add(new BatchResponse.Falha(registro.dynamodb().sequenceNumber()));
            }
        }
        return new BatchResponse(falhas);
    }
}
