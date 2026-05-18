package it.unibs.ingsoft.application.catalogo.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/*
Enum non ha senso di essere testato
 */
class CatalogoOperationResult_Test {
    @Test
    void valuesEValueOf_restituisconoCostantiAttese() {
        assertAll(
                () -> assertArrayEquals(new CatalogoOperationResult[]{
                        CatalogoOperationResult.SUCCESSO,
                        CatalogoOperationResult.NON_TROVATO,
                        CatalogoOperationResult.NESSUNA_MODIFICA
                }, CatalogoOperationResult.values()),
                () -> assertEquals(CatalogoOperationResult.SUCCESSO,
                        CatalogoOperationResult.valueOf("SUCCESSO"))
        );
    }
}
