package it.unibs.ingsoft.application.batch;

import it.unibs.ingsoft.domain.catalogo.CatalogFailure;
import it.unibs.ingsoft.domain.proposta.ProposalValidationFailure;
import it.unibs.ingsoft.domain.shared.error.ApplicationFailure;
import it.unibs.ingsoft.domain.shared.error.ValidationError;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/*
Inutile testare interfaccia
*/
class ImportFailure_Test {
    @Test
    void tutteLeFailure_sonoApplicationFailure() {
        assertAll(
                () -> assertInstanceOf(ApplicationFailure.class, new ImportFailure.FileNotFound(Path.of("x"))),
                () -> assertInstanceOf(ApplicationFailure.class, new ImportFailure.FileNotReadable(Path.of("x"))),
                () -> assertInstanceOf(ApplicationFailure.class, new ImportFailure.InvalidJson(Path.of("x"))),
                () -> assertInstanceOf(ApplicationFailure.class, new ImportFailure.CommonFieldNameMissing()),
                () -> assertInstanceOf(ApplicationFailure.class, new ImportFailure.CommonFieldDuplicated("Note")),
                () -> assertInstanceOf(ApplicationFailure.class, new ImportFailure.CommonFieldTypeInvalid("Note", "X")),
                () -> assertInstanceOf(ApplicationFailure.class, new ImportFailure.CategoryNameMissing()),
                () -> assertInstanceOf(ApplicationFailure.class, new ImportFailure.CategoryDuplicated("Sport")),
                () -> assertInstanceOf(ApplicationFailure.class, new ImportFailure.SpecificFieldNameMissing("Sport")),
                () -> assertInstanceOf(ApplicationFailure.class, new ImportFailure.SpecificFieldTypeInvalid("A", "Sport", "X")),
                () -> assertInstanceOf(ApplicationFailure.class, new ImportFailure.ProposalCategoryMissing("Torneo")),
                () -> assertInstanceOf(ApplicationFailure.class, new ImportFailure.ProposalCategoryNotFound("Torneo", "Sport")),
                () -> assertInstanceOf(ApplicationFailure.class, new ImportFailure.ProposalDuplicatedInFile("Torneo"))
        );
    }

    @Test
    void failureConCampi_conservanoValori() {
        CatalogFailure.FieldDuplicated domainFailure = new CatalogFailure.FieldDuplicated("Note");
        ValidationError validationError = new ValidationError(
                "Data",
                new ProposalValidationFailure.EventDateTooEarly());

        assertAll(
                () -> assertEquals(Path.of("x"), new ImportFailure.FileNotFound(Path.of("x")).path()),
                () -> assertEquals("Note", new ImportFailure.CommonFieldDuplicated("Note").fieldName()),
                () -> assertSame(domainFailure,
                        new ImportFailure.CommonFieldDomainError("Note", domainFailure).failure()),
                () -> assertSame(domainFailure,
                        new ImportFailure.CategoryDomainError("Sport", domainFailure).failure()),
                () -> assertSame(domainFailure,
                        new ImportFailure.SpecificFieldDomainError("A", "Sport", domainFailure).failure()),
                () -> assertSame(domainFailure,
                        new ImportFailure.ProposalDomainError("Torneo", domainFailure).failure()),
                () -> assertSame(validationError,
                        new ImportFailure.ProposalValidation("Torneo", validationError).validationError())
        );
    }
}
