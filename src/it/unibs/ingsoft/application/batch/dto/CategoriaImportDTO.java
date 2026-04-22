package it.unibs.ingsoft.application.batch.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

/**
 * DTO per l'importazione di una categoria (con i suoi campi specifici) da un file batch.
 */
public record CategoriaImportDTO(String nome, List<CampoSpecificoImportDTO> campiSpecifici) {

    @JsonCreator
    public CategoriaImportDTO(
            @JsonProperty("nome") String nome,
            @JsonProperty("campiSpecifici") List<CampoSpecificoImportDTO> campiSpecifici) {
        this.nome = nome;
        this.campiSpecifici = campiSpecifici != null ? campiSpecifici : Collections.emptyList();
    }
}
