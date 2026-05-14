package it.unibs.ingsoft.application.catalogo.dto;

import it.unibs.ingsoft.domain.catalogo.TipoDato;
import it.unibs.ingsoft.domain.catalogo.CatalogFailure;
import it.unibs.ingsoft.domain.shared.error.DomainException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CampoBaseExtraRequestTest {
    @Test
    void costruttore_conValoriValidi_normalizzaNome() {
        CampoBaseExtraRequest request = new CampoBaseExtraRequest("  Extra  ", TipoDato.STRINGA);

        assertEquals("Extra", request.nome());
    }

    @Test
    void costruttore_conNomeInvalidoOTipoNull_lanciaEccezione() {
        assertAll(
                () -> assertInstanceOf(CatalogFailure.FieldNameInvalid.class,
                        assertThrows(DomainException.class,
                                () -> new CampoBaseExtraRequest(" ", TipoDato.STRINGA)).failure()),
                () -> assertInstanceOf(CatalogFailure.FieldDataTypeInvalid.class,
                        assertThrows(DomainException.class,
                                () -> new CampoBaseExtraRequest("Extra", null)).failure()),
                () -> assertInstanceOf(CatalogFailure.FieldNameInvalid.class,
                        assertThrows(DomainException.class,
                                () -> new CampoBaseExtraRequest(null, null)).failure())
        );
    }
}
