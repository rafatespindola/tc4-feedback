package br.com.fiap.tc4.relatorio;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.jboss.logging.Logger;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import br.com.fiap.tc4.core.Avaliacao;

/**
 * Executada toda segunda-feira às 08:00 (Brasília) pelo EventBridge Scheduler: agrega a
 * semana anterior (segunda a domingo), salva o JSON no S3 e envia o resumo por e-mail.
 */
public class GeraRelatorioHandler implements RequestHandler<Execucao, Resultado> {

    private static final Logger LOG = Logger.getLogger(GeraRelatorioHandler.class);

    private final AvaliacaoConsulta consulta;
    private final ArmazenamentoRelatorio armazenamento;
    private final PublicadorRelatorio publicador;
    private final Clock clock;

    public GeraRelatorioHandler(AvaliacaoConsulta consulta, ArmazenamentoRelatorio armazenamento,
            PublicadorRelatorio publicador, Clock clock) {
        this.consulta = consulta;
        this.armazenamento = armazenamento;
        this.publicador = publicador;
        this.clock = clock;
    }

    @Override
    public Resultado handleRequest(Execucao execucao, Context context) {
        Semana semana = semana(execucao);
        List<Avaliacao> avaliacoes = semana.dias().stream()
                .flatMap(dia -> consulta.doDia(dia).stream())
                .toList();

        Relatorio relatorio = Relatorio.gerar(semana, avaliacoes, clock.instant());
        String arquivo = armazenamento.salvar(relatorio);
        publicador.publicar(EmailRelatorio.de(relatorio, arquivo));

        LOG.infof("relatorio gerado semana=%s..%s total=%d media=%s arquivo=%s",
                semana.inicio(), semana.fim(), relatorio.total(), relatorio.media(), arquivo);
        return new Resultado(semana.inicio(), semana.fim(), relatorio.total(), relatorio.media(), arquivo);
    }

    private Semana semana(Execucao execucao) {
        if (execucao != null && execucao.inicio() != null && !execucao.inicio().isBlank()) {
            return new Semana(LocalDate.parse(execucao.inicio()));
        }
        return Semana.anteriorA(LocalDate.now(clock.withZone(Avaliacao.FUSO)));
    }
}
