package it.unibs.ingsoft.application.batch.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO per l'importazione di un campo comune da un file batch.
 */
public record CampoImportDTO(String nome, String tipoDato, boolean obbligatorio) {

    @JsonCreator
    public CampoImportDTO(
            @JsonProperty("nome") String nome,
            @JsonProperty("tipoDato") String tipoDato,
            @JsonProperty("obbligatorio") boolean obbligatorio) {
        this.nome = nome;
        this.tipoDato = tipoDato;
        this.obbligatorio = obbligatorio;
    }
}
