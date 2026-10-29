package br.com.fiap.tc4.relatorio;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Entrada da função. O agendamento envia {@code {}} (semana anterior); para reprocessar
 * uma semana específica, invoque com {@code {"inicio": "2026-10-26"}} (uma segunda-feira).
 */
@RegisterForReflection
@JsonIgnoreProperties(ignoreUnknown = true)
public record Execucao(String inicio) {
}
