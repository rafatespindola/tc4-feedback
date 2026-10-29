package br.com.fiap.tc4.relatorio;

import static br.com.fiap.tc4.relatorio.Fixtures.avaliacao;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import br.com.fiap.tc4.core.Avaliacao;

class EmailRelatorioTest {

    private final Semana semana = new Semana(LocalDate.of(2026, 10, 26));
    private final Instant agora = Instant.parse("2026-11-02T11:00:00Z");

    @Test
    void resumoCompleto() {
        Relatorio r = Relatorio.gerar(semana, List.of(
                avaliacao("a", "2026-10-26T12:00:00Z", 2),
                avaliacao("b", "2026-10-28T15:30:00Z", 9)), agora);

        EmailRelatorio e = EmailRelatorio.de(r, "bucket/relatorios/2026/semana-2026-10-26.json");

        assertThat(e.assunto()).isEqualTo("[TC4] Relatorio semanal 26/10/2026 a 01/11/2026");
        assertThat(StandardCharsets.US_ASCII.newEncoder().canEncode(e.assunto())).isTrue();
        assertThat(e.mensagem())
                .contains("Total de avaliações: 2")
                .contains("Média das notas:     5,50")
                .contains("seg. 26/10  1")
                .contains("ter. 27/10  0")
                .contains("CRITICA  1")
                .contains("MEDIA    0")
                .contains("BAIXA    1")
                .contains("26/10 09:00  nota  2  CRITICA  descrição a")
                .contains("28/10 12:30  nota  9  BAIXA    descrição b")
                .contains("s3://bucket/relatorios/2026/semana-2026-10-26.json");
    }

    @Test
    void semanaVazia() {
        EmailRelatorio e = EmailRelatorio.de(Relatorio.gerar(semana, List.of(), agora), "b/k");

        assertThat(e.mensagem())
                .contains("Total de avaliações: 0")
                .contains("Média das notas:     —")
                .contains("(nenhuma avaliação na semana)");
    }

    @Test
    void limitaListaNoEmail() {
        List<Avaliacao> muitas = IntStream.range(0, 130)
                .mapToObj(i -> avaliacao("id" + i, Instant.parse("2026-10-27T12:00:00Z").plusSeconds(i).toString(), 7))
                .toList();

        EmailRelatorio e = EmailRelatorio.de(Relatorio.gerar(semana, muitas, agora), "b/k");

        assertThat(e.mensagem()).contains("descrição id99").doesNotContain("descrição id100")
                .contains("... e mais 30");
    }
}
