package it.unibs.ingsoft.application.proposta;

import it.unibs.ingsoft.application.proposta.dto.PropostaValidationResult;
import it.unibs.ingsoft.domain.shared.AppConstants;
import it.unibs.ingsoft.domain.catalogo.Campo;
import it.unibs.ingsoft.domain.catalogo.Categoria;
import it.unibs.ingsoft.domain.proposta.Proposta;
import it.unibs.ingsoft.domain.proposta.ProposalValidationFailure;
import it.unibs.ingsoft.domain.proposta.PropostaValidator;
import it.unibs.ingsoft.domain.proposta.StatoProposta;
import it.unibs.ingsoft.domain.catalogo.TipoCampo;
import it.unibs.ingsoft.domain.catalogo.TipoDato;
import it.unibs.ingsoft.domain.shared.error.ValidationError;
import it.unibs.ingsoft.domain.shared.validation.TypeValidationFailure;
import it.unibs.ingsoft.domain.catalogo.CampoFactory;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PropostaValidationServiceTest {
    private final PropostaValidationService service =
            new PropostaValidationService(new PropostaValidator());

    @Test
    void validValuesMarkProposalAsValidAndStoreParsedDates() {
        Proposta proposta = nuovaProposta();
        LocalDate oggi = LocalDate.now(AppConstants.clock);

        PropostaValidationResult result = service.applicaValoriEValida(proposta, valoriValidi(2));

        assertTrue(result.valida());
        assertTrue(proposta.isValida());
        assertEquals(oggi.plusDays(7), proposta.getTermineIscrizione());
        assertEquals(oggi.plusDays(10), proposta.getDataEvento());
    }

    @Test
    void invalidValuesReturnValidatedProposalToDraft() {
        Proposta proposta = nuovaProposta();
        service.applicaValoriEValida(proposta, valoriValidi(2));

        PropostaValidationResult result = service.applicaValoriEValida(
                proposta,
                Map.of(AppConstants.CAMPO_TITOLO, "")
        );

        assertFalse(result.valida());
        assertEquals(StatoProposta.BOZZA, proposta.getStato());
        assertTrue(result.errori().stream().anyMatch(error ->
                error.failure() instanceof ProposalValidationFailure.RequiredFieldMissing &&
                        AppConstants.CAMPO_TITOLO.equals(error.fieldName())));
    }

    @Test
    void singleFieldValidationAppliesDateRulesOnlyToAffectedFields() {
        Proposta proposta = nuovaProposta();
        String oggi = LocalDate.now(AppConstants.clock).format(AppConstants.DATE_FMT);

        List<ValidationError> errors = service.validaCampo(
                proposta,
                Map.of(),
                AppConstants.CAMPO_TERMINE_ISCRIZIONE,
                oggi
        );

        assertTrue(errors.stream().anyMatch(error ->
                error.failure() instanceof ProposalValidationFailure.SubscriptionDeadlineNotFuture &&
                        AppConstants.CAMPO_TERMINE_ISCRIZIONE.equals(error.fieldName())));
    }

    @Test
    void fullValidationReportsParticipantNumberErrors() {
        Proposta proposta = nuovaProposta();
        Map<String, String> valori = new LinkedHashMap<>(valoriValidi(2));
        valori.put(AppConstants.CAMPO_NUM_PARTECIPANTI, "-1");

        PropostaValidationResult result = service.applicaValoriEValida(proposta, valori);

        assertFalse(result.valida());
        assertTrue(result.errori().stream().anyMatch(error ->
                error.failure() instanceof ProposalValidationFailure.ParticipantsNotPositive &&
                        AppConstants.CAMPO_NUM_PARTECIPANTI.equals(error.fieldName())));
    }

    @Test
    void fullValidationReportsFieldTypeErrors() {
        Proposta proposta = nuovaPropostaConCampiComuni(List.of(
                new Campo("Numero tessera", TipoCampo.COMUNE, TipoDato.INTERO, false)
        ));
        Map<String, String> valori = new LinkedHashMap<>(valoriValidi(2));
        valori.put(AppConstants.CAMPO_DATA, "2026-05-20");
        valori.put(AppConstants.CAMPO_ORA, "20.30");
        valori.put(AppConstants.CAMPO_QUOTA, "dodici");
        valori.put("Numero tessera", "abc");

        PropostaValidationResult result = service.applicaValoriEValida(proposta, valori);

        assertFalse(result.valida());
        assertTrue(result.errori().stream().anyMatch(error ->
                error.failure() instanceof TypeValidationFailure.InvalidDate &&
                        AppConstants.CAMPO_DATA.equals(error.fieldName())));
        assertTrue(result.errori().stream().anyMatch(error ->
                error.failure() instanceof TypeValidationFailure.InvalidTime &&
                        AppConstants.CAMPO_ORA.equals(error.fieldName())));
        assertTrue(result.errori().stream().anyMatch(error ->
                error.failure() instanceof TypeValidationFailure.InvalidDecimal &&
                        AppConstants.CAMPO_QUOTA.equals(error.fieldName())));
        assertTrue(result.errori().stream().anyMatch(error ->
                error.failure() instanceof TypeValidationFailure.InvalidInteger &&
                        "Numero tessera".equals(error.fieldName())));
    }

    @Test
    void singleFieldValidationReportsTypeErrors() {
        Proposta proposta = nuovaProposta();

        List<ValidationError> errors = service.validaCampo(
                proposta,
                Map.of(),
                AppConstants.CAMPO_ORA,
                "20.30"
        );

        assertTrue(errors.stream().anyMatch(error ->
                error.failure() instanceof TypeValidationFailure.InvalidTime &&
                        AppConstants.CAMPO_ORA.equals(error.fieldName())));
    }

    @Test
    void singleFieldValidationReportsParticipantNumberErrors() {
        Proposta proposta = nuovaProposta();

        List<ValidationError> errors = service.validaCampo(
                proposta,
                Map.of(),
                AppConstants.CAMPO_NUM_PARTECIPANTI,
                "abc"
        );

        assertTrue(errors.stream().anyMatch(error ->
                error.failure() instanceof ProposalValidationFailure.ParticipantsNotInteger &&
                        AppConstants.CAMPO_NUM_PARTECIPANTI.equals(error.fieldName())));
    }

    private static Proposta nuovaProposta() {
        return nuovaPropostaConCampiComuni(List.of());
    }

    private static Proposta nuovaPropostaConCampiComuni(List<Campo> campiComuni) {
        CampoFactory campoFactory = CampoFactory.getInstance();
        List<Campo> campiBase = campoFactory.creaCampiBase();
        return new Proposta(new Categoria("Cinema"), campiBase, campiComuni);
    }

    private static Map<String, String> valoriValidi(int numeroPartecipanti) {
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        Map<String, String> valori = new LinkedHashMap<>();
        valori.put(AppConstants.CAMPO_TITOLO, "Rassegna");
        valori.put(AppConstants.CAMPO_TERMINE_ISCRIZIONE, oggi.plusDays(7).format(AppConstants.DATE_FMT));
        valori.put(AppConstants.CAMPO_DATA, oggi.plusDays(10).format(AppConstants.DATE_FMT));
        valori.put(AppConstants.CAMPO_DATA_CONCLUSIVA, oggi.plusDays(10).format(AppConstants.DATE_FMT));
        valori.put(AppConstants.CAMPO_ORA, "20:30");
        valori.put(AppConstants.CAMPO_LUOGO, "Brescia");
        valori.put(AppConstants.CAMPO_QUOTA, "12.50");
        valori.put(AppConstants.CAMPO_NUM_PARTECIPANTI, Integer.toString(numeroPartecipanti));
        return valori;
    }
}
