package it.unibs.ingsoft.application.batch.dto;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BatchDtoRecordTest {
    @Test
    void campoImportDTO_esponeValori() {
        CampoImportDTO dto = new CampoImportDTO("Note", "STRINGA", true);

        assertAll(
                () -> assertEquals("Note", dto.nome()),
                () -> assertEquals("STRINGA", dto.tipoDato()),
                () -> assertTrue(dto.obbligatorio())
        );
    }

    @Test
    void campoSpecificoImportDTO_esponeValori() {
        CampoSpecificoImportDTO dto = new CampoSpecificoImportDTO("Arbitro", "BOOLEANO", false);

        assertAll(
                () -> assertEquals("Arbitro", dto.nome()),
                () -> assertEquals("BOOLEANO", dto.tipoDato()),
                () -> assertFalse(dto.obbligatorio())
        );
    }

    @Test
    void categoriaImportDTO_conCampiNull_usaListaVuota() {
        CategoriaImportDTO dto = new CategoriaImportDTO("Sport", null);

        assertTrue(dto.campiSpecifici().isEmpty());
    }

    @Test
    void categoriaImportDTO_conCampiValorizzati_mantieneLista() {
        CampoSpecificoImportDTO campo = new CampoSpecificoImportDTO("Arbitro", "BOOLEANO", false);

        CategoriaImportDTO dto = new CategoriaImportDTO("Sport", List.of(campo));

        assertEquals(List.of(campo), dto.campiSpecifici());
    }

    @Test
    void propostaImportDTO_conValoriNull_usaMappaVuota() {
        PropostaImportDTO dto = new PropostaImportDTO("Sport", null);

        assertTrue(dto.valoriCampi().isEmpty());
    }

    @Test
    void propostaImportDTO_conValori_mantieneMappa() {
        Map<String, String> valori = Map.of("Titolo", "Torneo");

        PropostaImportDTO dto = new PropostaImportDTO("Sport", valori);

        assertEquals(valori, dto.valoriCampi());
    }
}
