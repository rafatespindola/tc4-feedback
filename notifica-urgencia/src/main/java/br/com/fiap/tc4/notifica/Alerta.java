package br.com.fiap.tc4.notifica;

import java.time.format.DateTimeFormatter;

import br.com.fiap.tc4.core.Avaliacao;

/** Conteúdo do alerta enviado aos administradores: descrição, urgência e data de envio. */
public record Alerta(String assunto, String mensagem) {

    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /** Assunto do SNS precisa ser ASCII e ter menos de 100 caracteres. */
    static Alerta de(Avaliacao a) {
        String assunto = "[TC4] Avaliacao CRITICA recebida (nota %d)".formatted(a.nota());
        String mensagem = """
                Uma avaliação crítica foi recebida e requer atenção.

                Descrição: %s
                Urgência:  %s
                Nota:      %d
                Data de envio: %s (horário de Brasília) — %s UTC
                Id: %s
                """.formatted(
                a.descricao(),
                a.urgencia(),
                a.nota(),
                DATA_HORA.format(a.dataEnvio().atZone(Avaliacao.FUSO)),
                a.dataEnvio(),
                a.id());
        return new Alerta(assunto, mensagem);
    }
}
