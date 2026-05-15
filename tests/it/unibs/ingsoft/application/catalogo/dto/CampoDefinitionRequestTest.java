package it.unibs.ingsoft.application.catalogo.dto;

import it.unibs.ingsoft.domain.model.catalogo.TipoDato;
import it.unibs.ingsoft.domain.model.catalogo.CatalogFailure;
import it.unibs.ingsoft.domain.error.DomainException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CampoDefinitionRequestTest {
    @Test
    void costruttore_conValoriValidi_normalizzaNome() {
        CampoDefinitionRequest request = new CampoDefinitionRequest("  Note  ", TipoDato.STRINGA, true);

        assertAll(
                () -> assertEquals("Note", request.nome()),
                () -> assertTrue(request.obbligatorio())
        );
    }

    @Test
    void costruttore_conNomeInvalidoOTipoNull_lanciaEccezione() {
        assertAll(
                () -> assertInstanceOf(CatalogFailure.FieldNameInvalid.class,
                        assertThrows(DomainException.class,
                                () -> new CampoDefinitionRequest(null, TipoDato.STRINGA, false)).failure()),
                () -> assertInstanceOf(CatalogFailure.FieldNameInvalid.class,
                        assertThrows(DomainException.class,
                                () -> new CampoDefinitionRequest("   ", TipoDato.STRINGA, false)).failure()),
                () -> assertInstanceOf(CatalogFailure.FieldDataTypeInvalid.class,
                        assertThrows(DomainException.class,
                                () -> new CampoDefinitionRequest("Note", null, false)).failure())
        );
    }
}
