package br.com.fiap.tc4.relatorio;

import java.time.LocalDate;
import java.util.List;

import br.com.fiap.tc4.core.Avaliacao;

public interface AvaliacaoConsulta {

    List<Avaliacao> doDia(LocalDate dia);
}
