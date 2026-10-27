package br.com.fiap.tc4.recebe;

import java.time.Clock;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.jackson.ObjectMapperCustomizer;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class Produtores {

    /** Região e credenciais vêm do ambiente da Lambda (AWS_REGION, cadeia padrão). */
    @Produces
    @ApplicationScoped
    DynamoDbClient dynamoDbClient() {
        return DynamoDbClient.builder()
                .httpClientBuilder(UrlConnectionHttpClient.builder())
                .build();
    }

    void fechar(@Disposes DynamoDbClient client) {
        client.close();
    }

    @Produces
    @Singleton
    Clock clock() {
        return Clock.systemUTC();
    }

    /** Rejeita {@code "nota": 2.5} (por padrão o Jackson truncaria para 2). */
    @Singleton
    static class JacksonConfig implements ObjectMapperCustomizer {
        @Override
        public void customize(ObjectMapper mapper) {
            mapper.disable(DeserializationFeature.ACCEPT_FLOAT_AS_INT);
        }
    }
}
