package it.unibs.ingsoft.domain.proposta;

import it.unibs.ingsoft.domain.catalogo.Campo;
import it.unibs.ingsoft.domain.catalogo.Categoria;
import it.unibs.ingsoft.domain.catalogo.TipoCampo;
import it.unibs.ingsoft.domain.catalogo.TipoDato;
import it.unibs.ingsoft.domain.shared.AppConstants;
import it.unibs.ingsoft.domain.shared.error.ValidationError;
import it.unibs.ingsoft.domain.shared.validation.TypeValidationFailure;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PropostaValidatorTest {
    private final PropostaValidator validator = new PropostaValidator();

    @Test
    void validaCompleta_conPropostaNull_lanciaNullPointerException() {
        assertThrows(NullPointerException.class, () -> validator.validaCompleta(null));
    }

    @Test
    void validaCampo_conPropostaNull_lanciaNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> validator.validaCampo(null, Map.of(), AppConstants.CAMPO_TITOLO, "Torneo"));
    }

    @Test
    void validaCompleta_conValoriValidi_restituisceOutcomeConDateParsed() {
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        Proposta proposta = propostaBase();
        proposta.aggiornaValoriCampi(valori(oggi.plusDays(1), oggi.plusDays(4), oggi.plusDays(5), "2"));

        PropostaValidationOutcome outcome = validator.validaCompleta(proposta);

        assertAll(
                () -> assertTrue(outcome.valida()),
                () -> assertEquals(oggi.plusDays(1), outcome.termineIscrizione()),
                () -> assertEquals(oggi.plusDays(4), outcome.dataEvento())
        );
    }

    @Test
    void validaCompleta_conObbligatorioMancanteTipoErratoDateErrateEPartecipantiInvalidi_restituisceErroriMirati() {
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        Proposta proposta = propostaBase();
        proposta.aggiornaValoriCampi(Map.of(
                AppConstants.CAMPO_TITOLO, "   ",
                AppConstants.CAMPO_NUM_PARTECIPANTI, "0",
                AppConstants.CAMPO_TERMINE_ISCRIZIONE, oggi.minusDays(1).format(AppConstants.DATE_FMT),
                AppConstants.CAMPO_DATA, oggi.format(AppConstants.DATE_FMT),
                AppConstants.CAMPO_DATA_CONCLUSIVA, oggi.minusDays(2).format(AppConstants.DATE_FMT),
                AppConstants.CAMPO_ORA, "25:99",
                AppConstants.CAMPO_QUOTA, "dieci",
                "Booleano", "forse"
        ));

        List<ValidationError> errori = validator.valida(proposta);

        assertAll(
                () -> assertFailure(errori, ProposalValidationFailure.RequiredFieldMissing.class),
                () -> assertFailure(errori, ProposalValidationFailure.ParticipantsNotPositive.class),
                () -> assertFailure(errori, ProposalValidationFailure.SubscriptionDeadlineNotFuture.class),
                () -> assertFailure(errori, ProposalValidationFailure.EventDateTooEarly.class),
                () -> assertFailure(errori, ProposalValidationFailure.ClosingDateBeforeEvent.class),
                () -> assertFailure(errori, TypeValidationFailure.InvalidTime.class),
                () -> assertFailure(errori, TypeValidationFailure.InvalidDecimal.class),
                () -> assertFailure(errori, TypeValidationFailure.InvalidBoolean.class)
        );
    }

    @Test
    void validaCompleta_conPartecipantiNonIntero_restituisceErroreSpecifico() {
        Proposta proposta = propostaBase();
        proposta.aggiornaValoriCampi(Map.of(AppConstants.CAMPO_NUM_PARTECIPANTI, "due"));

        List<ValidationError> errori = validator.valida(proposta);

        assertFailure(errori, ProposalValidationFailure.ParticipantsNotInteger.class);
    }

    @Test
    void validaCompleta_conPartecipantiMancantiOBlank_nonAggiungeErroreNumericoPartecipanti() {
        Proposta mancante = propostaBase();
        Proposta blank = propostaBase();
        blank.aggiornaValoriCampi(Map.of(AppConstants.CAMPO_NUM_PARTECIPANTI, "   "));

        List<ValidationError> erroriMancante = validator.valida(mancante);
        List<ValidationError> erroriBlank = validator.valida(blank);

        assertAll(
                () -> assertNoFailure(erroriMancante, ProposalValidationFailure.ParticipantsNotInteger.class),
                () -> assertNoFailure(erroriMancante, ProposalValidationFailure.ParticipantsNotPositive.class),
                () -> assertNoFailure(erroriBlank, ProposalValidationFailure.ParticipantsNotInteger.class),
                () -> assertNoFailure(erroriBlank, ProposalValidationFailure.ParticipantsNotPositive.class)
        );
    }

    @Test
    void validaCompleta_conDateBlankOMalformate_nonApplicaRegoleCorrelate() {
        Proposta proposta = propostaBase();
        proposta.aggiornaValoriCampi(Map.of(
                AppConstants.CAMPO_TERMINE_ISCRIZIONE, " ",
                AppConstants.CAMPO_DATA, "non-data",
                AppConstants.CAMPO_DATA_CONCLUSIVA, "non-data",
                AppConstants.CAMPO_NUM_PARTECIPANTI, "2"
        ));

        List<ValidationError> errori = validator.valida(proposta);

        assertAll(
                () -> assertNoFailure(errori, ProposalValidationFailure.SubscriptionDeadlineNotFuture.class),
                () -> assertNoFailure(errori, ProposalValidationFailure.EventDateTooEarly.class),
                () -> assertNoFailure(errori, ProposalValidationFailure.ClosingDateBeforeEvent.class)
        );
    }

    @Test
    void validaCampo_conValoriCorrentiNullEPartecipantiInvalidi_controllaSoloPartecipanti() {
        Proposta proposta = propostaBase();

        List<ValidationError> errori = validator.validaCampo(
                proposta,
                null,
                AppConstants.CAMPO_NUM_PARTECIPANTI,
                "abc");

        assertAll(
                () -> assertFailure(errori, ProposalValidationFailure.ParticipantsNotInteger.class),
                () -> assertNoFailure(errori, TypeValidationFailure.InvalidInteger.class)
        );
    }

    @Test
    void validaCampo_conCampoTipizzatoNonPartecipanti_restituisceErroreTipo() {
        Proposta proposta = propostaBase();

        List<ValidationError> errori = validator.validaCampo(
                proposta,
                Map.of(),
                AppConstants.CAMPO_QUOTA,
                "dieci");

        assertFailure(errori, TypeValidationFailure.InvalidDecimal.class);
    }

    @Test
    void validaCampo_conCampoTipizzatoBlank_nonProduceErroreTipo() {
        Proposta proposta = propostaBase();

        List<ValidationError> errori = validator.validaCampo(
                proposta,
                Map.of(),
                AppConstants.CAMPO_QUOTA,
                "   ");

        assertTrue(errori.isEmpty());
    }

    @Test
    void validaCampo_conCampoNonPresente_nonProduceErroreTipo() {
        Proposta proposta = propostaBase();

        List<ValidationError> errori = validator.validaCampo(proposta, Map.of(), "Campo ignoto", "x");

        assertTrue(errori.isEmpty());
    }

    @Test
    void validaCampo_conTermineIscrizioneNonFuturo_usaFailureSenzaDettaglioOggi() {
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        Proposta proposta = propostaBase();

        List<ValidationError> errori = validator.validaCampo(
                proposta,
                valori(oggi.plusDays(1), oggi.plusDays(4), oggi.plusDays(5), "2"),
                AppConstants.CAMPO_TERMINE_ISCRIZIONE,
                oggi.format(AppConstants.DATE_FMT));

        ProposalValidationFailure.SubscriptionDeadlineNotFuture failure = errori.stream()
                .map(ValidationError::failure)
                .filter(ProposalValidationFailure.SubscriptionDeadlineNotFuture.class::isInstance)
                .map(ProposalValidationFailure.SubscriptionDeadlineNotFuture.class::cast)
                .findFirst()
                .orElseThrow();

        assertNull(failure.today());
    }

    @Test
    void validaCampo_conTerminePresenteEDataEventoAssente_nonControllaDistanzaEvento() {
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        Proposta proposta = propostaBase();

        List<ValidationError> errori = validator.validaCampo(
                proposta,
                Map.of(),
                AppConstants.CAMPO_TERMINE_ISCRIZIONE,
                oggi.plusDays(2).format(AppConstants.DATE_FMT));

        assertNoFailure(errori, ProposalValidationFailure.EventDateTooEarly.class);
    }

    @Test
    void validaCampo_conDataOConclusivaCorrelate_restituisceErroriCoerenti() {
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        Proposta proposta = propostaBase();
        Map<String, String> valori = valori(oggi.plusDays(2), oggi.plusDays(5), oggi.plusDays(6), "2");

        List<ValidationError> erroreData = validator.validaCampo(
                proposta,
                valori,
                AppConstants.CAMPO_DATA,
                oggi.plusDays(3).format(AppConstants.DATE_FMT));
        List<ValidationError> erroreConclusiva = validator.validaCampo(
                proposta,
                valori,
                AppConstants.CAMPO_DATA_CONCLUSIVA,
                oggi.plusDays(4).format(AppConstants.DATE_FMT));

        assertAll(
                () -> assertFailure(erroreData, ProposalValidationFailure.EventDateTooEarly.class),
                () -> assertFailure(erroreConclusiva, ProposalValidationFailure.ClosingDateBeforeEvent.class)
        );
    }

    @Test
    void validaCampo_conDataEventoPresenteEDataConclusivaAssente_nonControllaConclusiva() {
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        Proposta proposta = propostaBase();

        List<ValidationError> errori = validator.validaCampo(
                proposta,
                Map.of(AppConstants.CAMPO_TERMINE_ISCRIZIONE, oggi.plusDays(2).format(AppConstants.DATE_FMT)),
                AppConstants.CAMPO_DATA,
                oggi.plusDays(5).format(AppConstants.DATE_FMT));

        assertNoFailure(errori, ProposalValidationFailure.ClosingDateBeforeEvent.class);
    }

    private Proposta propostaBase() {
        return new Proposta(
                new Categoria("Sport"),
                List.of(
                        campo(AppConstants.CAMPO_TITOLO, TipoDato.STRINGA, true),
                        campo(AppConstants.CAMPO_NUM_PARTECIPANTI, TipoDato.INTERO, true),
                        campo(AppConstants.CAMPO_TERMINE_ISCRIZIONE, TipoDato.DATA, true),
                        campo(AppConstants.CAMPO_DATA, TipoDato.DATA, true),
                        campo(AppConstants.CAMPO_DATA_CONCLUSIVA, TipoDato.DATA, false),
                        campo(AppConstants.CAMPO_ORA, TipoDato.ORA, false),
                        campo(AppConstants.CAMPO_QUOTA, TipoDato.DECIMALE, false),
                        campo("Booleano", TipoDato.BOOLEANO, false)
                ),
                List.of());
    }

    private Campo campo(String nome, TipoDato tipoDato, boolean obbligatorio) {
        return new Campo(nome, TipoCampo.BASE, tipoDato, obbligatorio);
    }

    private Map<String, String> valori(LocalDate termine,
                                       LocalDate data,
                                       LocalDate conclusiva,
                                       String partecipanti) {
        return Map.of(
                AppConstants.CAMPO_TITOLO, "Torneo",
                AppConstants.CAMPO_NUM_PARTECIPANTI, partecipanti,
                AppConstants.CAMPO_TERMINE_ISCRIZIONE, termine.format(AppConstants.DATE_FMT),
                AppConstants.CAMPO_DATA, data.format(AppConstants.DATE_FMT),
                AppConstants.CAMPO_DATA_CONCLUSIVA, conclusiva.format(AppConstants.DATE_FMT),
                AppConstants.CAMPO_ORA, "16:30",
                AppConstants.CAMPO_QUOTA, "10.50",
                "Booleano", "si"
        );
    }

    private void assertFailure(List<ValidationError> errori, Class<?> failureType) {
        assertTrue(errori.stream().anyMatch(e -> failureType.isInstance(e.failure())),
                "Manca failure " + failureType.getSimpleName() + " in " + errori);
    }

    private void assertNoFailure(List<ValidationError> errori, Class<?> failureType) {
        assertFalse(errori.stream().anyMatch(e -> failureType.isInstance(e.failure())),
                "Failure inattesa " + failureType.getSimpleName() + " in " + errori);
    }
}
