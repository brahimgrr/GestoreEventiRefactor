package it.unibs.ingsoft.domain;

import it.unibs.ingsoft.domain.error.DomainErrorCode;
import it.unibs.ingsoft.domain.error.DomainException;
import it.unibs.ingsoft.domain.error.ValidationError;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
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
    void costruttore_conCategoriaNull_lanciaIllegalStateException() {
        assertThrows(IllegalStateException.class, () -> new Proposta(null, List.of(), List.of()));
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
    void valida_conDatiValidiDaBozzaAValida_aggiornaStato() {
        Proposta proposta = propostaConCampiBaseMinimi();

        proposta.aggiornaValoriCampi(valoriValidi("2"));
        proposta.valida();

        assertEquals(StatoProposta.VALIDA, proposta.getStato());
    }

    @Test
    void valida_conDatiValidiDaBozzaAValida_aggiungeVoceAllaCronologia() {
        Proposta proposta = propostaConCampiBaseMinimi();

        proposta.aggiornaValoriCampi(valoriValidi("2"));
        proposta.valida();

        assertEquals(StatoProposta.VALIDA, proposta.getStateHistory().get(1).stato());
    }

    @Test
    void pubblica_conDataValidaDaValidaAAperta_aggiornaStato() {
        Proposta proposta = propostaValidaConCapienza("2");

        proposta.pubblica(LocalDate.now(AppConstants.clock));

        assertEquals(StatoProposta.APERTA, proposta.getStato());
    }

    @Test
    void pubblica_conPropostaInBozza_lanciaIllegalStateException() {
        Proposta proposta = propostaConCampiBaseMinimi();

        assertThrows(IllegalStateException.class, () -> proposta.pubblica(LocalDate.now(AppConstants.clock)));
    }

    @Test
    void valida_quandoStatoValidaEValoriNonValidi_riportaStatoABozza() {
        Proposta proposta = propostaValidaConCapienza("2");

        proposta.aggiornaValoriCampi(Map.of(AppConstants.CAMPO_NUM_PARTECIPANTI, "0"));
        proposta.valida();

        assertEquals(StatoProposta.BOZZA, proposta.getStato());
    }

    @Test
    void valida_quandoStatoValidaEValoriNonValidi_nonAggiungeVoceAllaCronologiaPerIlRitornoABozza() {
        Proposta proposta = propostaValidaConCapienza("2");

        proposta.aggiornaValoriCampi(Map.of(AppConstants.CAMPO_NUM_PARTECIPANTI, "0"));
        proposta.valida();

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

        proposta.aggiornaValoriCampi(valori);

        assertEquals(List.of(AppConstants.CAMPO_TITOLO, "Eta", "Arbitro", "Extra legacy"),
                proposta.getValoriCampi().keySet().stream().toList());
    }

    @Test
    void putAllValoriCampi_conPropostaAperta_lanciaIllegalStateException() {
        Proposta proposta = propostaApertaConCapienza("2");

        assertThrows(IllegalStateException.class,
                () -> proposta.aggiornaValoriCampi(Map.of(AppConstants.CAMPO_TITOLO, "Nuovo titolo")));
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

        proposta.iscrivi("mario", LocalDate.now(AppConstants.clock));

        assertEquals(List.of("mario"), proposta.getListaAderenti());
    }

    @Test
    void addAderente_conPropostaNonAperta_lanciaIllegalStateException() {
        Proposta proposta = propostaConCampiBaseMinimi();

        assertThrows(IllegalStateException.class, () -> proposta.iscrivi("mario", LocalDate.now(AppConstants.clock)));
    }

    @Test
    void addAderente_conUsernameGiaIscritto_lanciaIllegalStateException() {
        Proposta proposta = propostaApertaConCapienza("2");
        proposta.iscrivi("mario", LocalDate.now(AppConstants.clock));

        assertThrows(IllegalStateException.class, () -> proposta.iscrivi("mario", LocalDate.now(AppConstants.clock)));
    }

    @Test
    void addAderente_conCapienzaMassimaRaggiunta_lanciaIllegalStateException() {
        Proposta proposta = propostaApertaConCapienza("1");
        proposta.iscrivi("mario", LocalDate.now(AppConstants.clock));

        assertThrows(IllegalStateException.class, () -> proposta.iscrivi("luigi", LocalDate.now(AppConstants.clock)));
    }

    @Test
    void removeAderente_conPropostaApertaETermineNonScaduto_rimuoveUsername() {
        Proposta proposta = propostaApertaConCapienza("2");
        proposta.iscrivi("mario", LocalDate.now(AppConstants.clock));

        proposta.disiscrivi("mario", LocalDate.now(AppConstants.clock));

        assertTrue(proposta.getListaAderenti().isEmpty());
    }

    @Test
    void removeAderente_conPropostaNonAperta_lanciaIllegalStateException() {
        Proposta proposta = propostaConCampiBaseMinimi();

        assertThrows(IllegalStateException.class,
                () -> proposta.disiscrivi("mario", LocalDate.now(AppConstants.clock)));
    }

    @Test
    void removeAderente_conTermineIscrizioneScaduto_lanciaIllegalStateException() {
        Proposta proposta = propostaApertaConCapienza("2");
        proposta.iscrivi("mario", LocalDate.now(AppConstants.clock));

        assertThrows(IllegalStateException.class,
                () -> proposta.disiscrivi("mario", LocalDate.now(AppConstants.clock).plusDays(2)));
    }

    @Test
    void getNumeroPartecipanti_conInteroPositivo_restituisceNumeroPartecipanti() {
        Proposta proposta = propostaConCampiBaseMinimi();
        proposta.aggiornaValoriCampi(Map.of(AppConstants.CAMPO_NUM_PARTECIPANTI, " 12 "));

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
        proposta.aggiornaValoriCampi(Map.of(AppConstants.CAMPO_NUM_PARTECIPANTI, "molti"));

        assertThrows(IllegalStateException.class, proposta::getNumeroPartecipanti);
    }

    @Test
    void getNumeroPartecipanti_conZero_lanciaIllegalStateException() {
        Proposta proposta = propostaConCampiBaseMinimi();
        proposta.aggiornaValoriCampi(Map.of(AppConstants.CAMPO_NUM_PARTECIPANTI, "0"));

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
        Proposta proposta = propostaValidaConCapienza("2");

        assertEquals(proposta.getDataEvento(), proposta.getDataConclusiva());
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
        proposta.iscrivi("mario", LocalDate.now(AppConstants.clock));

        assertTrue(proposta.isCapienzaRaggiunta());
    }

    @Test
    void isTermineIscrizioneScaduto_conOggiDopoTermine_restituisceTrue() {
        Proposta proposta = propostaConCampiBaseMinimi();
        proposta.aggiornaValoriCampi(valoriValidi("2"));
        proposta.valida();

        assertTrue(proposta.isTermineIscrizioneScaduto(LocalDate.now(AppConstants.clock).plusDays(5)));
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
        proposta.aggiornaValoriCampi(Map.of(
                AppConstants.CAMPO_TITOLO, "Torneo",
                AppConstants.CAMPO_DATA, "25/12/2026",
                AppConstants.CAMPO_ORA, "16:30",
                AppConstants.CAMPO_LUOGO, "Brescia"
        ));

        assertEquals("torneo|25/12/2026|16:30|brescia", proposta.getChiaveIdentita());
    }

    @Test
    void chiaveIdentita_conCampiMancanti_usaStringheVuote() {
        assertEquals("torneo|||", Proposta.chiaveIdentita(Map.of(AppConstants.CAMPO_TITOLO, "Torneo")));
    }

    @Test
    void valoreCampoOrDefault_conCampoPresenteEAssente_restituisceValoreODefault() {
        Proposta proposta = propostaConCampiBaseMinimi();
        proposta.aggiornaValoriCampi(Map.of(AppConstants.CAMPO_TITOLO, "Torneo"));

        assertAll(
                () -> assertEquals("Torneo", proposta.valoreCampoOrDefault(AppConstants.CAMPO_TITOLO, "default")),
                () -> assertEquals("default", proposta.valoreCampoOrDefault(AppConstants.CAMPO_LUOGO, "default"))
        );
    }

    @Test
    void costruttore_conListeCampiNull_creaPropostaSenzaCampiMaConCategoria() {
        Proposta proposta = new Proposta(new Categoria("Sport"), null, null);

        assertAll(
                () -> assertEquals("Sport", proposta.getCategoria().getNome()),
                () -> assertTrue(proposta.getCampi().isEmpty())
        );
    }

    @Test
    void fromJson_conValoriNull_mantieneDefaultDelCostruttore() {
        Proposta proposta = Proposta.fromJson(null, null, new Categoria("Sport"),
                null, null, null, null, null, null, null);

        assertAll(
                () -> assertEquals(StatoProposta.BOZZA, proposta.getStato()),
                () -> assertTrue(proposta.getValoriCampi().isEmpty()),
                () -> assertTrue(proposta.getListaAderenti().isEmpty()),
                () -> assertEquals(1, proposta.getStateHistory().size())
        );
    }

    @Test
    void fromJson_conStateHistoryVuota_mantieneCronologiaIniziale() {
        Proposta proposta = Proposta.fromJson(
                List.of(campo(AppConstants.CAMPO_NUM_PARTECIPANTI, TipoCampo.BASE, TipoDato.INTERO, true)),
                List.of(),
                new Categoria("Sport"),
                Map.of(AppConstants.CAMPO_NUM_PARTECIPANTI, "3"),
                StatoProposta.APERTA,
                LocalDate.of(2026, 5, 13),
                LocalDate.of(2026, 5, 20),
                LocalDate.of(2026, 5, 25),
                List.of("mario"),
                List.of());

        assertAll(
                () -> assertEquals(StatoProposta.APERTA, proposta.getStato()),
                () -> assertEquals(LocalDate.of(2026, 5, 13), proposta.getDataPubblicazione()),
                () -> assertEquals(LocalDate.of(2026, 5, 20), proposta.getTermineIscrizione()),
                () -> assertEquals(LocalDate.of(2026, 5, 25), proposta.getDataEvento()),
                () -> assertEquals(List.of("mario"), proposta.getListaAderenti()),
                () -> assertEquals(1, proposta.getStateHistory().size())
        );
    }

    @Test
    void fromJson_conStateHistoryNonVuota_sostituisceCronologiaIniziale() {
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        List<PropostaStateChange> history = List.of(
                new PropostaStateChange(StatoProposta.BOZZA, oggi.minusDays(2)),
                new PropostaStateChange(StatoProposta.VALIDA, oggi.minusDays(1)));

        Proposta proposta = Proposta.fromJson(
                campiBaseMinimiCompleti(),
                List.of(),
                new Categoria("Sport"),
                valoriValidiCompleti("2"),
                StatoProposta.VALIDA,
                null,
                oggi.plusDays(1),
                oggi.plusDays(4),
                List.of(),
                history);

        assertEquals(history, proposta.getStateHistory());
    }

    @Test
    void validatoriStaticiDate_conInputNullOInvalido_restituisconoFalse() {
        LocalDate oggi = LocalDate.now(AppConstants.clock);

        assertAll(
                () -> assertFalse(Proposta.isTermineIscrizioneValido(null)),
                () -> assertFalse(Proposta.isTermineIscrizioneValido(oggi)),
                () -> assertFalse(Proposta.isDataEventoValida(null, oggi)),
                () -> assertFalse(Proposta.isDataEventoValida(oggi.plusDays(1), oggi)),
                () -> assertFalse(Proposta.isDataConclusivaValida(null, oggi)),
                () -> assertFalse(Proposta.isDataConclusivaValida(oggi.minusDays(1), oggi))
        );
    }

    @Test
    void verificaSalvabile_conPropostaValida_nonLanciaEccezioni() {
        assertDoesNotThrow(() -> propostaValidaCompleta("2").verificaSalvabile());
    }

    @Test
    void verificaSalvabile_conPropostaNonValida_lanciaEccezione() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> propostaBaseCompleta().verificaSalvabile());

        assertEquals(DomainErrorCode.PROPOSTA_NOT_SALVABILE.name(), exception.getMessage());
    }

    @Test
    void verificaPubblicabile_conPropostaValidaETermineFuturo_nonLanciaEccezioni() {
        Proposta proposta = propostaValidaCompleta("2");

        assertDoesNotThrow(() -> proposta.verificaPubblicabile(LocalDate.now(AppConstants.clock)));
    }

    @Test
    void verificaPubblicabile_conPropostaValidaETermineNull_nonLanciaEccezioni() {
        Proposta proposta = Proposta.fromJson(
                campiBaseMinimiCompleti(),
                List.of(),
                new Categoria("Sport"),
                valoriValidiCompleti("2"),
                StatoProposta.VALIDA,
                null,
                null,
                LocalDate.now(AppConstants.clock).plusDays(4),
                List.of(),
                List.of(new PropostaStateChange(StatoProposta.BOZZA, LocalDate.now(AppConstants.clock)),
                        new PropostaStateChange(StatoProposta.VALIDA, LocalDate.now(AppConstants.clock))));

        assertDoesNotThrow(() -> proposta.verificaPubblicabile(LocalDate.now(AppConstants.clock)));
    }

    @Test
    void verificaPubblicabile_conTermineScaduto_lanciaEccezioneConCodiceSpecifico() {
        Proposta proposta = Proposta.fromJson(
                campiBaseMinimiCompleti(),
                List.of(),
                new Categoria("Sport"),
                valoriValidiCompleti("2"),
                StatoProposta.VALIDA,
                null,
                LocalDate.now(AppConstants.clock).minusDays(1),
                LocalDate.now(AppConstants.clock).plusDays(3),
                List.of(),
                List.of(new PropostaStateChange(StatoProposta.BOZZA, LocalDate.now(AppConstants.clock)),
                        new PropostaStateChange(StatoProposta.VALIDA, LocalDate.now(AppConstants.clock))));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> proposta.verificaPubblicabile(LocalDate.now(AppConstants.clock)));

        assertEquals(DomainErrorCode.PROPOSTA_PUBLICATION_DEADLINE_EXPIRED.name(), exception.getMessage());
    }

    @Test
    void transizioniNonApplicabili_suStatiNonCompatibili_restituisconoFalse() {
        Proposta bozza = propostaBaseCompleta();
        Proposta valida = propostaValidaCompleta("2");

        assertAll(
                () -> assertFalse(bozza.confermaSeAperta()),
                () -> assertFalse(bozza.annullaSeAperta()),
                () -> assertFalse(valida.concludiSeConfermata())
        );
    }

    @Test
    void cambiaStato_conStatoNull_lanciaEccezioneConCodiceSpecifico() throws Exception {
        InvocationTargetException exception = assertThrows(InvocationTargetException.class,
                () -> invocaCambiaStato(propostaBaseCompleta(), null));

        assertEquals(DomainErrorCode.NULL_STATO_PROPOSTA, ((DomainException) exception.getCause()).code());
    }

    @Test
    void cambiaStato_conTransizioneNonValida_lanciaEccezioneConCodiceSpecifico() throws Exception {
        InvocationTargetException exception = assertThrows(InvocationTargetException.class,
                () -> invocaCambiaStato(propostaBaseCompleta(), StatoProposta.CONCLUSA));

        assertEquals(DomainErrorCode.INVALID_STATE_TRANSITION, ((DomainException) exception.getCause()).code());
    }

    @Test
    void applicaTransizionePerScadenza_conIscrizioniScaduteECapienzaCompleta_confermaProposta() {
        Proposta proposta = propostaApertaConDate("Completa", "1",
                LocalDate.now(AppConstants.clock).minusDays(1),
                LocalDate.now(AppConstants.clock).plusDays(2));
        proposta.iscrivi("mario", LocalDate.now(AppConstants.clock).minusDays(2));

        EsitoTransizioneProposta esito = proposta.applicaTransizionePerScadenza(LocalDate.now(AppConstants.clock));

        assertAll(
                () -> assertEquals(EsitoTransizioneProposta.CONFERMATA, esito),
                () -> assertEquals(StatoProposta.CONFERMATA, proposta.getStato())
        );
    }

    @Test
    void applicaTransizionePerScadenza_conIscrizioniScaduteECapienzaNonCompleta_annullaProposta() {
        Proposta proposta = propostaApertaConDate("Incompleta", "2",
                LocalDate.now(AppConstants.clock).minusDays(1),
                LocalDate.now(AppConstants.clock).plusDays(2));

        EsitoTransizioneProposta esito = proposta.applicaTransizionePerScadenza(LocalDate.now(AppConstants.clock));

        assertAll(
                () -> assertEquals(EsitoTransizioneProposta.ANNULLATA, esito),
                () -> assertEquals(StatoProposta.ANNULLATA, proposta.getStato())
        );
    }

    @Test
    void applicaTransizionePerScadenza_conPropostaConfermataEDataConclusivaPassata_concludeProposta() {
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        Proposta proposta = Proposta.fromJson(
                campiBaseMinimiCompleti(),
                List.of(),
                new Categoria("Sport"),
                valoriValidiConDateCompleti("Conclusione", "1", oggi.minusDays(6), oggi.minusDays(4), oggi.minusDays(2)),
                StatoProposta.CONFERMATA,
                oggi.minusDays(7),
                oggi.minusDays(6),
                oggi.minusDays(4),
                List.of("mario"),
                List.of(new PropostaStateChange(StatoProposta.BOZZA, oggi),
                        new PropostaStateChange(StatoProposta.VALIDA, oggi),
                        new PropostaStateChange(StatoProposta.APERTA, oggi),
                        new PropostaStateChange(StatoProposta.CONFERMATA, oggi)));

        EsitoTransizioneProposta esito = proposta.applicaTransizionePerScadenza(oggi);

        assertAll(
                () -> assertEquals(EsitoTransizioneProposta.CONCLUSA, esito),
                () -> assertEquals(StatoProposta.CONCLUSA, proposta.getStato())
        );
    }

    @Test
    void applicaTransizionePerScadenza_quandoNessunaCondizioneScaduta_restituisceNessuna() {
        Proposta proposta = propostaApertaConDate("Non scaduta", "2",
                LocalDate.now(AppConstants.clock).plusDays(1),
                LocalDate.now(AppConstants.clock).plusDays(4));

        assertEquals(EsitoTransizioneProposta.NESSUNA,
                proposta.applicaTransizionePerScadenza(LocalDate.now(AppConstants.clock)));
    }

    @Test
    void applicaTransizionePerScadenza_conPropostaConfermataSenzaDataConclusiva_restituisceNessuna() {
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        Proposta proposta = Proposta.fromJson(
                campiBaseMinimiCompleti(),
                List.of(),
                new Categoria("Sport"),
                Map.of(AppConstants.CAMPO_TITOLO, "Senza conclusiva",
                        AppConstants.CAMPO_NUM_PARTECIPANTI, "1"),
                StatoProposta.CONFERMATA,
                oggi.minusDays(3),
                oggi.minusDays(2),
                null,
                List.of("mario"),
                List.of(new PropostaStateChange(StatoProposta.BOZZA, oggi),
                        new PropostaStateChange(StatoProposta.VALIDA, oggi),
                        new PropostaStateChange(StatoProposta.APERTA, oggi),
                        new PropostaStateChange(StatoProposta.CONFERMATA, oggi)));

        assertEquals(EsitoTransizioneProposta.NESSUNA, proposta.applicaTransizionePerScadenza(oggi));
    }

    @Test
    void applicaTransizionePerScadenza_conPropostaConfermataEConclusivaNonPassata_restituisceNessuna() {
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        Proposta proposta = Proposta.fromJson(
                campiBaseMinimiCompleti(),
                List.of(),
                new Categoria("Sport"),
                valoriValidiConDateCompleti("Non conclusa", "1", oggi.minusDays(4), oggi.minusDays(2), oggi.plusDays(1)),
                StatoProposta.CONFERMATA,
                oggi.minusDays(5),
                oggi.minusDays(4),
                oggi.minusDays(2),
                List.of("mario"),
                List.of(new PropostaStateChange(StatoProposta.BOZZA, oggi),
                        new PropostaStateChange(StatoProposta.VALIDA, oggi),
                        new PropostaStateChange(StatoProposta.APERTA, oggi),
                        new PropostaStateChange(StatoProposta.CONFERMATA, oggi)));

        assertEquals(EsitoTransizioneProposta.NESSUNA, proposta.applicaTransizionePerScadenza(oggi));
    }

    @Test
    void iscrivi_conTermineScaduto_lanciaEccezioneDiDeadline() {
        Proposta proposta = propostaApertaConDate("Scaduta", "2",
                LocalDate.now(AppConstants.clock).minusDays(1),
                LocalDate.now(AppConstants.clock).plusDays(2));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> proposta.iscrivi("mario", LocalDate.now(AppConstants.clock)));

        assertEquals(DomainErrorCode.PROPOSTA_SUBSCRIPTION_DEADLINE_EXPIRED.name(), exception.getMessage());
    }

    @Test
    void disiscrivi_conUsernameNonIscritto_lanciaEccezione() {
        Proposta proposta = propostaApertaConDate("Non iscritto", "2",
                LocalDate.now(AppConstants.clock).plusDays(1),
                LocalDate.now(AppConstants.clock).plusDays(4));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> proposta.disiscrivi("mario", LocalDate.now(AppConstants.clock)));

        assertEquals(DomainErrorCode.PROPOSTA_NOT_SUBSCRIBED.name(), exception.getMessage());
    }

    @Test
    void iscrivi_conTermineNullEAperta_aggiungeUsername() {
        Proposta proposta = Proposta.fromJson(
                campiBaseMinimiCompleti(),
                List.of(),
                new Categoria("Sport"),
                Map.of(AppConstants.CAMPO_TITOLO, "Senza termine",
                        AppConstants.CAMPO_NUM_PARTECIPANTI, "2"),
                StatoProposta.APERTA,
                LocalDate.now(AppConstants.clock),
                null,
                LocalDate.now(AppConstants.clock).plusDays(4),
                List.of(),
                List.of(new PropostaStateChange(StatoProposta.BOZZA, LocalDate.now(AppConstants.clock)),
                        new PropostaStateChange(StatoProposta.VALIDA, LocalDate.now(AppConstants.clock)),
                        new PropostaStateChange(StatoProposta.APERTA, LocalDate.now(AppConstants.clock))));

        proposta.iscrivi("mario", LocalDate.now(AppConstants.clock));

        assertEquals(List.of("mario"), proposta.getListaAderenti());
    }

    @Test
    void ritira_conPropostaNonRitirabile_lanciaEccezione() {
        Proposta proposta = propostaBaseCompleta();

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> proposta.ritira(LocalDate.now(AppConstants.clock)));

        assertEquals(DomainErrorCode.PROPOSTA_NOT_WITHDRAWABLE.name(), exception.getMessage());
    }

    @Test
    void ritira_conDataEventoOggi_lanciaEccezioneDiRitiroTardivo() {
        Proposta proposta = propostaApertaConDate("Oggi", "2",
                LocalDate.now(AppConstants.clock).minusDays(2),
                LocalDate.now(AppConstants.clock));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> proposta.ritira(LocalDate.now(AppConstants.clock)));

        assertEquals(DomainErrorCode.PROPOSTA_WITHDRAWAL_TOO_LATE.name(), exception.getMessage());
    }

    @Test
    void ritira_conPropostaApertaPrimaEvento_impostaStatoRitirata() {
        Proposta proposta = propostaApertaConDate("Ritirabile", "2",
                LocalDate.now(AppConstants.clock).plusDays(1),
                LocalDate.now(AppConstants.clock).plusDays(4));

        proposta.ritira(LocalDate.now(AppConstants.clock));

        assertEquals(StatoProposta.RITIRATA, proposta.getStato());
    }

    @Test
    void ritira_conPropostaConfermataPrimaEvento_impostaStatoRitirata() {
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        Proposta proposta = Proposta.fromJson(
                campiBaseMinimiCompleti(),
                List.of(),
                new Categoria("Sport"),
                valoriValidiConDateCompleti("Confermata", "1", oggi.minusDays(2), oggi.plusDays(4), oggi.plusDays(5)),
                StatoProposta.CONFERMATA,
                oggi.minusDays(3),
                oggi.minusDays(2),
                oggi.plusDays(4),
                List.of("mario"),
                List.of(new PropostaStateChange(StatoProposta.BOZZA, oggi),
                        new PropostaStateChange(StatoProposta.VALIDA, oggi),
                        new PropostaStateChange(StatoProposta.APERTA, oggi),
                        new PropostaStateChange(StatoProposta.CONFERMATA, oggi)));

        proposta.ritira(oggi);

        assertEquals(StatoProposta.RITIRATA, proposta.getStato());
    }

    @Test
    void ritira_conPropostaApertaSenzaDataEvento_impostaStatoRitirata() {
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        Proposta proposta = Proposta.fromJson(
                campiBaseMinimiCompleti(),
                List.of(),
                new Categoria("Sport"),
                Map.of(AppConstants.CAMPO_TITOLO, "Senza data",
                        AppConstants.CAMPO_NUM_PARTECIPANTI, "1"),
                StatoProposta.APERTA,
                oggi,
                oggi.plusDays(1),
                null,
                List.of(),
                List.of(new PropostaStateChange(StatoProposta.BOZZA, oggi),
                        new PropostaStateChange(StatoProposta.VALIDA, oggi),
                        new PropostaStateChange(StatoProposta.APERTA, oggi)));

        proposta.ritira(oggi);

        assertEquals(StatoProposta.RITIRATA, proposta.getStato());
    }

    @Test
    void valida_conTermineNonFuturoDataTroppoPrestoEConclusivaPrecedente_restituisceTreErrori() {
        Proposta proposta = propostaBaseCompleta();
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        proposta.aggiornaValoriCampi(valoriValidiConDateCompleti("Date invalide", "2", oggi, oggi.plusDays(1), oggi.minusDays(1)));

        List<DomainErrorCode> codici = proposta.valida().stream().map(ValidationError::code).toList();

        assertAll(
                () -> assertTrue(codici.contains(DomainErrorCode.TERMINE_ISCRIZIONE_NON_FUTURO)),
                () -> assertTrue(codici.contains(DomainErrorCode.DATA_EVENTO_TROPPO_PRESTO)),
                () -> assertTrue(codici.contains(DomainErrorCode.DATA_CONCLUSIVA_PRECEDENTE))
        );
    }

    @Test
    void valida_conCampoObbligatorioMancante_restituisceErroreSulCampo() {
        Proposta proposta = new Proposta(new Categoria("Sport"),
                List.of(campo("Nome obbligatorio", TipoCampo.BASE, TipoDato.STRINGA, true)),
                List.of());

        ValidationError errore = proposta.valida().get(0);

        assertAll(
                () -> assertEquals(DomainErrorCode.CAMPO_OBBLIGATORIO_MANCANTE, errore.code()),
                () -> assertEquals("Nome obbligatorio", errore.fieldName())
        );
    }

    @Test
    void valida_conCampoObbligatorioBlank_restituisceErroreSulCampo() {
        Proposta proposta = new Proposta(new Categoria("Sport"),
                List.of(campo("Nome obbligatorio", TipoCampo.BASE, TipoDato.STRINGA, true)),
                List.of());
        proposta.aggiornaValoriCampi(Map.of("Nome obbligatorio", "   "));

        ValidationError errore = proposta.valida().get(0);

        assertEquals(DomainErrorCode.CAMPO_OBBLIGATORIO_MANCANTE, errore.code());
    }

    @Test
    void valida_conDateBlankONonParsabili_ignoraControlliCronologiciEValidaSeAltriCampiSonoCorretti() {
        Proposta proposta = new Proposta(new Categoria("Sport"), List.of(
                campo(AppConstants.CAMPO_TITOLO, TipoCampo.BASE, TipoDato.STRINGA, true),
                campo(AppConstants.CAMPO_NUM_PARTECIPANTI, TipoCampo.BASE, TipoDato.INTERO, true),
                campo(AppConstants.CAMPO_TERMINE_ISCRIZIONE, TipoCampo.BASE, TipoDato.DATA, false),
                campo(AppConstants.CAMPO_DATA, TipoCampo.BASE, TipoDato.DATA, false),
                campo(AppConstants.CAMPO_DATA_CONCLUSIVA, TipoCampo.BASE, TipoDato.DATA, false)), List.of());
        proposta.aggiornaValoriCampi(Map.of(
                AppConstants.CAMPO_TITOLO, "Date non parsabili",
                AppConstants.CAMPO_NUM_PARTECIPANTI, "2",
                AppConstants.CAMPO_TERMINE_ISCRIZIONE, "   ",
                AppConstants.CAMPO_DATA, "non data",
                AppConstants.CAMPO_DATA_CONCLUSIVA, "non data"));

        assertAll(
                () -> assertTrue(proposta.valida().isEmpty()),
                () -> assertEquals(StatoProposta.VALIDA, proposta.getStato()),
                () -> assertNull(proposta.getTermineIscrizione()),
                () -> assertNull(proposta.getDataEvento())
        );
    }

    @Test
    void valida_conNumeroPartecipantiBlank_nonAggiungeErroreNumerico() {
        Proposta proposta = new Proposta(new Categoria("Sport"),
                List.of(campo(AppConstants.CAMPO_NUM_PARTECIPANTI, TipoCampo.BASE, TipoDato.INTERO, false)),
                List.of());
        proposta.aggiornaValoriCampi(Map.of(AppConstants.CAMPO_NUM_PARTECIPANTI, "   "));

        assertTrue(proposta.valida().isEmpty());
    }

    @Test
    void validaCampo_conTermineIscrizioneValidaMaDataTroppoPresto_restituisceErroreData() {
        Proposta proposta = propostaBaseCompleta();
        LocalDate oggi = LocalDate.now(AppConstants.clock);

        List<ValidationError> errori = proposta.validaCampo(
                Map.of(AppConstants.CAMPO_DATA, oggi.plusDays(2).format(AppConstants.DATE_FMT)),
                AppConstants.CAMPO_TERMINE_ISCRIZIONE,
                oggi.plusDays(1).format(AppConstants.DATE_FMT));

        assertEquals(DomainErrorCode.DATA_EVENTO_TROPPO_PRESTO, errori.get(0).code());
    }

    @Test
    void validaCampo_conTermineIscrizioneNonFuturo_restituisceErroreTermine() {
        Proposta proposta = propostaBaseCompleta();
        LocalDate oggi = LocalDate.now(AppConstants.clock);

        List<ValidationError> errori = proposta.validaCampo(
                Map.of(),
                AppConstants.CAMPO_TERMINE_ISCRIZIONE,
                oggi.format(AppConstants.DATE_FMT));

        assertEquals(DomainErrorCode.TERMINE_ISCRIZIONE_NON_FUTURO, errori.get(0).code());
    }

    @Test
    void validaCampo_conCampoDataTroppoPresto_restituisceErroreData() {
        Proposta proposta = propostaBaseCompleta();
        LocalDate oggi = LocalDate.now(AppConstants.clock);

        List<ValidationError> errori = proposta.validaCampo(
                Map.of(AppConstants.CAMPO_TERMINE_ISCRIZIONE, oggi.plusDays(2).format(AppConstants.DATE_FMT)),
                AppConstants.CAMPO_DATA,
                oggi.plusDays(3).format(AppConstants.DATE_FMT));

        assertEquals(DomainErrorCode.DATA_EVENTO_TROPPO_PRESTO, errori.get(0).code());
    }

    @Test
    void validaCampo_conDataConclusivaPrecedenteSuCampoData_restituisceErroreConclusiva() {
        Proposta proposta = propostaBaseCompleta();
        LocalDate oggi = LocalDate.now(AppConstants.clock);

        List<ValidationError> errori = proposta.validaCampo(
                Map.of(
                        AppConstants.CAMPO_TERMINE_ISCRIZIONE, oggi.plusDays(1).format(AppConstants.DATE_FMT),
                        AppConstants.CAMPO_DATA_CONCLUSIVA, oggi.plusDays(2).format(AppConstants.DATE_FMT)),
                AppConstants.CAMPO_DATA,
                oggi.plusDays(4).format(AppConstants.DATE_FMT));

        assertEquals(DomainErrorCode.DATA_CONCLUSIVA_PRECEDENTE, errori.get(0).code());
    }

    @Test
    void validaCampo_conDataConclusivaPrecedenteSuCampoConclusiva_restituisceErroreConclusiva() {
        Proposta proposta = propostaBaseCompleta();
        LocalDate oggi = LocalDate.now(AppConstants.clock);

        List<ValidationError> errori = proposta.validaCampo(
                Map.of(AppConstants.CAMPO_DATA, oggi.plusDays(4).format(AppConstants.DATE_FMT)),
                AppConstants.CAMPO_DATA_CONCLUSIVA,
                oggi.plusDays(3).format(AppConstants.DATE_FMT));

        assertEquals(DomainErrorCode.DATA_CONCLUSIVA_PRECEDENTE, errori.get(0).code());
    }

    @Test
    void validaCampo_conDataConclusivaValida_restituisceListaVuota() {
        Proposta proposta = propostaBaseCompleta();
        LocalDate oggi = LocalDate.now(AppConstants.clock);

        assertTrue(proposta.validaCampo(
                Map.of(AppConstants.CAMPO_DATA, oggi.plusDays(4).format(AppConstants.DATE_FMT)),
                AppConstants.CAMPO_DATA_CONCLUSIVA,
                oggi.plusDays(5).format(AppConstants.DATE_FMT)).isEmpty());
    }

    @Test
    void validaCampo_conCampoNonData_restituisceListaVuota() {
        assertTrue(propostaBaseCompleta().validaCampo(Map.of(), AppConstants.CAMPO_TITOLO, "Torneo").isEmpty());
    }

    @Test
    void getDataConclusiva_conCampoValido_restituisceDataConclusiva() {
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        Proposta proposta = propostaApertaConDate("Conclusiva", "2", oggi.plusDays(1), oggi.plusDays(4));

        assertEquals(oggi.plusDays(5), proposta.getDataConclusiva());
    }

    @Test
    void getDataConclusiva_conCampoMalformato_restituisceDataEvento() {
        Proposta proposta = propostaValidaCompleta("2");
        proposta.aggiornaValoriCampi(Map.of(AppConstants.CAMPO_DATA_CONCLUSIVA, "non data"));

        assertEquals(proposta.getDataEvento(), proposta.getDataConclusiva());
    }

    @Test
    void getDataConclusiva_conCampoBlank_restituisceDataEvento() {
        Proposta proposta = propostaValidaCompleta("2");
        proposta.aggiornaValoriCampi(Map.of(AppConstants.CAMPO_DATA_CONCLUSIVA, "   "));

        assertEquals(proposta.getDataEvento(), proposta.getDataConclusiva());
    }

    @Test
    void getNumeroPartecipanti_conCampoBlank_lanciaIllegalStateException() {
        Proposta proposta = propostaConCampiBaseMinimi();
        proposta.aggiornaValoriCampi(Map.of(AppConstants.CAMPO_NUM_PARTECIPANTI, "   "));

        assertThrows(IllegalStateException.class, proposta::getNumeroPartecipanti);
    }

    @Test
    void isCapienzaRaggiunta_conAderentiMenoDellaCapienza_restituisceFalse() {
        Proposta proposta = propostaApertaConCapienza("2");
        proposta.iscrivi("mario", LocalDate.now(AppConstants.clock));

        assertFalse(proposta.isCapienzaRaggiunta());
    }

    @Test
    void isTermineIscrizioneScaduto_conOggiUgualeOTermineFuturo_restituisceFalse() {
        Proposta proposta = propostaValidaConCapienza("2");

        assertAll(
                () -> assertFalse(proposta.isTermineIscrizioneScaduto(proposta.getTermineIscrizione())),
                () -> assertFalse(proposta.isTermineIscrizioneScaduto(LocalDate.now(AppConstants.clock)))
        );
    }

    private void invocaCambiaStato(Proposta proposta, StatoProposta stato) throws Exception {
        Method method = Proposta.class.getDeclaredMethod("cambiaStato", StatoProposta.class);
        method.setAccessible(true);
        method.invoke(proposta, stato);
    }

    private Proposta propostaApertaConCapienza(String numeroPartecipanti) {
        Proposta proposta = propostaValidaConCapienza(numeroPartecipanti);
        proposta.pubblica(LocalDate.now(AppConstants.clock));
        return proposta;
    }

    private Proposta propostaValidaConCapienza(String numeroPartecipanti) {
        Proposta proposta = propostaConCampiBaseMinimi();
        proposta.aggiornaValoriCampi(valoriValidi(numeroPartecipanti));
        proposta.valida();
        return proposta;
    }

    private Map<String, String> valoriValidi(String numeroPartecipanti) {
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        return Map.of(
                AppConstants.CAMPO_NUM_PARTECIPANTI, numeroPartecipanti,
                AppConstants.CAMPO_TERMINE_ISCRIZIONE, oggi.plusDays(1).format(AppConstants.DATE_FMT),
                AppConstants.CAMPO_DATA, oggi.plusDays(4).format(AppConstants.DATE_FMT)
        );
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

    private Proposta propostaBaseCompleta() {
        return new Proposta(new Categoria("Sport"), campiBaseMinimiCompleti(), List.of());
    }

    private Proposta propostaValidaCompleta(String numeroPartecipanti) {
        Proposta proposta = propostaBaseCompleta();
        proposta.aggiornaValoriCampi(valoriValidiCompleti(numeroPartecipanti));
        proposta.valida();
        return proposta;
    }

    private Proposta propostaApertaConDate(String titolo, String numeroPartecipanti, LocalDate termine, LocalDate dataEvento) {
        Proposta proposta = propostaBaseCompleta();
        proposta.aggiornaValoriCampi(valoriValidiConDateCompleti(titolo, numeroPartecipanti, termine, dataEvento, dataEvento.plusDays(1)));
        if (proposta.valida().isEmpty()) {
            proposta.pubblica(LocalDate.now(AppConstants.clock).minusDays(2));
            return proposta;
        }

        return Proposta.fromJson(
                campiBaseMinimiCompleti(),
                List.of(),
                new Categoria("Sport"),
                valoriValidiConDateCompleti(titolo, numeroPartecipanti, termine, dataEvento, dataEvento.plusDays(1)),
                StatoProposta.APERTA,
                LocalDate.now(AppConstants.clock).minusDays(2),
                termine,
                dataEvento,
                List.of(),
                List.of(new PropostaStateChange(StatoProposta.BOZZA, LocalDate.now(AppConstants.clock)),
                        new PropostaStateChange(StatoProposta.VALIDA, LocalDate.now(AppConstants.clock)),
                        new PropostaStateChange(StatoProposta.APERTA, LocalDate.now(AppConstants.clock))));
    }

    private List<Campo> campiBaseMinimiCompleti() {
        return List.of(
                campo(AppConstants.CAMPO_TITOLO, TipoCampo.BASE, TipoDato.STRINGA, true),
                campo(AppConstants.CAMPO_NUM_PARTECIPANTI, TipoCampo.BASE, TipoDato.INTERO, true),
                campo(AppConstants.CAMPO_TERMINE_ISCRIZIONE, TipoCampo.BASE, TipoDato.DATA, true),
                campo(AppConstants.CAMPO_DATA, TipoCampo.BASE, TipoDato.DATA, true),
                campo(AppConstants.CAMPO_DATA_CONCLUSIVA, TipoCampo.BASE, TipoDato.DATA, false));
    }

    private Map<String, String> valoriValidiCompleti(String numeroPartecipanti) {
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        return valoriValidiConDateCompleti("Torneo", numeroPartecipanti, oggi.plusDays(1), oggi.plusDays(4), oggi.plusDays(5));
    }

    private Map<String, String> valoriValidiConDateCompleti(String titolo, String numeroPartecipanti,
                                                            LocalDate termine, LocalDate dataEvento,
                                                            LocalDate dataConclusiva) {
        return Map.of(
                AppConstants.CAMPO_TITOLO, titolo,
                AppConstants.CAMPO_NUM_PARTECIPANTI, numeroPartecipanti,
                AppConstants.CAMPO_TERMINE_ISCRIZIONE, termine.format(AppConstants.DATE_FMT),
                AppConstants.CAMPO_DATA, dataEvento.format(AppConstants.DATE_FMT),
                AppConstants.CAMPO_DATA_CONCLUSIVA, dataConclusiva.format(AppConstants.DATE_FMT)
        );
    }

    private Campo campo(String nome, TipoCampo tipo, TipoDato tipoDato, boolean obbligatorio) {
        return new Campo(nome, tipo, tipoDato, obbligatorio);
    }
}
