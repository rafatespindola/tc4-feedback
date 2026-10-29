package br.com.fiap.tc4.relatorio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import br.com.fiap.tc4.core.Avaliacao;
import br.com.fiap.tc4.core.Urgencia;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Relatório semanal: média das notas, quantidade por dia, quantidade por urgência e
 * a lista de avaliações (em ordem de envio).
 *
 * @param media média das notas com 2 casas, ou {@code null} se não houve avaliações
 */
@RegisterForReflection
public record Relatorio(
        LocalDate inicio,
        LocalDate fim,
        Instant geradoEm,
        int total,
        BigDecimal media,
        List<PorDia> porDia,
        List<PorUrgencia> porUrgencia,
        List<Item> avaliacoes) {

    public record PorDia(LocalDate dia, long quantidade) {
    }

    public record PorUrgencia(Urgencia urgencia, long quantidade) {
    }

    public record Item(String id, Instant dataEnvio, int nota, Urgencia urgencia, String descricao) {
        static Item de(Avaliacao a) {
            return new Item(a.id(), a.dataEnvio(), a.nota(), a.urgencia(), a.descricao());
        }
    }

    public static Relatorio gerar(Semana semana, List<Avaliacao> avaliacoes, Instant geradoEm) {
        List<Avaliacao> ordenadas = avaliacoes.stream()
                .sorted(Comparator.comparing(Avaliacao::dataEnvio))
                .toList();

        BigDecimal media = ordenadas.isEmpty() ? null
                : BigDecimal.valueOf(ordenadas.stream().mapToInt(Avaliacao::nota).sum())
                        .divide(BigDecimal.valueOf(ordenadas.size()), 2, RoundingMode.HALF_UP);

        // todos os dias e urgências aparecem, mesmo com quantidade zero
        List<PorDia> porDia = semana.dias().stream()
                .map(d -> new PorDia(d, ordenadas.stream().filter(a -> a.diaLocal().equals(d)).count()))
                .toList();
        List<PorUrgencia> porUrgencia = Arrays.stream(Urgencia.values())
                .map(u -> new PorUrgencia(u, ordenadas.stream().filter(a -> a.urgencia() == u).count()))
                .toList();

        return new Relatorio(semana.inicio(), semana.fim(), geradoEm, ordenadas.size(), media,
                porDia, porUrgencia, ordenadas.stream().map(Item::de).toList());
    }
}
