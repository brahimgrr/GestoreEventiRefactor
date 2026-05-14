package it.unibs.ingsoft.application.batch.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ImportDataTest {
    @Test
    void costruttore_conListeNull_usaListeVuote() {
        ImportData data = new ImportData(null, null, null);

        assertAll(
                () -> assertTrue(data.campiComuni().isEmpty()),
                () -> assertTrue(data.categorie().isEmpty()),
                () -> assertTrue(data.proposte().isEmpty())
        );
    }

    @Test
    void costruttore_conListeValorizzate_mantieneValori() {
        CampoImportDTO campo = new CampoImportDTO("Note", "STRINGA", false);
        CategoriaImportDTO categoria = new CategoriaImportDTO("Sport", List.of());
        PropostaImportDTO proposta = new PropostaImportDTO("Sport", java.util.Map.of());

        ImportData data = new ImportData(List.of(campo), List.of(categoria), List.of(proposta));

        assertAll(
                () -> assertEquals(List.of(campo), data.campiComuni()),
                () -> assertEquals(List.of(categoria), data.categorie()),
                () -> assertEquals(List.of(proposta), data.proposte())
        );
    }
}
