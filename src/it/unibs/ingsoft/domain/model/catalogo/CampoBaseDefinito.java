package it.unibs.ingsoft.domain.model.catalogo;

/**
 * Enumerazione degli otto campi base fissi mandati dalla specifica.
 * Ogni costante definisce il nome canonico del campo e il suo tipo di dato.
 */
public enum CampoBaseDefinito {
    TITOLO("Titolo", TipoDato.STRINGA),
    NUMERO_PARTECIPANTI("Numero di partecipanti", TipoDato.INTERO_POSITIVO),
    TERMINE_ISCRIZIONE("Termine ultimo di iscrizione", TipoDato.DATA),
    LUOGO("Luogo", TipoDato.STRINGA),
    DATA("Data", TipoDato.DATA),
    ORA("Ora", TipoDato.ORA),
    QUOTA_INDIVIDUALE("Quota individuale", TipoDato.DECIMALE),
    DATA_CONCLUSIVA("Data conclusiva", TipoDato.DATA);

    private final String nomeCampo;
    private final TipoDato tipoDato;

    CampoBaseDefinito(String nomeCampo, TipoDato tipoDato) {
        this.nomeCampo = nomeCampo;
        this.tipoDato = tipoDato;
    }

    /**
     * Ricerca case-insensitive per nome campo; restituisce {@code null} se non trovato.
     */
    public static CampoBaseDefinito fromNome(String nome) {
        if (nome == null) return null;
        for (CampoBaseDefinito c : values())
            if (c.nomeCampo.equalsIgnoreCase(nome.trim()))
                return c;
        return null;
    }

    /**
     * {@code true} se {@code nome} corrisponde a un campo base fisso (case-insensitive).
     */
    public static boolean isNomeFisso(String nome) {
        return fromNome(nome) != null;
    }

    public String getNomeCampo() {
        return nomeCampo;
    }

    public TipoDato getTipoDato() {
        return tipoDato;
    }

    /**
     * Converte la costante in un'istanza di {@link Campo}.
     * I campi base sono sempre obbligatori ({@code obbligatorio = true}).
     */
    public Campo toCampo() {
        return new Campo(nomeCampo, TipoCampo.BASE, tipoDato, true);
    }
}
