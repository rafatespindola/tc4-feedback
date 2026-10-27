package br.com.fiap.tc4.core;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

/**
 * Avaliação de aula enviada por um estudante.
 *
 * @param id        identificador único (UUID)
 * @param descricao texto livre, não vazio
 * @param nota      0–10
 * @param urgencia  derivada da nota por {@link Urgencia#classificar(int)}
 * @param dataEnvio instante do recebimento (UTC, precisão de segundos)
 */
public record Avaliacao(String id, String descricao, int nota, Urgencia urgencia, Instant dataEnvio) {

    public Avaliacao {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(dataEnvio, "dataEnvio");
        if (descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("descricao não pode ser vazia");
        }
        if (urgencia != Urgencia.classificar(nota)) {
            throw new IllegalArgumentException("urgencia %s não corresponde à nota %d".formatted(urgencia, nota));
        }
    }

    /** Cria uma nova avaliação, gerando id e data de envio e classificando a urgência. */
    public static Avaliacao nova(String descricao, int nota, Clock clock) {
        return new Avaliacao(
                UUID.randomUUID().toString(),
                descricao == null ? null : descricao.strip(),
                nota,
                Urgencia.classificar(nota),
                clock.instant().truncatedTo(ChronoUnit.SECONDS));
    }

    /** Partition key da tabela: dia (UTC) do envio, ex. {@code 2026-10-27}. */
    public String dia() {
        return LocalDate.ofInstant(dataEnvio, ZoneOffset.UTC).toString();
    }

    /** Sort key da tabela: {@code criadoEm#id}, ordena por horário dentro do dia. */
    public String criadoEmId() {
        return dataEnvio + "#" + id;
    }

    public boolean critica() {
        return urgencia == Urgencia.CRITICA;
    }
}
