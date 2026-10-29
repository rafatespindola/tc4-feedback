package br.com.fiap.tc4.relatorio;

import java.io.UncheckedIOException;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@ApplicationScoped
public class S3ArmazenamentoRelatorio implements ArmazenamentoRelatorio {

    private final S3Client s3;
    private final ObjectMapper mapper;
    private final String bucket;

    public S3ArmazenamentoRelatorio(S3Client s3, ObjectMapper mapper,
            @ConfigProperty(name = "relatorios.bucket") String bucket) {
        this.s3 = s3;
        this.mapper = mapper;
        this.bucket = bucket;
    }

    @Override
    public String salvar(Relatorio relatorio) {
        String chave = chave(relatorio);
        s3.putObject(PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(chave)
                        .contentType("application/json; charset=utf-8")
                        .build(),
                RequestBody.fromBytes(json(relatorio)));
        return bucket + "/" + chave;
    }

    /** Ex.: {@code relatorios/2026/semana-2026-10-26.json}. Reexecutar a mesma semana sobrescreve. */
    static String chave(Relatorio r) {
        return "relatorios/%d/semana-%s.json".formatted(r.inicio().getYear(), r.inicio());
    }

    private byte[] json(Relatorio relatorio) {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(relatorio);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }
}
