package it.unibs.ingsoft.persistence.file.document;

import it.unibs.ingsoft.domain.model.catalogo.Campo;
import it.unibs.ingsoft.domain.model.catalogo.TipoCampo;
import it.unibs.ingsoft.domain.model.catalogo.TipoDato;

public record CampoDocument(String nome, TipoCampo tipo, TipoDato tipoDato, boolean obbligatorio) {
    public static CampoDocument fromDomain(Campo campo) {
        return new CampoDocument(
                campo.getNome(),
                campo.getTipo(),
                campo.getTipoDato(),
                campo.isObbligatorio());
    }

    public Campo toDomain() {
        return new Campo(nome, tipo, tipoDato, obbligatorio);
    }
}
