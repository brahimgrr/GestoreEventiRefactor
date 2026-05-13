package it.unibs.ingsoft.domain;

import it.unibs.ingsoft.domain.catalogo.Categoria;
import it.unibs.ingsoft.persistence.dto.BachecaDTO;
import it.unibs.ingsoft.domain.proposta.Proposta;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BachecaTest {
    @Test
    void addProposta_conPropostaValida_aggiungePropostaAllaBacheca() {
        BachecaDTO bacheca = new BachecaDTO();
        Proposta proposta = propostaMinima();

        bacheca.addProposta(proposta);

        assertEquals(List.of(proposta), bacheca.getProposte());
    }

    @Test
    void getProposte_quandoSiModificaListaRestituita_lanciaUnsupportedOperationException() {
        BachecaDTO bacheca = new BachecaDTO();

        assertThrows(UnsupportedOperationException.class,
                () -> bacheca.getProposte().add(propostaMinima()));
    }

    @Test
    void fromJson_conListaNull_creaBachecaVuota() {
        BachecaDTO bacheca = BachecaDTO.fromJson(null);

        assertTrue(bacheca.getProposte().isEmpty());
    }

    @Test
    void fromJson_conListaValorizzata_copiaLeProposteNellaBacheca() {
        Proposta proposta = propostaMinima();

        BachecaDTO bacheca = BachecaDTO.fromJson(List.of(proposta));

        assertEquals(List.of(proposta), bacheca.getProposte());
    }

    private Proposta propostaMinima() {
        return new Proposta(new Categoria("Sport"), List.of(), List.of());
    }
}
