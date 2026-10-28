package br.com.fiap.tc4.notifica;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Evento do DynamoDB Stream (apenas os campos usados). Modelado aqui em vez de usar
 * {@code aws-lambda-java-events} para ter controle total da desserialização via Jackson.
 */
@RegisterForReflection
@JsonIgnoreProperties(ignoreUnknown = true)
public record StreamEvent(@JsonProperty("Records") List<Registro> registros) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Registro(String eventID, String eventName, Dados dynamodb) {
    }

    /** Cada atributo vem no formato do DynamoDB, ex. {@code {"S": "texto"}} ou {@code {"N": "2"}}. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Dados(
            @JsonProperty("SequenceNumber") String sequenceNumber,
            @JsonProperty("NewImage") Map<String, Map<String, String>> newImage) {
    }
}
