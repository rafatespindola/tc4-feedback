package br.com.fiap.tc4.relatorio;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

@ApplicationScoped
public class SnsPublicadorRelatorio implements PublicadorRelatorio {

    private final SnsClient sns;
    private final String topicoArn;

    public SnsPublicadorRelatorio(SnsClient sns, @ConfigProperty(name = "relatorio.topico-arn") String topicoArn) {
        this.sns = sns;
        this.topicoArn = topicoArn;
    }

    @Override
    public void publicar(EmailRelatorio email) {
        sns.publish(PublishRequest.builder()
                .topicArn(topicoArn)
                .subject(email.assunto())
                .message(email.mensagem())
                .build());
    }
}
