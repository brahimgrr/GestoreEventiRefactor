package it.unibs.ingsoft.domain;

import it.unibs.ingsoft.domain.catalogo.Campo;
import it.unibs.ingsoft.domain.catalogo.Categoria;
import it.unibs.ingsoft.domain.catalogo.TipoCampo;
import it.unibs.ingsoft.domain.catalogo.TipoDato;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CategoriaCopyTest {
    @Test
    void copiedCategoriaKeepsMutableSpecificFields() {
        Categoria originale = new Categoria("Cinema");
        originale.addCampoSpecifico(new Campo("Regista", TipoCampo.SPECIFICO, TipoDato.STRINGA, false));

        Categoria copia = new Categoria(originale);
        copia.addCampoSpecifico(new Campo("Sala", TipoCampo.SPECIFICO, TipoDato.STRINGA, false));

        assertTrue(copia.removeCampoSpecifico("Regista"));
        assertTrue(copia.getCampiSpecifici().stream()
                .anyMatch(campo -> campo.getNome().equals("Sala")));
    }
}
