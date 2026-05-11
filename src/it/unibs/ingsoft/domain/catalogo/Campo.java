package it.unibs.ingsoft.domain.catalogo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.unibs.ingsoft.domain.shared.error.DomainErrorCode;
import it.unibs.ingsoft.domain.shared.error.DomainException;

/**
 * Value object immutabile che rappresenta la definizione di un campo (base, comune o specifico).
 */
public final class Campo {
    private final String nome;
    private final TipoCampo tipo;
    private final TipoDato tipoDato;
    private final boolean obbligatorio;

    /**
     * @pre nome != null &amp;&amp; !nome.isBlank()
     * @pre tipo != null
     * @pre tipoDato != null
     * @post getNome().equals(nome.trim ())
     * @post getTipo() == tipo
     * @post getTipoDato() == tipoDato
     * @post isObbligatorio() == obbligatorio
     */
    @JsonCreator
    public Campo(@JsonProperty("nome") String nome,
                 @JsonProperty("tipo") TipoCampo tipo,
                 @JsonProperty("tipoDato") TipoDato tipoDato,
                 @JsonProperty("obbligatorio") boolean obbligatorio) {
        if (nome == null || nome.isBlank())
            throw new DomainException(DomainErrorCode.CAMPO_NOME_NON_VALIDO);
        if (tipo == null)
            throw new DomainException(DomainErrorCode.CAMPO_TIPO_NON_VALIDO);
        if (tipoDato == null)
            throw new DomainException(DomainErrorCode.CAMPO_TIPO_DATO_NON_VALIDO);

        this.nome = nome.trim();
        this.tipo = tipo;
        this.tipoDato = tipoDato;
        this.obbligatorio = obbligatorio;
    }

    public Campo(Campo oldCampo) {
        this.nome = oldCampo.nome;
        this.tipo = oldCampo.tipo;
        this.tipoDato = oldCampo.tipoDato;
        this.obbligatorio = oldCampo.obbligatorio;
    }

    public String getNome() {
        return nome;
    }

    public TipoCampo getTipo() {
        return tipo;
    }

    public TipoDato getTipoDato() {
        return tipoDato;
    }

    public boolean isObbligatorio() {
        return obbligatorio;
    }

    /**
     * Restituisce un nuovo {@code Campo} identico a questo ma con il valore {@code obbligatorio} indicato.
     */
    public Campo withObbligatorio(boolean obbligatorio) {
        return new Campo(this.nome, this.tipo, this.tipoDato, obbligatorio);
    }

    /**
     * Uguaglianza case-insensitive sul nome.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Campo)) return false;
        return nome.equalsIgnoreCase(((Campo) o).nome);
    }

    @Override
    public int hashCode() {
        return nome.toLowerCase().hashCode();
    }

    @Override
    public String toString() {
        //return nome + " [" + tipoDato + "]" + (obbligatorio ? "  (obbligatorio)" : "");
        return "Campo{nome='" + nome + "', tipoDato=" + tipoDato + ", obbligatorio=" + obbligatorio + "}";
    }
}
