package br.com.fiap.tc4.recebe;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import br.com.fiap.tc4.core.Avaliacao;
import br.com.fiap.tc4.core.Urgencia;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
class AvaliacaoResourceTest {

    @InjectMock
    AvaliacaoRepository repository;

    @Test
    void notaBaixaEhCritica() {
        given().contentType(ContentType.JSON)
                .body("{\"descricao\":\"Áudio da aula cortando\",\"nota\":2}")
                .when().post("/avaliacao")
                .then().statusCode(201)
                .body("id", notNullValue())
                .body("nota", equalTo(2))
                .body("urgencia", equalTo("CRITICA"))
                .body("dataEnvio", notNullValue());

        ArgumentCaptor<Avaliacao> captor = ArgumentCaptor.forClass(Avaliacao.class);
        verify(repository).salvar(captor.capture());
        assertThat(captor.getValue().descricao()).isEqualTo("Áudio da aula cortando");
        assertThat(captor.getValue().urgencia()).isEqualTo(Urgencia.CRITICA);
    }

    @Test
    void notaAltaEhBaixa() {
        given().contentType(ContentType.JSON)
                .body("{\"descricao\":\"Ótima aula\",\"nota\":9}")
                .when().post("/avaliacao")
                .then().statusCode(201)
                .body("urgencia", equalTo("BAIXA"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "{\"descricao\":\"x\",\"nota\":11}",
        "{\"descricao\":\"x\",\"nota\":-1}",
        "{\"descricao\":\"x\",\"nota\":2.5}",
        "{\"descricao\":\"x\",\"nota\":\"dez\"}",
        "{\"descricao\":\"x\"}",
        "{\"descricao\":\"\",\"nota\":5}",
        "{\"descricao\":\"   \",\"nota\":5}",
        "{\"nota\":5}",
        "{nao é json",
        ""
    })
    void entradaInvalidaRetorna400(String corpo) {
        given().contentType(ContentType.JSON)
                .body(corpo)
                .when().post("/avaliacao")
                .then().statusCode(400);

        verify(repository, never()).salvar(any());
    }

    @Test
    void mensagemDeValidacaoIndicaOCampo() {
        given().contentType(ContentType.JSON)
                .body("{\"descricao\":\"x\",\"nota\":11}")
                .when().post("/avaliacao")
                .then().statusCode(400)
                .body(containsString("nota deve estar entre 0 e 10"));
    }
}
