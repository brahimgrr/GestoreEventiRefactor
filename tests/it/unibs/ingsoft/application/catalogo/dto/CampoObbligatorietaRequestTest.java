package it.unibs.ingsoft.application.catalogo.dto;

import it.unibs.ingsoft.domain.catalogo.CatalogFailure;
import it.unibs.ingsoft.domain.shared.error.DomainException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CampoObbligatorietaRequestTest {
    @Test
    void costruttore_conValoriValidi_normalizzaNome() {
        CampoObbligatorietaRequest request = new CampoObbligatorietaRequest("  Note  ", true);

        assertAll(
                () -> assertEquals("Note", request.nomeCampo()),
                () -> assertTrue(request.obbligatorio())
        );
    }

    @Test
    void costruttore_conNomeInvalido_lanciaEccezione() {
        assertAll(
                () -> assertInstanceOf(CatalogFailure.FieldNameInvalid.class,
                        assertThrows(DomainException.class,
                                () -> new CampoObbligatorietaRequest(" ", false)).failure()),
                () -> assertInstanceOf(CatalogFailure.FieldNameInvalid.class,
                        assertThrows(DomainException.class,
                                () -> new CampoObbligatorietaRequest(null, false)).failure())
        );
    }
}
