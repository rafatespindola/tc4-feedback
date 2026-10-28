package br.com.fiap.tc4.notifica;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;

import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.services.sns.SnsClient;

public class Produtores {

    /** Região e credenciais vêm do ambiente da Lambda (AWS_REGION, cadeia padrão). */
    @Produces
    @ApplicationScoped
    SnsClient snsClient() {
        return SnsClient.builder()
                .httpClientBuilder(UrlConnectionHttpClient.builder())
                .build();
    }

    void fechar(@Disposes SnsClient client) {
        client.close();
    }
}
