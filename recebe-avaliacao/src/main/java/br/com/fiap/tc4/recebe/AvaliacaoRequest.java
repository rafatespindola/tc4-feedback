package br.com.fiap.tc4.recebe;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import br.com.fiap.tc4.core.Urgencia;

public record AvaliacaoRequest(
        @NotBlank(message = "descricao é obrigatória")
        @Size(max = 2000, message = "descricao deve ter no máximo 2000 caracteres")
        String descricao,

        @NotNull(message = "nota é obrigatória")
        @Min(value = Urgencia.NOTA_MINIMA, message = "nota deve estar entre 0 e 10")
        @Max(value = Urgencia.NOTA_MAXIMA, message = "nota deve estar entre 0 e 10")
        Integer nota) {
}
