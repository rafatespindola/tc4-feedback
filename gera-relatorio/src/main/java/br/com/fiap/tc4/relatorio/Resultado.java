package br.com.fiap.tc4.relatorio;

import java.math.BigDecimal;
import java.time.LocalDate;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record Resultado(LocalDate inicio, LocalDate fim, int total, BigDecimal media, String arquivo) {
}
