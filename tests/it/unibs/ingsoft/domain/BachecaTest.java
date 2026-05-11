package it.unibs.ingsoft.domain;

import it.unibs.ingsoft.domain.catalogo.Categoria;
import it.unibs.ingsoft.domain.proposta.Bacheca;
import it.unibs.ingsoft.domain.proposta.Proposta;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BachecaTest {
    @Test
    void addProposta_conPropostaValida_aggiungePropostaAllaBacheca() {
        Bacheca bacheca = new Bacheca();
        Proposta proposta = propostaMinima();

        bacheca.addProposta(proposta);

        assertEquals(List.of(proposta), bacheca.getProposte());
    }

    @Test
    void getProposte_quandoSiModificaListaRestituita_lanciaUnsupportedOperationException() {
        Bacheca bacheca = new Bacheca();

        assertThrows(UnsupportedOperationException.class,
                () -> bacheca.getProposte().add(propostaMinima()));
    }

    @Test
    void fromJson_conListaNull_creaBachecaVuota() {
        Bacheca bacheca = Bacheca.fromJson(null);

        assertTrue(bacheca.getProposte().isEmpty());
    }

    @Test
    void fromJson_conListaValorizzata_copiaLeProposteNellaBacheca() {
        Proposta proposta = propostaMinima();

        Bacheca bacheca = Bacheca.fromJson(List.of(proposta));

        assertEquals(List.of(proposta), bacheca.getProposte());
    }

    private Proposta propostaMinima() {
        return new Proposta(new Categoria("Sport"), List.of(), List.of());
    }
}
