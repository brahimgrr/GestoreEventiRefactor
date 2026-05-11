package it.unibs.ingsoft.domain;

import it.unibs.ingsoft.domain.catalogo.Campo;
import it.unibs.ingsoft.domain.catalogo.Categoria;
import it.unibs.ingsoft.domain.catalogo.TipoCampo;
import it.unibs.ingsoft.domain.catalogo.TipoDato;
import it.unibs.ingsoft.domain.proposta.Proposta;
import it.unibs.ingsoft.domain.proposta.StatoProposta;
import it.unibs.ingsoft.domain.shared.AppConstants;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class Proposta_Test {
    @Test
    void costruttore_conCategoriaValida_creaPropostaInStatoBozza() {
        Proposta proposta = propostaConCampiBaseMinimi();

        assertEquals(StatoProposta.BOZZA, proposta.getStato());
    }

    @Test
    void costruttore_conCategoriaValida_registraStatoInizialeNellaCronologia() {
        Proposta proposta = propostaConCampiBaseMinimi();

        assertAll(
                () -> assertEquals(1, proposta.getStateHistory().size()),
                () -> assertEquals(StatoProposta.BOZZA, proposta.getStateHistory().get(0).stato())
        );
    }

    @Test
    void costruttore_conCategoriaNull_lanciaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Proposta(null, List.of(), List.of()));
    }

    @Test
    void getCampi_conCampiBaseComuniESpecifici_restituisceCampiInOrdineBaseComuniSpecifici() {
        Categoria categoria = new Categoria("Sport");
        categoria.addCampoSpecifico(campo("Arbitro", TipoCampo.SPECIFICO, TipoDato.BOOLEANO, false));
        Proposta proposta = new Proposta(categoria,
                List.of(campo(AppConstants.CAMPO_TITOLO, TipoCampo.BASE, TipoDato.STRINGA, true)),
                List.of(campo("Eta", TipoCampo.COMUNE, TipoDato.INTERO, false)));

        List<String> nomi = proposta.getCampi().stream().map(Campo::getNome).toList();

        assertEquals(List.of(AppConstants.CAMPO_TITOLO, "Eta", "Arbitro"), nomi);
    }

    @Test
    void setStato_conTransizioneValidaDaBozzaAValida_aggiornaStato() {
        Proposta proposta = propostaConCampiBaseMinimi();

        proposta.setStato(StatoProposta.VALIDA);

        assertEquals(StatoProposta.VALIDA, proposta.getStato());
    }

    @Test
    void setStato_conTransizioneValidaDaBozzaAValida_aggiungeVoceAllaCronologia() {
        Proposta proposta = propostaConCampiBaseMinimi();

        proposta.setStato(StatoProposta.VALIDA);

        assertEquals(StatoProposta.VALIDA, proposta.getStateHistory().get(1).stato());
    }

    @Test
    void setStato_conStatoNull_lanciaIllegalArgumentException() {
        Proposta proposta = propostaConCampiBaseMinimi();

        assertThrows(IllegalArgumentException.class, () -> proposta.setStato(null));
    }

    @Test
    void setStato_conTransizioneNonConsentitaDaBozzaAAperta_lanciaIllegalStateException() {
        Proposta proposta = propostaConCampiBaseMinimi();

        assertThrows(IllegalStateException.class, () -> proposta.setStato(StatoProposta.APERTA));
    }

    @Test
    void revertToBozzaSilent_quandoStatoValida_riportaStatoABozza() {
        Proposta proposta = propostaConCampiBaseMinimi();
        proposta.setStato(StatoProposta.VALIDA);

        proposta.revertToBozzaSilent();

        assertEquals(StatoProposta.BOZZA, proposta.getStato());
    }

    @Test
    void revertToBozzaSilent_quandoStatoValida_nonAggiungeVoceAllaCronologia() {
        Proposta proposta = propostaConCampiBaseMinimi();
        proposta.setStato(StatoProposta.VALIDA);

        proposta.revertToBozzaSilent();

        assertEquals(2, proposta.getStateHistory().size());
    }

    @Test
    void putAllValoriCampi_conPropostaInBozza_ordinaValoriSecondoOrdineDeiCampi() {
        Proposta proposta = propostaConCampiBaseComuneESpecifico();
        Map<String, String> valori = new LinkedHashMap<>();
        valori.put("Extra legacy", "x");
        valori.put("Arbitro", "si");
        valori.put("Eta", "18");
        valori.put(AppConstants.CAMPO_TITOLO, "Torneo");

        proposta.putAllValoriCampi(valori);

        assertEquals(List.of(AppConstants.CAMPO_TITOLO, "Eta", "Arbitro", "Extra legacy"),
                proposta.getValoriCampi().keySet().stream().toList());
    }

    @Test
    void putAllValoriCampi_conPropostaAperta_lanciaIllegalStateException() {
        Proposta proposta = propostaApertaConCapienza("2");

        assertThrows(IllegalStateException.class,
                () -> proposta.putAllValoriCampi(Map.of(AppConstants.CAMPO_TITOLO, "Nuovo titolo")));
    }

    @Test
    void getValoriCampi_quandoSiModificaMappaRestituita_lanciaUnsupportedOperationException() {
        Proposta proposta = propostaConCampiBaseMinimi();

        assertThrows(UnsupportedOperationException.class,
                () -> proposta.getValoriCampi().put("Campo", "Valore"));
    }

    @Test
    void addAderente_conPropostaApertaECapienzaDisponibile_aggiungeUsername() {
        Proposta proposta = propostaApertaConCapienza("2");

        proposta.addAderente("mario");

        assertEquals(List.of("mario"), proposta.getListaAderenti());
    }

    @Test
    void addAderente_conPropostaNonAperta_lanciaIllegalStateException() {
        Proposta proposta = propostaConCampiBaseMinimi();

        assertThrows(IllegalStateException.class, () -> proposta.addAderente("mario"));
    }

    @Test
    void addAderente_conUsernameGiaIscritto_lanciaIllegalStateException() {
        Proposta proposta = propostaApertaConCapienza("2");
        proposta.addAderente("mario");

        assertThrows(IllegalStateException.class, () -> proposta.addAderente("mario"));
    }

    @Test
    void addAderente_conCapienzaMassimaRaggiunta_lanciaIllegalStateException() {
        Proposta proposta = propostaApertaConCapienza("1");
        proposta.addAderente("mario");

        assertThrows(IllegalStateException.class, () -> proposta.addAderente("luigi"));
    }

    @Test
    void removeAderente_conPropostaApertaETermineNonScaduto_rimuoveUsername() {
        Proposta proposta = propostaApertaConCapienza("2");
        proposta.setTermineIscrizione(LocalDateTime.now().toLocalDate().plusDays(3));
        proposta.addAderente("mario");

        proposta.removeAderente("mario", LocalDateTime.now().toLocalDate());

        assertTrue(proposta.getListaAderenti().isEmpty());
    }

    @Test
    void removeAderente_conPropostaNonAperta_lanciaIllegalStateException() {
        Proposta proposta = propostaConCampiBaseMinimi();

        assertThrows(IllegalStateException.class,
                () -> proposta.removeAderente("mario", LocalDateTime.now().toLocalDate()));
    }

    @Test
    void removeAderente_conTermineIscrizioneScaduto_lanciaIllegalStateException() {
        Proposta proposta = propostaApertaConCapienza("2");
        proposta.setTermineIscrizione(LocalDateTime.now().toLocalDate());
        proposta.addAderente("mario");

        assertThrows(IllegalStateException.class,
                () -> proposta.removeAderente("mario", LocalDateTime.now().toLocalDate().plusDays(1)));
    }

    @Test
    void getNumeroPartecipanti_conInteroPositivo_restituisceNumeroPartecipanti() {
        Proposta proposta = propostaConCampiBaseMinimi();
        proposta.putAllValoriCampi(Map.of(AppConstants.CAMPO_NUM_PARTECIPANTI, " 12 "));

        assertEquals(12, proposta.getNumeroPartecipanti());
    }

    @Test
    void getNumeroPartecipanti_conCampoAssente_lanciaIllegalStateException() {
        Proposta proposta = propostaConCampiBaseMinimi();

        assertThrows(IllegalStateException.class, proposta::getNumeroPartecipanti);
    }

    @Test
    void getNumeroPartecipanti_conValoreNonNumerico_lanciaIllegalStateException() {
        Proposta proposta = propostaConCampiBaseMinimi();
        proposta.putAllValoriCampi(Map.of(AppConstants.CAMPO_NUM_PARTECIPANTI, "molti"));

        assertThrows(IllegalStateException.class, proposta::getNumeroPartecipanti);
    }

    @Test
    void getNumeroPartecipanti_conZero_lanciaIllegalStateException() {
        Proposta proposta = propostaConCampiBaseMinimi();
        proposta.putAllValoriCampi(Map.of(AppConstants.CAMPO_NUM_PARTECIPANTI, "0"));

        assertThrows(IllegalStateException.class, proposta::getNumeroPartecipanti);
    }

    /*
    DA WARNING
     */
    /*
    @Test
    void getDataConclusiva_conCampoValido_restituisceDataConclusivaDelCampo() {
        Proposta proposta = propostaConCampiBaseMinimi();
        proposta.setDataEvento(LocalDateTime.now().toLocalDate());
        proposta.putAllValoriCampi(Map.of(AppConstants.CAMPO_DATA_CONCLUSIVA, LocalDateTime.now().toLocalDate().toString()));

        assertEquals(LocalDateTime.now().toLocalDate(), proposta.getDataConclusiva());
    }
     */

    @Test
    void getDataConclusiva_conCampoAssente_restituisceDataEvento() {
        Proposta proposta = propostaConCampiBaseMinimi();
        proposta.setDataEvento(LocalDateTime.now().toLocalDate());

        assertEquals(LocalDateTime.now().toLocalDate(), proposta.getDataConclusiva());
    }

    /*
    DA WARNING
     */
    /*
    @Test
    void getDataConclusiva_conCampoMalformato_restituisceDataEvento() {
        Proposta proposta = propostaConCampiBaseMinimi();
        proposta.setDataEvento(LocalDateTime.now().toLocalDate());
        proposta.putAllValoriCampi(Map.of(AppConstants.CAMPO_DATA_CONCLUSIVA, LocalDateTime.now().toLocalDate().plusDays(3).toString()));

        assertEquals(LocalDateTime.now().toLocalDate(), proposta.getDataConclusiva());
    }
    */

    @Test
    void isCapienzaRaggiunta_conAderentiUgualiAllaCapienza_restituisceTrue() {
        Proposta proposta = propostaApertaConCapienza("1");
        proposta.addAderente("mario");

        assertTrue(proposta.isCapienzaRaggiunta());
    }

    @Test
    void isTermineIscrizioneScaduto_conOggiDopoTermine_restituisceTrue() {
        Proposta proposta = propostaConCampiBaseMinimi();
        proposta.setTermineIscrizione(LocalDateTime.now().toLocalDate());

        assertTrue(proposta.isTermineIscrizioneScaduto(LocalDateTime.now().toLocalDate().plusDays(5)));
    }

    @Test
    void isTermineIscrizioneScaduto_conTermineNull_restituisceFalse() {
        Proposta proposta = propostaConCampiBaseMinimi();

        assertFalse(proposta.isTermineIscrizioneScaduto(LocalDateTime.now().toLocalDate()));
    }

    @Test
    void chiaveIdentita_conValoriConSpaziEMaiuscole_restituisceChiaveNormalizzata() {
        Map<String, String> valori = Map.of(
                AppConstants.CAMPO_TITOLO, "  Torneo  ",
                AppConstants.CAMPO_DATA, "25/12/2026",
                AppConstants.CAMPO_ORA, "16:30",
                AppConstants.CAMPO_LUOGO, "  Brescia  "
        );

        assertEquals("torneo|25/12/2026|16:30|brescia", Proposta.chiaveIdentita(valori));
    }

    @Test
    void getChiaveIdentita_conValoriImpostati_restituisceChiaveNormalizzataDellaProposta() {
        Proposta proposta = propostaConCampiBaseMinimi();
        proposta.putAllValoriCampi(Map.of(
                AppConstants.CAMPO_TITOLO, "Torneo",
                AppConstants.CAMPO_DATA, "25/12/2026",
                AppConstants.CAMPO_ORA, "16:30",
                AppConstants.CAMPO_LUOGO, "Brescia"
        ));

        assertEquals("torneo|25/12/2026|16:30|brescia", proposta.getChiaveIdentita());
    }

    private Proposta propostaApertaConCapienza(String numeroPartecipanti) {
        Proposta proposta = propostaConCampiBaseMinimi();
        proposta.putAllValoriCampi(Map.of(AppConstants.CAMPO_NUM_PARTECIPANTI, numeroPartecipanti));
        proposta.setStato(StatoProposta.VALIDA);
        proposta.setStato(StatoProposta.APERTA);
        return proposta;
    }

    private Proposta propostaConCampiBaseMinimi() {
        return new Proposta(new Categoria("Sport"),
                List.of(campo(AppConstants.CAMPO_NUM_PARTECIPANTI, TipoCampo.BASE, TipoDato.INTERO, true)),
                List.of());
    }

    private Proposta propostaConCampiBaseComuneESpecifico() {
        Categoria categoria = new Categoria("Sport");
        categoria.addCampoSpecifico(campo("Arbitro", TipoCampo.SPECIFICO, TipoDato.BOOLEANO, false));
        return new Proposta(categoria,
                List.of(campo(AppConstants.CAMPO_TITOLO, TipoCampo.BASE, TipoDato.STRINGA, true)),
                List.of(campo("Eta", TipoCampo.COMUNE, TipoDato.INTERO, false)));
    }

    private Campo campo(String nome, TipoCampo tipo, TipoDato tipoDato, boolean obbligatorio) {
        return new Campo(nome, tipo, tipoDato, obbligatorio);
    }
}
