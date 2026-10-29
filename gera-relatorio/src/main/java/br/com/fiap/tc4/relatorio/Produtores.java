package br.com.fiap.tc4.relatorio;

import java.time.Clock;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sns.SnsClient;

/** Região e credenciais dos clientes vêm do ambiente da Lambda (AWS_REGION, cadeia padrão). */
public class Produtores {

    @Produces
    @ApplicationScoped
    DynamoDbClient dynamoDbClient() {
        return DynamoDbClient.builder().httpClientBuilder(UrlConnectionHttpClient.builder()).build();
    }

    @Produces
    @ApplicationScoped
    S3Client s3Client() {
        return S3Client.builder().httpClientBuilder(UrlConnectionHttpClient.builder()).build();
    }

    @Produces
    @ApplicationScoped
    SnsClient snsClient() {
        return SnsClient.builder().httpClientBuilder(UrlConnectionHttpClient.builder()).build();
    }

    void fechar(@Disposes DynamoDbClient client) {
        client.close();
    }

    void fechar(@Disposes S3Client client) {
        client.close();
    }

    void fechar(@Disposes SnsClient client) {
        client.close();
    }

    @Produces
    @Singleton
    Clock clock() {
        return Clock.systemUTC();
    }
}
