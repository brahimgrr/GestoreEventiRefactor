package it.unibs.ingsoft.application.catalogo.dto;

import it.unibs.ingsoft.domain.TipoDato;
import it.unibs.ingsoft.domain.error.DomainErrorCode;
import it.unibs.ingsoft.domain.error.DomainException;
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
                () -> assertEquals(DomainErrorCode.CAMPO_NOME_NON_VALIDO,
                        assertThrows(DomainException.class,
                                () -> new CampoBaseExtraRequest(" ", TipoDato.STRINGA)).code()),
                () -> assertEquals(DomainErrorCode.CAMPO_TIPO_DATO_NON_VALIDO,
                        assertThrows(DomainException.class,
                                () -> new CampoBaseExtraRequest("Extra", null)).code()),
                () -> assertEquals(DomainErrorCode.CAMPO_NOME_NON_VALIDO,
                        assertThrows(DomainException.class,
                                () -> new CampoBaseExtraRequest(null, null)).code())
        );
    }
}
