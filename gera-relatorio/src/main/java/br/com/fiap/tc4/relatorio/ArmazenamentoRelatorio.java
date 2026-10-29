package br.com.fiap.tc4.relatorio;

public interface ArmazenamentoRelatorio {

    /** Salva o relatório e devolve o local onde ficou ({@code bucket/chave}). */
    String salvar(Relatorio relatorio);
}
