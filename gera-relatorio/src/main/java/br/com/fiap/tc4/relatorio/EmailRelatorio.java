package br.com.fiap.tc4.relatorio;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

import br.com.fiap.tc4.core.Avaliacao;

/** Texto do e-mail do relatório semanal (resumo + lista, limitada para caber no SNS). */
public record EmailRelatorio(String assunto, String mensagem) {

    static final int MAX_AVALIACOES_NO_EMAIL = 100;
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DIA_SEMANA = DateTimeFormatter.ofPattern("EEE dd/MM", Locale.of("pt", "BR"));
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    /** Assunto do SNS precisa ser ASCII e ter menos de 100 caracteres. */
    static EmailRelatorio de(Relatorio r, String chaveS3) {
        String assunto = "[TC4] Relatorio semanal %s a %s".formatted(DATA.format(r.inicio()), DATA.format(r.fim()));

        StringBuilder m = new StringBuilder();
        m.append("Relatório semanal de avaliações — %s a %s%n%n".formatted(DATA.format(r.inicio()), DATA.format(r.fim())));
        m.append("Total de avaliações: %d%n".formatted(r.total()));
        m.append("Média das notas:     %s%n%n".formatted(r.media() == null ? "—" : r.media().toPlainString().replace('.', ',')));

        m.append("Quantidade por dia:\n");
        r.porDia().forEach(d -> m.append("  %-11s %d%n".formatted(DIA_SEMANA.format(d.dia()), d.quantidade())));

        m.append("\nQuantidade por urgência:\n");
        r.porUrgencia().forEach(u -> m.append("  %-8s %d%n".formatted(u.urgencia(), u.quantidade())));

        m.append("\nAvaliações:\n");
        if (r.avaliacoes().isEmpty()) {
            m.append("  (nenhuma avaliação na semana)\n");
        }
        r.avaliacoes().stream().limit(MAX_AVALIACOES_NO_EMAIL).forEach(a -> m.append("  %s  nota %2d  %-7s  %s%n".formatted(
                DATA_HORA.format(a.dataEnvio().atZone(Avaliacao.FUSO)), a.nota(), a.urgencia(), a.descricao())));
        if (r.avaliacoes().size() > MAX_AVALIACOES_NO_EMAIL) {
            m.append("  ... e mais %d (lista completa no arquivo abaixo)%n"
                    .formatted(r.avaliacoes().size() - MAX_AVALIACOES_NO_EMAIL));
        }

        m.append("\nRelatório completo (JSON): s3://%s%n".formatted(chaveS3));
        m.append("Horários em Brasília.\n");
        return new EmailRelatorio(assunto, m.toString());
    }
}
