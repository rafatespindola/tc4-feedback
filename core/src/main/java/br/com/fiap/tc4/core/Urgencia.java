package br.com.fiap.tc4.core;

/**
 * Nível de urgência de uma avaliação, derivado da nota (0–10).
 *
 * <ul>
 *   <li>0–3 → {@link #CRITICA} (dispara alerta)</li>
 *   <li>4–6 → {@link #MEDIA}</li>
 *   <li>7–10 → {@link #BAIXA}</li>
 * </ul>
 */
public enum Urgencia {
    CRITICA,
    MEDIA,
    BAIXA;

    public static final int NOTA_MINIMA = 0;
    public static final int NOTA_MAXIMA = 10;

    public static Urgencia classificar(int nota) {
        if (nota < NOTA_MINIMA || nota > NOTA_MAXIMA) {
            throw new IllegalArgumentException(
                    "nota deve estar entre %d e %d: %d".formatted(NOTA_MINIMA, NOTA_MAXIMA, nota));
        }
        if (nota <= 3) {
            return CRITICA;
        }
        if (nota <= 6) {
            return MEDIA;
        }
        return BAIXA;
    }
}
