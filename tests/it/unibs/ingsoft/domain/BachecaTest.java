package it.unibs.ingsoft.domain;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

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

    @Test
    void findByChiaveIdentita_conChiaveNull_restituisceOptionalVuoto() {
        Bacheca bacheca = new Bacheca();

        assertTrue(bacheca.findByChiaveIdentita(null).isEmpty());
    }

    @Test
    void findByChiaveIdentita_conChiavePresente_restituisceProposta() {
        Bacheca bacheca = new Bacheca();
        Proposta proposta = propostaConChiave("Torneo", "25/12/2026", "16:30", "Brescia");
        bacheca.addProposta(proposta);

        assertSame(proposta, bacheca.findByChiaveIdentita("torneo|25/12/2026|16:30|brescia").orElseThrow());
    }

    @Test
    void findByChiaveIdentita_conChiaveAssente_restituisceOptionalVuoto() {
        Bacheca bacheca = new Bacheca();
        bacheca.addProposta(propostaConChiave("Torneo", "25/12/2026", "16:30", "Brescia"));

        assertTrue(bacheca.findByChiaveIdentita("altro|25/12/2026|16:30|brescia").isEmpty());
    }

    @Test
    void containsChiaveIdentita_conChiavePresente_restituisceTrue() {
        Bacheca bacheca = new Bacheca();
        bacheca.addProposta(propostaConChiave("Torneo", "25/12/2026", "16:30", "Brescia"));

        assertTrue(bacheca.containsChiaveIdentita("torneo|25/12/2026|16:30|brescia"));
    }

    @Test
    void containsChiaveIdentita_conChiaveAssenteONull_restituisceFalse() {
        Bacheca bacheca = new Bacheca();

        assertAll(
                () -> assertFalse(bacheca.containsChiaveIdentita("assente")),
                () -> assertFalse(bacheca.containsChiaveIdentita(null))
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
