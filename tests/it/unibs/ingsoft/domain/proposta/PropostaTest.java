package it.unibs.ingsoft.domain.proposta;

import it.unibs.ingsoft.domain.catalogo.Campo;
import it.unibs.ingsoft.domain.catalogo.Categoria;
import it.unibs.ingsoft.domain.catalogo.TipoCampo;
import it.unibs.ingsoft.domain.catalogo.TipoDato;
import it.unibs.ingsoft.domain.shared.AppConstants;
import it.unibs.ingsoft.domain.shared.error.DomainException;
import it.unibs.ingsoft.domain.shared.error.ValidationError;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PropostaTest {
    @Test
    void costruttore_conCategoriaValida_creaPropostaInBozzaConId() {
        Proposta proposta = propostaConCampiBaseMinimi();

        assertAll(
                () -> assertNotNull(proposta.getId()),
                () -> assertEquals(StatoProposta.BOZZA, proposta.getStato()),
                () -> assertEquals("Sport", proposta.getCategoria().getNome()),
                () -> assertFalse(proposta.isValida())
        );
    }

    @Test
    void costruttore_conCategoriaNull_lanciaNullCategory() {
        DomainException exception = assertThrows(DomainException.class,
                () -> new Proposta(null, List.of(), List.of()));

        assertInstanceOf(ProposalFailure.NullCategory.class, exception.failure());
    }

    @Test
    void fromJson_conCampiCompleti_ripristinaStatoEValori() {
        LocalDate oggi = LocalDate.now(AppConstants.clock);

        Proposta proposta = Proposta.fromJson(
                "id-1",
                campiBaseMinimiCompleti(),
                List.of(),
                new Categoria("Sport"),
                valoriValidiConDate("Torneo", "2", oggi.plusDays(1), oggi.plusDays(4), oggi.plusDays(5)),
                StatoProposta.APERTA,
                oggi,
                oggi.plusDays(1),
                oggi.plusDays(4),
                List.of("mario"),
                List.of(new PropostaStateChange(StatoProposta.BOZZA, oggi),
                        new PropostaStateChange(StatoProposta.APERTA, oggi)));

        assertAll(
                () -> assertEquals("id-1", proposta.getId()),
                () -> assertEquals(StatoProposta.APERTA, proposta.getStato()),
                () -> assertEquals(oggi, proposta.getDataPubblicazione()),
                () -> assertTrue(proposta.isIscritto("mario")),
                () -> assertEquals("Torneo", proposta.valoreCampoOrDefault(AppConstants.CAMPO_TITOLO, ""))
        );
    }

    @Test
    void fromJson_conIdBlankValoriNullEHistoryVuota_generaIdEMantieneBozzaDefault() {
        Proposta proposta = Proposta.fromJson(
                "  ",
                null,
                null,
                new Categoria("Sport"),
                null,
                null,
                null,
                null,
                null,
                null,
                List.of());

        assertAll(
                () -> assertNotNull(proposta.getId()),
                () -> assertFalse(proposta.getId().isBlank()),
                () -> assertEquals(StatoProposta.BOZZA, proposta.getStato()),
                () -> assertEquals(1, proposta.getStateHistory().size()),
                () -> assertTrue(proposta.getCampi().isEmpty()),
                () -> assertTrue(proposta.getValoriCampi().isEmpty()),
                () -> assertTrue(proposta.getListaAderenti().isEmpty())
        );
    }

    @Test
    void fromJson_conIdNull_generaId() {
        Proposta proposta = Proposta.fromJson(
                null,
                null,
                null,
                new Categoria("Sport"),
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        assertAll(
                () -> assertNotNull(proposta.getId()),
                () -> assertFalse(proposta.getId().isBlank()),
                () -> assertEquals(StatoProposta.BOZZA, proposta.getStato())
        );
    }

    @Test
    void fromJson_conIdConSpazi_loNormalizza() {
        Proposta proposta = Proposta.fromJson(
                "  id-1  ",
                null,
                null,
                new Categoria("Sport"),
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        assertEquals("id-1", proposta.getId());
    }

    @Test
    void getCampi_unisceBaseComuniESpecificiSenzaEsporreLeListeOriginali() {
        Categoria categoria = new Categoria("Sport");
        Campo specifico = campo("Livello", TipoCampo.SPECIFICO, TipoDato.STRINGA, false);
        categoria.addCampoSpecifico(specifico);
        Campo base = campo("Titolo", TipoCampo.BASE, TipoDato.STRINGA, true);
        Campo comune = campo("Note", TipoCampo.COMUNE, TipoDato.STRINGA, false);

        Proposta proposta = new Proposta(categoria, List.of(base), List.of(comune));

        assertEquals(List.of(base, comune, specifico), proposta.getCampi());
    }

    @Test
    void valida_conCampiObbligatoriMancanti_restituisceValidationError() {
        Proposta proposta = propostaBaseCompleta();

        List<ValidationError> errori = new PropostaValidator().valida(proposta);

        assertAll(
                () -> assertFalse(errori.isEmpty()),
                () -> assertTrue(errori.stream()
                        .anyMatch(e -> e.failure() instanceof ProposalValidationFailure.RequiredFieldMissing))
        );
    }

    @Test
    void valida_conValoriCorretti_portaLaPropostaInStatoValida() {
        Proposta proposta = propostaValidaCompleta("2");

        assertAll(
                () -> assertTrue(proposta.isValida()),
                () -> assertTrue(new PropostaValidator().valida(proposta).isEmpty()),
                () -> assertEquals(StatoProposta.VALIDA, proposta.getStato())
        );
    }

    @Test
    void pubblica_conPropostaValida_aprePropostaERegistraData() {
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        Proposta proposta = propostaValidaCompleta("2");

        proposta.pubblica(oggi);

        assertAll(
                () -> assertTrue(proposta.isAperta()),
                () -> assertEquals(oggi, proposta.getDataPubblicazione())
        );
    }

    @Test
    void verificaPubblicabile_conTermineNullONelFuturo_nonLancia() {
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        Proposta termineNull = Proposta.fromJson(
                "id-1",
                campiBaseMinimiCompleti(),
                List.of(),
                new Categoria("Sport"),
                valoriValidiCompleti("2"),
                StatoProposta.VALIDA,
                null,
                null,
                oggi.plusDays(4),
                List.of(),
                null);
        Proposta termineFuturo = propostaValidaCompleta("2");

        assertAll(
                () -> assertDoesNotThrow(() -> termineNull.verificaPubblicabile(oggi)),
                () -> assertDoesNotThrow(() -> termineFuturo.verificaPubblicabile(oggi))
        );
    }

    @Test
    void pubblica_conPropostaNonValida_lanciaNotValidForPublication() {
        Proposta proposta = propostaConCampiBaseMinimi();

        DomainException exception = assertThrows(DomainException.class,
                () -> proposta.pubblica(LocalDate.now(AppConstants.clock)));

        assertInstanceOf(ProposalFailure.NotValidForPublication.class, exception.failure());
    }

    @Test
    void verificaSalvabile_conPropostaNonValidaLanciaEConValidaNonLancia() {
        Proposta nonValida = propostaConCampiBaseMinimi();
        Proposta valida = propostaValidaCompleta("2");

        DomainException exception = assertThrows(DomainException.class, nonValida::verificaSalvabile);

        assertAll(
                () -> assertInstanceOf(ProposalFailure.NotSavable.class, exception.failure()),
                () -> assertDoesNotThrow(valida::verificaSalvabile)
        );
    }

    @Test
    void verificaPubblicabile_conTermineScaduto_lanciaPublicationDeadlineExpired() {
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        Proposta proposta = Proposta.fromJson(
                "id-1",
                campiBaseMinimiCompleti(),
                List.of(),
                new Categoria("Sport"),
                valoriValidiConDate("Torneo", "2", oggi.minusDays(2), oggi.plusDays(2), oggi.plusDays(3)),
                StatoProposta.VALIDA,
                null,
                oggi.minusDays(2),
                oggi.plusDays(2),
                List.of(),
                null);

        DomainException exception = assertThrows(DomainException.class,
                () -> proposta.verificaPubblicabile(oggi));

        assertInstanceOf(ProposalFailure.PublicationDeadlineExpired.class, exception.failure());
    }

    @Test
    void iscrivi_conPostoDisponibile_aggiungeAderenteENonDuplica() {
        Proposta proposta = propostaApertaConCapienza("2");

        proposta.iscrivi("mario", LocalDate.now(AppConstants.clock));
        DomainException duplicato = assertThrows(DomainException.class,
                () -> proposta.iscrivi("mario", LocalDate.now(AppConstants.clock)));

        assertAll(
                () -> assertEquals(List.of("mario"), proposta.getListaAderenti()),
                () -> assertInstanceOf(ProposalFailure.AlreadySubscribed.class, duplicato.failure())
        );
    }

    @Test
    void iscrivi_conUltimoPosto_confermaProposta() {
        Proposta proposta = propostaApertaConCapienza("1");

        proposta.iscrivi("mario", LocalDate.now(AppConstants.clock));
        proposta.confermaSeAperta();

        assertTrue(proposta.isConfermata());
    }

    @Test
    void confermaAnnullaConcludi_quandoStatoNonCompatibile_restituisconoFalse() {
        Proposta bozza = propostaConCampiBaseMinimi();
        Proposta confermata = propostaApertaConCapienza("1");
        confermata.iscrivi("mario", LocalDate.now(AppConstants.clock));
        confermata.confermaSeAperta();

        assertAll(
                () -> assertFalse(bozza.confermaSeAperta()),
                () -> assertFalse(bozza.annullaSeAperta()),
                () -> assertFalse(bozza.concludiSeConfermata()),
                () -> assertFalse(confermata.confermaSeAperta()),
                () -> assertFalse(confermata.annullaSeAperta())
        );
    }

    @Test
    void annullaEConcludi_quandoStatoCompatibile_cambianoStatoERestituisconoTrue() {
        Proposta aperta = propostaApertaConCapienza("2");
        Proposta confermata = propostaApertaConCapienza("1");
        confermata.iscrivi("mario", LocalDate.now(AppConstants.clock));
        confermata.confermaSeAperta();

        assertAll(
                () -> assertTrue(aperta.annullaSeAperta()),
                () -> assertEquals(StatoProposta.ANNULLATA, aperta.getStato()),
                () -> assertTrue(confermata.concludiSeConfermata()),
                () -> assertEquals(StatoProposta.CONCLUSA, confermata.getStato())
        );
    }

    @Test
    void disiscrivi_conUtenteIscritto_rimuoveAderente() {
        Proposta proposta = propostaApertaConCapienza("2");
        proposta.iscrivi("mario", LocalDate.now(AppConstants.clock));

        proposta.disiscrivi("mario", LocalDate.now(AppConstants.clock));

        assertFalse(proposta.isIscritto("mario"));
    }

    @Test
    void ritira_conPropostaApertaPrimaEvento_passaaRitirata() {
        Proposta proposta = propostaApertaConCapienza("2");

        proposta.ritira(LocalDate.now(AppConstants.clock));

        assertEquals(StatoProposta.RITIRATA, proposta.getStato());
    }

    @Test
    void ritira_conDataEventoNull_nonApplicaControlloSulRitardo() {
        Proposta proposta = Proposta.fromJson(
                "id-1",
                campiBaseMinimiCompleti(),
                List.of(),
                new Categoria("Sport"),
                valoriValidiCompleti("2"),
                StatoProposta.APERTA,
                LocalDate.now(AppConstants.clock).minusDays(1),
                LocalDate.now(AppConstants.clock).plusDays(1),
                null,
                List.of(),
                null);

        proposta.ritira(LocalDate.now(AppConstants.clock));

        assertEquals(StatoProposta.RITIRATA, proposta.getStato());
    }

    @Test
    void isRitirabile_restituisceTrueSoloPerApertaOConfermata() {
        Proposta bozza = propostaConCampiBaseMinimi();
        Proposta aperta = propostaApertaConCapienza("2");
        Proposta confermata = propostaApertaConCapienza("1");
        confermata.iscrivi("mario", LocalDate.now(AppConstants.clock));
        confermata.confermaSeAperta();

        assertAll(
                () -> assertFalse(bozza.isRitirabile()),
                () -> assertTrue(aperta.isRitirabile()),
                () -> assertTrue(confermata.isRitirabile())
        );
    }

    @Test
    void cambiaStato_conNullOTransizioneInvalida_lanciaFailureSpecifico() throws Exception {
        Proposta proposta = propostaConCampiBaseMinimi();
        var method = Proposta.class.getDeclaredMethod("cambiaStato", StatoProposta.class);
        method.setAccessible(true);

        Exception nullException = assertThrows(Exception.class, () -> method.invoke(proposta, (Object) null));
        Exception invalidException = assertThrows(Exception.class, () -> method.invoke(proposta, StatoProposta.APERTA));

        assertAll(
                () -> assertInstanceOf(ProposalFailure.NullState.class,
                        ((DomainException) nullException.getCause()).failure()),
                () -> assertInstanceOf(ProposalFailure.InvalidStateTransition.class,
                        ((DomainException) invalidException.getCause()).failure())
        );
    }

    @Test
    void ritira_conBozzaOTroppoTardi_lanciaFailureSpecifici() {
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        Proposta bozza = propostaConCampiBaseMinimi();
        Proposta aperta = propostaApertaConCapienza("2");

        DomainException nonRitirabile = assertThrows(DomainException.class, () -> bozza.ritira(oggi));
        DomainException troppoTardi = assertThrows(DomainException.class,
                () -> aperta.ritira(aperta.getDataEvento()));

        assertAll(
                () -> assertInstanceOf(ProposalFailure.NotWithdrawable.class, nonRitirabile.failure()),
                () -> assertInstanceOf(ProposalFailure.WithdrawalTooLate.class, troppoTardi.failure())
        );
    }

    @Test
    void iscrivi_conStatoTermineScadutoOCapienzaPiena_lanciaFailureSpecifici() {
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        Proposta bozza = propostaConCampiBaseMinimi();
        Proposta scaduta = propostaApertaPersistita("Scaduta", "2",
                oggi.minusDays(2), oggi.plusDays(3), oggi.plusDays(4), List.of());
        Proposta piena = propostaApertaConCapienza("1");
        piena.iscrivi("mario", oggi);

        DomainException nonAperta = assertThrows(DomainException.class, () -> bozza.iscrivi("mario", oggi));
        DomainException termineScaduto = assertThrows(DomainException.class, () -> scaduta.iscrivi("mario", oggi));
        DomainException pienaException = assertThrows(DomainException.class, () -> piena.iscrivi("luigi", oggi));

        assertAll(
                () -> assertInstanceOf(ProposalFailure.NotOpenForSubscription.class, nonAperta.failure()),
                () -> assertInstanceOf(ProposalFailure.SubscriptionDeadlineExpired.class, termineScaduto.failure()),
                () -> assertInstanceOf(ProposalFailure.Full.class, pienaException.failure())
        );
    }

    @Test
    void disiscrivi_conStatoTermineScadutoONonIscritto_lanciaFailureSpecifici() {
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        Proposta bozza = propostaConCampiBaseMinimi();
        Proposta scaduta = propostaApertaPersistita("Scaduta", "2",
                oggi.minusDays(2), oggi.plusDays(3), oggi.plusDays(4), List.of("mario"));
        Proposta aperta = propostaApertaConCapienza("2");

        DomainException nonAperta = assertThrows(DomainException.class, () -> bozza.disiscrivi("mario", oggi));
        DomainException termineScaduto = assertThrows(DomainException.class, () -> scaduta.disiscrivi("mario", oggi));
        DomainException nonIscritto = assertThrows(DomainException.class, () -> aperta.disiscrivi("mario", oggi));

        assertAll(
                () -> assertInstanceOf(ProposalFailure.NotOpenForUnsubscription.class, nonAperta.failure()),
                () -> assertInstanceOf(ProposalFailure.UnsubscriptionDeadlineExpired.class, termineScaduto.failure()),
                () -> assertInstanceOf(ProposalFailure.NotSubscribed.class, nonIscritto.failure())
        );
    }

    @Test
    void applicaTransizionePerScadenza_copreConfermaAnnullamentoConclusioneENessuna() {
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        Proposta daConfermare = propostaApertaPersistita("Confermare", "1",
                oggi.minusDays(2), oggi.plusDays(2), oggi.plusDays(3), List.of("mario"));
        Proposta daAnnullare = propostaApertaPersistita("Annullare", "2",
                oggi.minusDays(2), oggi.plusDays(2), oggi.plusDays(3), List.of("mario"));
        Proposta daConcludere = Proposta.fromJson(
                "conclusa",
                campiBaseMinimiCompleti(),
                List.of(),
                new Categoria("Sport"),
                valoriValidiConDate("Concludere", "1", oggi.minusDays(5), oggi.minusDays(3), oggi.minusDays(1)),
                StatoProposta.CONFERMATA,
                oggi.minusDays(6),
                oggi.minusDays(5),
                oggi.minusDays(3),
                List.of("mario"),
                null);
        Proposta nessuna = propostaApertaPersistita("Futura", "2",
                oggi.plusDays(1), oggi.plusDays(4), oggi.plusDays(5), List.of());

        assertAll(
                () -> assertEquals(EsitoTransizioneProposta.CONFERMATA,
                        daConfermare.applicaTransizionePerScadenza(oggi)),
                () -> assertEquals(EsitoTransizioneProposta.ANNULLATA,
                        daAnnullare.applicaTransizionePerScadenza(oggi)),
                () -> assertEquals(EsitoTransizioneProposta.CONCLUSA,
                        daConcludere.applicaTransizionePerScadenza(oggi)),
                () -> assertEquals(EsitoTransizioneProposta.NESSUNA,
                        nessuna.applicaTransizionePerScadenza(oggi))
        );
    }

    @Test
    void condizioniDiScadenzaECapienza_copronoRamiTrueEFalse() {
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        Proposta apertaFutura = propostaApertaPersistita("Futura", "2",
                oggi.plusDays(1), oggi.plusDays(4), oggi.plusDays(5), List.of("mario"));
        Proposta apertaScadutaPiena = propostaApertaPersistita("Piena", "1",
                oggi.minusDays(2), oggi.plusDays(2), oggi.plusDays(3), List.of("mario"));
        Proposta confermataDaConcludere = Proposta.fromJson(
                "confermata",
                campiBaseMinimiCompleti(),
                List.of(),
                new Categoria("Sport"),
                valoriValidiConDate("Confermata", "1", oggi.minusDays(5), oggi.minusDays(4), oggi.minusDays(2)),
                StatoProposta.CONFERMATA,
                oggi.minusDays(6),
                oggi.minusDays(5),
                oggi.minusDays(4),
                List.of("mario"),
                null);
        Proposta confermataSenzaData = Proposta.fromJson(
                "confermata-senza-data",
                campiBaseMinimiCompleti(),
                List.of(),
                new Categoria("Sport"),
                Map.of(AppConstants.CAMPO_NUM_PARTECIPANTI, "1"),
                StatoProposta.CONFERMATA,
                oggi.minusDays(6),
                oggi.minusDays(5),
                null,
                List.of("mario"),
                null);
        Proposta senzaTermine = Proposta.fromJson(
                "senza-termine",
                campiBaseMinimiCompleti(),
                List.of(),
                new Categoria("Sport"),
                valoriValidiCompleti("2"),
                StatoProposta.APERTA,
                oggi.minusDays(1),
                null,
                oggi.plusDays(4),
                List.of(),
                null);
        Proposta bozza = propostaConCampiBaseMinimi();

        assertAll(
                () -> assertFalse(senzaTermine.isTermineIscrizioneScaduto(oggi)),
                () -> assertFalse(apertaFutura.isTermineIscrizioneScaduto(oggi)),
                () -> assertFalse(apertaFutura.deveChiudereIscrizioni(oggi)),
                () -> assertTrue(apertaScadutaPiena.isTermineIscrizioneScaduto(oggi)),
                () -> assertTrue(apertaScadutaPiena.deveChiudereIscrizioni(oggi)),
                () -> assertTrue(apertaScadutaPiena.haNumeroPartecipantiCompleto()),
                () -> assertTrue(apertaScadutaPiena.isCapienzaRaggiunta()),
                () -> assertFalse(apertaFutura.haNumeroPartecipantiCompleto()),
                () -> assertFalse(apertaFutura.isCapienzaRaggiunta()),
                () -> assertTrue(confermataDaConcludere.deveConcludersi(oggi)),
                () -> assertFalse(confermataSenzaData.deveConcludersi(oggi)),
                () -> assertFalse(bozza.deveConcludersi(oggi))
        );
    }

    @Test
    void applicaEsitoValidazione_conOutcomeNullOInvalido_copreNullERevertBozza() {
        Proposta proposta = propostaValidaCompleta("2");
        PropostaValidationOutcome invalido = new PropostaValidationOutcome(
                List.of(new ValidationError("Titolo",
                        new ProposalValidationFailure.RequiredFieldMissing("Titolo"))),
                null,
                null);

        assertThrows(NullPointerException.class, () -> proposta.applicaEsitoValidazione(null));
        proposta.applicaEsitoValidazione(invalido);

        assertEquals(StatoProposta.BOZZA, proposta.getStato());
    }

    @Test
    void applicaEsitoValidazione_conOutcomeInvalidoSuBozza_lasciaStatoInvariato() {
        Proposta proposta = propostaConCampiBaseMinimi();
        PropostaValidationOutcome invalido = new PropostaValidationOutcome(
                List.of(new ValidationError("Titolo",
                        new ProposalValidationFailure.RequiredFieldMissing("Titolo"))),
                null,
                null);

        proposta.applicaEsitoValidazione(invalido);

        assertEquals(StatoProposta.BOZZA, proposta.getStato());
    }

    @Test
    void applicaEsitoValidazione_conOutcomeValidoSuPropostaGiaValida_nonCambiaHistory() {
        Proposta proposta = propostaValidaCompleta("2");
        int historySize = proposta.getStateHistory().size();
        LocalDate oggi = LocalDate.now(AppConstants.clock);

        proposta.applicaEsitoValidazione(new PropostaValidationOutcome(
                List.of(),
                oggi.plusDays(1),
                oggi.plusDays(4)));

        assertAll(
                () -> assertEquals(StatoProposta.VALIDA, proposta.getStato()),
                () -> assertEquals(historySize, proposta.getStateHistory().size()),
                () -> assertEquals(oggi.plusDays(1), proposta.getTermineIscrizione()),
                () -> assertEquals(oggi.plusDays(4), proposta.getDataEvento())
        );
    }

    @Test
    void aggiornaValoriCampi_ordinaCampiConosciutiEAppendeExtra() {
        Proposta proposta = new Proposta(
                new Categoria("Sport"),
                List.of(campo("A", TipoCampo.BASE, TipoDato.STRINGA, false)),
                List.of(campo("B", TipoCampo.COMUNE, TipoDato.STRINGA, false)));

        proposta.aggiornaValoriCampi(Map.of("Extra", "3", "B", "2", "A", "1"));

        assertEquals(List.of("A", "B", "Extra"), List.copyOf(proposta.getValoriCampi().keySet()));
    }

    @Test
    void aggiornaValoriCampi_conStatoNonModificabile_lanciaFieldsNotModifiable() {
        Proposta proposta = propostaApertaConCapienza("2");

        DomainException exception = assertThrows(DomainException.class,
                () -> proposta.aggiornaValoriCampi(Map.of(AppConstants.CAMPO_TITOLO, "Altro")));

        assertInstanceOf(ProposalFailure.FieldsNotModifiable.class, exception.failure());
    }

    @Test
    void aggiornaValoriCampi_conStatoValida_consenteModifica() {
        Proposta proposta = propostaValidaCompleta("2");

        proposta.aggiornaValoriCampi(Map.of(AppConstants.CAMPO_TITOLO, "Altro"));

        assertEquals("Altro", proposta.valoreCampoOrDefault(AppConstants.CAMPO_TITOLO, ""));
    }

    @Test
    void getDataConclusiva_conValoreAssenteBlankOMalformato_usaDataEvento() {
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        Proposta assente = propostaApertaPersistitaConValori("Assente", "2",
                oggi.plusDays(1), oggi.plusDays(4), null, null);
        Proposta blank = propostaApertaPersistitaConValori("Blank", "2",
                oggi.plusDays(1), oggi.plusDays(4), "   ", null);
        Proposta malformata = propostaApertaPersistitaConValori("Malformata", "2",
                oggi.plusDays(1), oggi.plusDays(4), "non-data", null);

        assertAll(
                () -> assertEquals(assente.getDataEvento(), assente.getDataConclusiva()),
                () -> assertEquals(blank.getDataEvento(), blank.getDataConclusiva()),
                () -> assertEquals(malformata.getDataEvento(), malformata.getDataConclusiva())
        );
    }

    @Test
    void getDataConclusiva_conValoreValido_restituisceDataConclusiva() {
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        Proposta proposta = propostaApertaPersistitaConValori("Conclusiva", "2",
                oggi.plusDays(1), oggi.plusDays(4), oggi.plusDays(6).format(AppConstants.DATE_FMT), null);

        assertEquals(oggi.plusDays(6), proposta.getDataConclusiva());
    }

    @Test
    void getNumeroPartecipanti_conValoriInvalidi_lanciaFailureSpecifici() {
        Proposta mancante = propostaConCampiBaseMinimi();
        Proposta blank = propostaConCampiBaseMinimi();
        blank.aggiornaValoriCampi(Map.of(AppConstants.CAMPO_NUM_PARTECIPANTI, "   "));
        Proposta nonPositivo = propostaConCampiBaseMinimi();
        nonPositivo.aggiornaValoriCampi(Map.of(AppConstants.CAMPO_NUM_PARTECIPANTI, "0"));
        Proposta nonIntero = propostaConCampiBaseMinimi();
        nonIntero.aggiornaValoriCampi(Map.of(AppConstants.CAMPO_NUM_PARTECIPANTI, "due"));

        DomainException missing = assertThrows(DomainException.class, mancante::getNumeroPartecipanti);
        DomainException blankMissing = assertThrows(DomainException.class, blank::getNumeroPartecipanti);
        DomainException notPositive = assertThrows(DomainException.class, nonPositivo::getNumeroPartecipanti);
        DomainException notInteger = assertThrows(DomainException.class, nonIntero::getNumeroPartecipanti);

        assertAll(
                () -> assertInstanceOf(ProposalFailure.ParticipantsMissing.class, missing.failure()),
                () -> assertInstanceOf(ProposalFailure.ParticipantsMissing.class, blankMissing.failure()),
                () -> assertInstanceOf(ProposalFailure.ParticipantsNotPositive.class, notPositive.failure()),
                () -> assertInstanceOf(ProposalFailure.ParticipantsNotInteger.class, notInteger.failure()),
                () -> assertEquals("due", ((ProposalFailure.ParticipantsNotInteger) notInteger.failure()).value())
        );
    }

    @Test
    void statoListeEMappeEsposteSonoImmutabili() {
        Proposta proposta = propostaValidaCompleta("2");

        assertAll(
                () -> assertThrows(UnsupportedOperationException.class,
                        () -> proposta.getValoriCampi().put("x", "y")),
                () -> assertThrows(UnsupportedOperationException.class,
                        () -> proposta.getListaAderenti().add("mario")),
                () -> assertThrows(UnsupportedOperationException.class,
                        () -> proposta.getStateHistory().add(new PropostaStateChange(StatoProposta.APERTA,
                                LocalDate.now(AppConstants.clock))))
        );
    }

    @Test
    void chiaveIdentita_normalizzaValoriBase() {
        Map<String, String> valori = Map.of(
                AppConstants.CAMPO_TITOLO, " Torneo ",
                AppConstants.CAMPO_DATA, "25/12/2026",
                AppConstants.CAMPO_ORA, "16:30",
                AppConstants.CAMPO_LUOGO, " Brescia ");

        assertEquals("torneo|25/12/2026|16:30|brescia", Proposta.chiaveIdentita(valori));
    }

    @Test
    void valoreCampoOrDefaultEChiaveIdentitaIstanziata_usanoValoriCorrenti() {
        Proposta proposta = propostaConCampiBaseMinimi();
        proposta.aggiornaValoriCampi(Map.of(AppConstants.CAMPO_TITOLO, " Torneo "));

        assertAll(
                () -> assertEquals(" Torneo ", proposta.valoreCampoOrDefault(AppConstants.CAMPO_TITOLO, "Default")),
                () -> assertEquals("Default", proposta.valoreCampoOrDefault("Assente", "Default")),
                () -> assertEquals("torneo|||", proposta.getChiaveIdentita())
        );
    }

    private Proposta propostaApertaConCapienza(String numeroPartecipanti) {
        Proposta proposta = propostaValidaCompleta(numeroPartecipanti);
        proposta.pubblica(LocalDate.now(AppConstants.clock));
        return proposta;
    }

    private Proposta propostaApertaPersistita(String titolo,
                                              String numeroPartecipanti,
                                              LocalDate termine,
                                              LocalDate dataEvento,
                                              LocalDate dataConclusiva,
                                              List<String> aderenti) {
        return Proposta.fromJson(
                titolo.toLowerCase(),
                campiBaseMinimiCompleti(),
                List.of(),
                new Categoria("Sport"),
                valoriValidiConDate(titolo, numeroPartecipanti, termine, dataEvento, dataConclusiva),
                StatoProposta.APERTA,
                LocalDate.now(AppConstants.clock).minusDays(1),
                termine,
                dataEvento,
                aderenti,
                null);
    }

    private Proposta propostaApertaPersistitaConValori(String titolo,
                                                       String numeroPartecipanti,
                                                       LocalDate termine,
                                                       LocalDate dataEvento,
                                                       String dataConclusivaRaw,
                                                       List<String> aderenti) {
        Map<String, String> valori = new java.util.LinkedHashMap<>(Map.of(
                AppConstants.CAMPO_TITOLO, titolo,
                AppConstants.CAMPO_NUM_PARTECIPANTI, numeroPartecipanti,
                AppConstants.CAMPO_TERMINE_ISCRIZIONE, termine.format(AppConstants.DATE_FMT),
                AppConstants.CAMPO_LUOGO, "Brescia",
                AppConstants.CAMPO_DATA, dataEvento.format(AppConstants.DATE_FMT),
                AppConstants.CAMPO_ORA, "16:30",
                AppConstants.CAMPO_QUOTA, "10.50"
        ));
        if (dataConclusivaRaw != null) {
            valori.put(AppConstants.CAMPO_DATA_CONCLUSIVA, dataConclusivaRaw);
        }
        return Proposta.fromJson(
                titolo.toLowerCase(),
                campiBaseMinimiCompleti(),
                List.of(),
                new Categoria("Sport"),
                valori,
                StatoProposta.APERTA,
                LocalDate.now(AppConstants.clock).minusDays(1),
                termine,
                dataEvento,
                aderenti == null ? List.of() : aderenti,
                null);
    }

    private Proposta propostaValidaCompleta(String numeroPartecipanti) {
        Proposta proposta = propostaBaseCompleta();
        proposta.aggiornaValoriCampi(valoriValidiCompleti(numeroPartecipanti));
        proposta.valida();
        return proposta;
    }

    private Proposta propostaConCampiBaseMinimi() {
        return new Proposta(new Categoria("Sport"),
                List.of(campo(AppConstants.CAMPO_NUM_PARTECIPANTI, TipoCampo.BASE, TipoDato.INTERO, true)),
                List.of());
    }

    private Proposta propostaBaseCompleta() {
        return new Proposta(new Categoria("Sport"), campiBaseMinimiCompleti(), List.of());
    }

    private List<Campo> campiBaseMinimiCompleti() {
        return List.of(
                campo(AppConstants.CAMPO_TITOLO, TipoCampo.BASE, TipoDato.STRINGA, true),
                campo(AppConstants.CAMPO_NUM_PARTECIPANTI, TipoCampo.BASE, TipoDato.INTERO, true),
                campo(AppConstants.CAMPO_TERMINE_ISCRIZIONE, TipoCampo.BASE, TipoDato.DATA, true),
                campo(AppConstants.CAMPO_LUOGO, TipoCampo.BASE, TipoDato.STRINGA, true),
                campo(AppConstants.CAMPO_DATA, TipoCampo.BASE, TipoDato.DATA, true),
                campo(AppConstants.CAMPO_ORA, TipoCampo.BASE, TipoDato.ORA, true),
                campo(AppConstants.CAMPO_QUOTA, TipoCampo.BASE, TipoDato.DECIMALE, true),
                campo(AppConstants.CAMPO_DATA_CONCLUSIVA, TipoCampo.BASE, TipoDato.DATA, false));
    }

    private Map<String, String> valoriValidiCompleti(String numeroPartecipanti) {
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        return valoriValidiConDate("Torneo", numeroPartecipanti, oggi.plusDays(1), oggi.plusDays(4), oggi.plusDays(5));
    }

    private Map<String, String> valoriValidiConDate(String titolo, String numeroPartecipanti,
                                                    LocalDate termine, LocalDate dataEvento,
                                                    LocalDate dataConclusiva) {
        return Map.of(
                AppConstants.CAMPO_TITOLO, titolo,
                AppConstants.CAMPO_NUM_PARTECIPANTI, numeroPartecipanti,
                AppConstants.CAMPO_TERMINE_ISCRIZIONE, termine.format(AppConstants.DATE_FMT),
                AppConstants.CAMPO_LUOGO, "Brescia",
                AppConstants.CAMPO_DATA, dataEvento.format(AppConstants.DATE_FMT),
                AppConstants.CAMPO_ORA, "16:30",
                AppConstants.CAMPO_QUOTA, "10.50",
                AppConstants.CAMPO_DATA_CONCLUSIVA, dataConclusiva.format(AppConstants.DATE_FMT)
        );
    }

    private Campo campo(String nome, TipoCampo tipo, TipoDato tipoDato, boolean obbligatorio) {
        return new Campo(nome, tipo, tipoDato, obbligatorio);
    }
}
