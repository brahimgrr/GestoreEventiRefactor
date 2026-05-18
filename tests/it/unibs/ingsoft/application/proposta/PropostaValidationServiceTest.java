package it.unibs.ingsoft.application.proposta;

import it.unibs.ingsoft.application.proposta.dto.PropostaValidationResult;
import it.unibs.ingsoft.domain.catalogo.Campo;
import it.unibs.ingsoft.domain.catalogo.Categoria;
import it.unibs.ingsoft.domain.catalogo.TipoCampo;
import it.unibs.ingsoft.domain.catalogo.TipoDato;
import it.unibs.ingsoft.domain.proposta.ProposalValidationFailure;
import it.unibs.ingsoft.domain.proposta.Proposta;
import it.unibs.ingsoft.domain.proposta.PropostaValidator;
import it.unibs.ingsoft.domain.shared.AppConstants;
import it.unibs.ingsoft.domain.shared.error.ValidationError;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PropostaValidationServiceTest {
    @Test
    void costruttore_conValidatorNull_lanciaNullPointerException() {
        assertThrows(NullPointerException.class, () -> new PropostaValidationService(null));
    }

    @Test
    void validaPropostaEValidaCampo_deleganoAlValidator() {
        PropostaValidationService service = new PropostaValidationService(new PropostaValidator());
        Proposta proposta = propostaBase();

        assertAll(
                () -> assertFalse(service.validaProposta(proposta).isEmpty()),
                () -> assertTrue(service.validaCampo(
                        proposta,
                        Map.of(),
                        AppConstants.CAMPO_QUOTA,
                        "dieci").stream().anyMatch(e -> e.fieldName().equals(AppConstants.CAMPO_QUOTA)))
        );
    }

    @Test
    void getCampiConErrore_restituisceSoloCampiConNomeAssociatoAgliErrori() {
        PropostaValidationService service = new PropostaValidationService();
        Proposta proposta = propostaBase();
        ValidationError titolo = new ValidationError(
                AppConstants.CAMPO_TITOLO,
                new ProposalValidationFailure.RequiredFieldMissing(AppConstants.CAMPO_TITOLO));
        ValidationError ignoto = new ValidationError(
                "Ignoto",
                new ProposalValidationFailure.RequiredFieldMissing("Ignoto"));

        List<Campo> campi = service.getCampiConErrore(proposta, List.of(titolo, ignoto));

        assertEquals(List.of(proposta.getCampi().get(0)), campi);
    }

    @Test
    void applicaValoriEValida_conValoriValidiOInvalidi_aggiornaStatoERisultato() {
        PropostaValidationService service = new PropostaValidationService();
        Proposta valida = propostaBase();
        Proposta invalida = propostaBase();

        PropostaValidationResult ok = service.applicaValoriEValida(valida, valoriValidi());
        PropostaValidationResult ko = service.applicaValoriEValida(invalida, Map.of(AppConstants.CAMPO_TITOLO, " "));

        assertAll(
                () -> assertTrue(ok.valida()),
                () -> assertTrue(ok.errori().isEmpty()),
                () -> assertTrue(ok.campiConErrore().isEmpty()),
                () -> assertFalse(ko.valida()),
                () -> assertFalse(ko.errori().isEmpty()),
                () -> assertFalse(ko.campiConErrore().isEmpty())
        );
    }

    private Proposta propostaBase() {
        return new Proposta(
                new Categoria("Sport"),
                List.of(
                        campo(AppConstants.CAMPO_TITOLO, TipoDato.STRINGA, true),
                        campo(AppConstants.CAMPO_NUM_PARTECIPANTI, TipoDato.INTERO, true),
                        campo(AppConstants.CAMPO_TERMINE_ISCRIZIONE, TipoDato.DATA, true),
                        campo(AppConstants.CAMPO_DATA, TipoDato.DATA, true),
                        campo(AppConstants.CAMPO_ORA, TipoDato.ORA, true),
                        campo(AppConstants.CAMPO_QUOTA, TipoDato.DECIMALE, true)
                ),
                List.of());
    }

    private Campo campo(String nome, TipoDato tipoDato, boolean obbligatorio) {
        return new Campo(nome, TipoCampo.BASE, tipoDato, obbligatorio);
    }

    private Map<String, String> valoriValidi() {
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        return Map.of(
                AppConstants.CAMPO_TITOLO, "Torneo",
                AppConstants.CAMPO_NUM_PARTECIPANTI, "2",
                AppConstants.CAMPO_TERMINE_ISCRIZIONE, oggi.plusDays(1).format(AppConstants.DATE_FMT),
                AppConstants.CAMPO_DATA, oggi.plusDays(4).format(AppConstants.DATE_FMT),
                AppConstants.CAMPO_ORA, "16:30",
                AppConstants.CAMPO_QUOTA, "10.50"
        );
    }
}
