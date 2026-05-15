package it.unibs.ingsoft.domain.error;

import it.unibs.ingsoft.application.batch.ImportFailure;
import it.unibs.ingsoft.application.batch.dto.ImportError;
import it.unibs.ingsoft.domain.model.catalogo.CatalogFailure;
import it.unibs.ingsoft.domain.model.proposta.ProposalValidationFailure;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ImportErrorTest {
    @Test
    void costruttore_conFailureValido_memorizzaFailure() {
        ImportFailure.CommonFieldDuplicated failure = new ImportFailure.CommonFieldDuplicated("Note");

        ImportError error = new ImportError(failure);

        assertSame(failure, error.failure());
    }

    @Test
    void costruttore_conFailureNull_lanciaNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ImportError(null));
    }

    @Test
    void domainError_conservaFailureDominioAnnidato() {
        ImportError error = new ImportError(
                new ImportFailure.CommonFieldDomainError(
                        "Note",
                        new CatalogFailure.FieldDuplicated("Note")));

        ImportFailure.CommonFieldDomainError failure =
                assertInstanceOf(ImportFailure.CommonFieldDomainError.class, error.failure());

        assertAll(
                () -> assertEquals("Note", failure.fieldName()),
                () -> assertInstanceOf(CatalogFailure.FieldDuplicated.class, failure.failure())
        );
    }

    @Test
    void validationError_conservaErroreValidazioneAnnidato() {
        ValidationError validationError = new ValidationError(
                "Data",
                new ProposalValidationFailure.EventDateTooEarly());
        ImportError error = new ImportError(new ImportFailure.ProposalValidation("Torneo", validationError));

        ImportFailure.ProposalValidation failure =
                assertInstanceOf(ImportFailure.ProposalValidation.class, error.failure());

        assertAll(
                () -> assertEquals("Torneo", failure.title()),
                () -> assertSame(validationError, failure.validationError())
        );
    }
}
