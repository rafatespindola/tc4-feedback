package br.com.fiap.tc4.relatorio;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Stream;

/** Semana fechada de segunda a domingo (horário de Brasília). */
public record Semana(LocalDate inicio) {

    public Semana {
        if (inicio.getDayOfWeek() != DayOfWeek.MONDAY) {
            throw new IllegalArgumentException("a semana deve começar numa segunda-feira: " + inicio);
        }
    }

    /** Semana completa anterior à data informada (ex.: segunda 02/11 → 26/10 a 01/11). */
    public static Semana anteriorA(LocalDate hoje) {
        return new Semana(hoje.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusWeeks(1));
    }

    public LocalDate fim() {
        return inicio.plusDays(6);
    }

    public List<LocalDate> dias() {
        return Stream.iterate(inicio, d -> d.plusDays(1)).limit(7).toList();
    }
}
