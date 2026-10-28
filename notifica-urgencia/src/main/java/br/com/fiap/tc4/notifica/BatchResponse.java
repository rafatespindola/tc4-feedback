package br.com.fiap.tc4.notifica;

import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;

/** Resposta de falha parcial ({@code ReportBatchItemFailures}): só os registros listados são reprocessados. */
@RegisterForReflection
public record BatchResponse(List<Falha> batchItemFailures) {

    public record Falha(String itemIdentifier) {
    }
}
