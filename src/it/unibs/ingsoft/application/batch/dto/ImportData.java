package it.unibs.ingsoft.application.batch.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

/**
 * DTO radice che rappresenta l'intero contenuto di un file JSON di importazione batch.
 * Tutte le sezioni sono facoltative — un file vuoto o parziale è valido.
 */
public record ImportData(List<CampoImportDTO> campiComuni, List<CategoriaImportDTO> categorie,
                         List<PropostaImportDTO> proposte) {

    @JsonCreator
    public ImportData(
            @JsonProperty("campiComuni") List<CampoImportDTO> campiComuni,
            @JsonProperty("categorie") List<CategoriaImportDTO> categorie,
            @JsonProperty("proposte") List<PropostaImportDTO> proposte) {
        this.campiComuni = campiComuni != null ? campiComuni : Collections.emptyList();
        this.categorie = categorie != null ? categorie : Collections.emptyList();
        this.proposte = proposte != null ? proposte : Collections.emptyList();
    }
}
