package it.unibs.ingsoft.application.batch.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.Map;

/**
 * DTO per l'importazione di una proposta da un file batch.
 * Il campo {@code categoria} è il nome di una categoria esistente nel catalogo.
 */
public record PropostaImportDTO(String categoria, Map<String, String> valoriCampi) {

    @JsonCreator
    public PropostaImportDTO(
            @JsonProperty("categoria") String categoria,
            @JsonProperty("valoriCampi") Map<String, String> valoriCampi) {
        this.categoria = categoria;
        this.valoriCampi = valoriCampi != null ? valoriCampi : Collections.emptyMap();
    }
}
