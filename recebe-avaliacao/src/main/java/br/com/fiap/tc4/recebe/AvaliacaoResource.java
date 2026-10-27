package br.com.fiap.tc4.recebe;

import java.time.Clock;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.jboss.logging.Logger;

import br.com.fiap.tc4.core.Avaliacao;

@Path("/avaliacao")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AvaliacaoResource {

    private static final Logger LOG = Logger.getLogger(AvaliacaoResource.class);

    private final AvaliacaoRepository repository;
    private final Clock clock;

    public AvaliacaoResource(AvaliacaoRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @POST
    public Response receber(@Valid @NotNull(message = "corpo da requisição é obrigatório") AvaliacaoRequest request) {
        Avaliacao avaliacao = Avaliacao.nova(request.descricao(), request.nota(), clock);
        repository.salvar(avaliacao);
        LOG.infof("avaliacao recebida id=%s nota=%d urgencia=%s", avaliacao.id(), avaliacao.nota(), avaliacao.urgencia());
        return Response.status(Response.Status.CREATED).entity(AvaliacaoResponse.de(avaliacao)).build();
    }
}
