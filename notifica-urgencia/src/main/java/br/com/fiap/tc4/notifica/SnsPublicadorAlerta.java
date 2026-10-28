package br.com.fiap.tc4.notifica;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

@ApplicationScoped
public class SnsPublicadorAlerta implements PublicadorAlerta {

    private final SnsClient sns;
    private final String topicoArn;

    public SnsPublicadorAlerta(SnsClient sns, @ConfigProperty(name = "alertas.topico-arn") String topicoArn) {
        this.sns = sns;
        this.topicoArn = topicoArn;
    }

    @Override
    public void publicar(Alerta alerta) {
        sns.publish(PublishRequest.builder()
                .topicArn(topicoArn)
                .subject(alerta.assunto())
                .message(alerta.mensagem())
                .build());
    }
}
