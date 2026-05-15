package it.unibs.ingsoft.domain;

import it.unibs.ingsoft.domain.error.ValidationError;
import it.unibs.ingsoft.domain.model.catalogo.Campo;
import it.unibs.ingsoft.domain.model.catalogo.Categoria;
import it.unibs.ingsoft.domain.model.catalogo.TipoCampo;
import it.unibs.ingsoft.domain.model.catalogo.TipoDato;
import it.unibs.ingsoft.domain.policy.proposta.PropostaValidationFailure;
import it.unibs.ingsoft.domain.model.proposta.Proposta;
import it.unibs.ingsoft.domain.policy.proposta.rules.CampiObbligatoriRule;
import it.unibs.ingsoft.domain.policy.proposta.rules.DataConclusivaDopoEventoRule;
import it.unibs.ingsoft.domain.policy.proposta.rules.DataEventoDopoTermineIscrizioneRule;
import it.unibs.ingsoft.domain.policy.proposta.PropostaValidationRuleFactory;
import it.unibs.ingsoft.domain.policy.proposta.PropostaValidationRule;
import it.unibs.ingsoft.domain.policy.proposta.PropostaValidator;
import it.unibs.ingsoft.domain.policy.proposta.rules.TermineIscrizioneFuturoRule;
import it.unibs.ingsoft.domain.policy.proposta.rules.TipoDatoCampoRule;
import it.unibs.ingsoft.domain.policy.tipodato.TipoDatoValidationStrategyRegistry;
import it.unibs.ingsoft.domain.policy.tipodato.TipoDatoValidator;
import it.unibs.ingsoft.domain.policy.tipodato.TypeValidationFailure;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PropostaValidatorTest {
    @Test
    void valida_conCampiObbligatoriMancanti_restituisceRequiredFieldMissing() {
        Proposta proposta = propostaBaseCompleta();

        List<ValidationError> errori = PropostaValidator.standard().valida(proposta).errori();

        assertTrue(errori.stream()
                .anyMatch(e -> e.fieldName().equals(AppConstants.CAMPO_TITOLO)
                        && e.failure() instanceof PropostaValidationFailure.RequiredFieldMissing));
    }

    @Test
    void valida_conTipoCampoGenericoNonValido_delegaAlTipoDatoValidator() {
        Proposta proposta = propostaBaseCompleta();
        proposta.aggiornaValoriCampi(valoriValidi("Torneo", "2", "non-decimale"));

        List<ValidationError> errori = PropostaValidator.standard().valida(proposta).errori();

        assertTrue(errori.stream()
                .anyMatch(e -> e.fieldName().equals(AppConstants.CAMPO_QUOTA)
                        && e.failure() instanceof TypeValidationFailure.InvalidDecimal));
    }

    @Test
    void valida_conNumeroPartecipantiNonValido_usaTipoDatoInteroPositivo() {
        Proposta proposta = propostaBaseCompleta();
        proposta.aggiornaValoriCampi(valoriValidi("Torneo", "zero", "10.50"));

        List<ValidationError> errori = PropostaValidator.standard().valida(proposta).errori();

        assertTrue(errori.stream()
                .anyMatch(e -> e.fieldName().equals(AppConstants.CAMPO_NUM_PARTECIPANTI)
                        && e.failure() instanceof TypeValidationFailure.InvalidInteger));
    }

    @Test
    void valida_conNumeroPartecipantiNonPositivo_usaTipoDatoInteroPositivo() {
        Proposta proposta = propostaBaseCompleta();
        proposta.aggiornaValoriCampi(valoriValidi("Torneo", "0", "10.50"));

        List<ValidationError> errori = PropostaValidator.standard().valida(proposta).errori();

        assertTrue(errori.stream()
                .anyMatch(e -> e.fieldName().equals(AppConstants.CAMPO_NUM_PARTECIPANTI)
                        && e.failure() instanceof TypeValidationFailure.InvalidPositiveInteger));
    }

    @Test
    void valida_conDateNonCoerenti_restituisceLeFailureDiRelazione() {
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        Proposta proposta = propostaBaseCompleta();
        proposta.aggiornaValoriCampi(valoriConDate(
                "Torneo",
                "2",
                oggi,
                oggi.plusDays(1),
                oggi.minusDays(1)));

        List<ValidationError> errori = PropostaValidator.standard().valida(proposta).errori();

        assertAll(
                () -> assertTrue(errori.stream()
                        .anyMatch(e -> e.failure() instanceof PropostaValidationFailure.SubscriptionDeadlineNotFuture)),
                () -> assertTrue(errori.stream()
                        .anyMatch(e -> e.failure() instanceof PropostaValidationFailure.EventDateTooEarly)),
                () -> assertTrue(errori.stream()
                        .anyMatch(e -> e.failure() instanceof PropostaValidationFailure.ClosingDateBeforeEvent))
        );
    }

    @Test
    void validaCampo_esegueSoloLeRegoleCollegateAlCampoModificato() {
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        Proposta proposta = propostaBaseCompleta();
        Map<String, String> valori = valoriConDate(
                "Torneo",
                "2",
                oggi.plusDays(3),
                oggi.plusDays(6),
                oggi.plusDays(7));

        List<ValidationError> errori = PropostaValidator.standard().validaCampo(
                proposta,
                valori,
                AppConstants.CAMPO_DATA,
                oggi.plusDays(4).format(AppConstants.DATE_FMT));

        assertAll(
                () -> assertTrue(errori.stream()
                        .anyMatch(e -> e.failure() instanceof PropostaValidationFailure.EventDateTooEarly)),
                () -> assertTrue(errori.stream()
                        .noneMatch(e -> e.failure() instanceof PropostaValidationFailure.SubscriptionDeadlineNotFuture))
        );
    }

    @Test
    void valida_conRegolaCustomPermetteEstensioneSenzaModificareIlValidator() {
        PropostaValidationRule customRule = (context, errors) -> errors.add(new ValidationError(
                "Custom",
                new PropostaValidationFailure.RequiredFieldMissing("Custom")));
        Proposta proposta = propostaBaseCompleta();

        List<ValidationError> errori = new PropostaValidator(List.of(customRule)).valida(proposta).errori();

        assertEquals("Custom", errori.get(0).fieldName());
    }

    @Test
    void valida_conStrategiaTipoDatoCustomPermetteEstensioneSenzaModificareTipoDatoCampoRule() {
        TipoDatoValidationStrategyRegistry registry = TipoDatoValidationStrategyRegistry.of(Map.of(
                TipoDato.STRINGA,
                value -> Optional.of(new ValidationError(null, new TypeValidationFailure.InvalidBoolean()))
        ));
        TipoDatoCampoRule tipoDatoCampoRule = new TipoDatoCampoRule(
                new TipoDatoValidator(registry));
        Proposta proposta = new Proposta(
                new Categoria("Sport"),
                List.of(campo("Codice", TipoCampo.BASE, TipoDato.STRINGA, false)),
                List.of());
        proposta.aggiornaValoriCampi(Map.of("Codice", "ABC"));

        List<ValidationError> errori = new PropostaValidator(List.of(tipoDatoCampoRule)).valida(proposta).errori();

        assertAll(
                () -> assertEquals("Codice", errori.get(0).fieldName()),
                () -> assertInstanceOf(TypeValidationFailure.InvalidBoolean.class, errori.get(0).failure())
        );
    }

    @Test
    void factoryStandard_creaLaChainDiValidazioneNellOrdineDiDominio() {
        List<? extends Class<?>> ruleTypes = PropostaValidationRuleFactory
                .getRules(TipoDatoValidator.INSTANCE)
                .stream()
                .map(Object::getClass)
                .toList();

        assertIterableEquals(List.of(
                CampiObbligatoriRule.class,
                TipoDatoCampoRule.class,
                TermineIscrizioneFuturoRule.class,
                DataEventoDopoTermineIscrizioneRule.class,
                DataConclusivaDopoEventoRule.class
        ), ruleTypes);
    }

    private Proposta propostaBaseCompleta() {
        return new Proposta(new Categoria("Sport"), campiBaseMinimiCompleti(), List.of());
    }

    private List<Campo> campiBaseMinimiCompleti() {
        return List.of(
                campo(AppConstants.CAMPO_TITOLO, TipoCampo.BASE, TipoDato.STRINGA, true),
                campo(AppConstants.CAMPO_NUM_PARTECIPANTI, TipoCampo.BASE, TipoDato.INTERO_POSITIVO, true),
                campo(AppConstants.CAMPO_TERMINE_ISCRIZIONE, TipoCampo.BASE, TipoDato.DATA, true),
                campo(AppConstants.CAMPO_LUOGO, TipoCampo.BASE, TipoDato.STRINGA, true),
                campo(AppConstants.CAMPO_DATA, TipoCampo.BASE, TipoDato.DATA, true),
                campo(AppConstants.CAMPO_ORA, TipoCampo.BASE, TipoDato.ORA, true),
                campo(AppConstants.CAMPO_QUOTA, TipoCampo.BASE, TipoDato.DECIMALE, true),
                campo(AppConstants.CAMPO_DATA_CONCLUSIVA, TipoCampo.BASE, TipoDato.DATA, false));
    }

    private Map<String, String> valoriValidi(String titolo, String numeroPartecipanti, String quota) {
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        return valoriConDate(titolo, numeroPartecipanti, oggi.plusDays(1), oggi.plusDays(4), oggi.plusDays(5), quota);
    }

    private Map<String, String> valoriConDate(String titolo,
                                              String numeroPartecipanti,
                                              LocalDate termine,
                                              LocalDate dataEvento,
                                              LocalDate dataConclusiva) {
        return valoriConDate(titolo, numeroPartecipanti, termine, dataEvento, dataConclusiva, "10.50");
    }

    private Map<String, String> valoriConDate(String titolo,
                                              String numeroPartecipanti,
                                              LocalDate termine,
                                              LocalDate dataEvento,
                                              LocalDate dataConclusiva,
                                              String quota) {
        return Map.of(
                AppConstants.CAMPO_TITOLO, titolo,
                AppConstants.CAMPO_NUM_PARTECIPANTI, numeroPartecipanti,
                AppConstants.CAMPO_TERMINE_ISCRIZIONE, termine.format(AppConstants.DATE_FMT),
                AppConstants.CAMPO_LUOGO, "Brescia",
                AppConstants.CAMPO_DATA, dataEvento.format(AppConstants.DATE_FMT),
                AppConstants.CAMPO_ORA, "16:30",
                AppConstants.CAMPO_QUOTA, quota,
                AppConstants.CAMPO_DATA_CONCLUSIVA, dataConclusiva.format(AppConstants.DATE_FMT)
        );
    }

    private Campo campo(String nome, TipoCampo tipo, TipoDato tipoDato, boolean obbligatorio) {
        return new Campo(nome, tipo, tipoDato, obbligatorio);
    }
}
