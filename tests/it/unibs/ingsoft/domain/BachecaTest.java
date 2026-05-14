package it.unibs.ingsoft.domain;

import it.unibs.ingsoft.domain.catalogo.Categoria;
import it.unibs.ingsoft.domain.shared.AppConstants;
import it.unibs.ingsoft.persistence.dto.BachecaDTO;
import it.unibs.ingsoft.domain.proposta.Proposta;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

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

    @Test
    void findByChiaveDuplicato_conChiaveNull_restituisceOptionalVuoto() {
        BachecaDTO bacheca = new BachecaDTO();

        assertTrue(bacheca.findByChiaveDuplicato(null).isEmpty());
    }

    @Test
    void findByChiaveDuplicato_conChiavePresente_restituisceProposta() {
        BachecaDTO bacheca = new BachecaDTO();
        Proposta proposta = propostaConChiave("Torneo", "25/12/2026", "16:30", "Brescia");
        bacheca.addProposta(proposta);

        assertSame(proposta, bacheca.findByChiaveDuplicato("torneo|25/12/2026|16:30|brescia").orElseThrow());
    }

    @Test
    void findByChiaveDuplicato_conChiaveAssente_restituisceOptionalVuoto() {
        BachecaDTO bacheca = new BachecaDTO();
        bacheca.addProposta(propostaConChiave("Torneo", "25/12/2026", "16:30", "Brescia"));

        assertTrue(bacheca.findByChiaveDuplicato("altro|25/12/2026|16:30|brescia").isEmpty());
    }

    @Test
    void containsChiaveDuplicato_conChiavePresente_restituisceTrue() {
        BachecaDTO bacheca = new BachecaDTO();
        bacheca.addProposta(propostaConChiave("Torneo", "25/12/2026", "16:30", "Brescia"));

        assertTrue(bacheca.containsChiaveDuplicato("torneo|25/12/2026|16:30|brescia"));
    }

    @Test
    void containsChiaveDuplicato_conChiaveAssenteONull_restituisceFalse() {
        BachecaDTO bacheca = new BachecaDTO();

        assertAll(
                () -> assertFalse(bacheca.containsChiaveDuplicato("assente")),
                () -> assertFalse(bacheca.containsChiaveDuplicato(null))
        );
    }

    private Proposta propostaMinima() {
        return new Proposta(new Categoria("Sport"), List.of(), List.of());
    }

    private Proposta propostaConChiave(String titolo, String data, String ora, String luogo) {
        Proposta proposta = new Proposta(new Categoria("Sport"), List.of(), List.of());
        proposta.aggiornaValoriCampi(Map.of(
                AppConstants.CAMPO_TITOLO, titolo,
                AppConstants.CAMPO_DATA, data,
                AppConstants.CAMPO_ORA, ora,
                AppConstants.CAMPO_LUOGO, luogo
        ));
        return proposta;
    }
}
