package it.unibs.ingsoft.application.batch;

import it.unibs.ingsoft.application.error.ApplicationFailure;
import it.unibs.ingsoft.domain.error.DomainFailure;
import it.unibs.ingsoft.domain.error.ValidationError;

import java.nio.file.Path;

public sealed interface ImportFailure extends ApplicationFailure
        permits ImportFailure.FileNotFound,
        ImportFailure.FileNotReadable,
        ImportFailure.InvalidJson,
        ImportFailure.CommonFieldNameMissing,
        ImportFailure.CommonFieldDuplicated,
        ImportFailure.CommonFieldTypeInvalid,
        ImportFailure.CommonFieldDomainError,
        ImportFailure.CategoryNameMissing,
        ImportFailure.CategoryDuplicated,
        ImportFailure.CategoryDomainError,
        ImportFailure.SpecificFieldNameMissing,
        ImportFailure.SpecificFieldTypeInvalid,
        ImportFailure.SpecificFieldDomainError,
        ImportFailure.ProposalCategoryMissing,
        ImportFailure.ProposalCategoryNotFound,
        ImportFailure.ProposalDuplicatedInFile,
        ImportFailure.ProposalValidation,
        ImportFailure.ProposalDomainError {

    record FileNotFound(Path path) implements ImportFailure {
    }

    record FileNotReadable(Path path) implements ImportFailure {
    }

    record InvalidJson(Path path) implements ImportFailure {
    }

    record CommonFieldNameMissing() implements ImportFailure {
    }

    record CommonFieldDuplicated(String fieldName) implements ImportFailure {
    }

    record CommonFieldTypeInvalid(String fieldName, String rawType) implements ImportFailure {
    }

    record CommonFieldDomainError(String fieldName, DomainFailure failure) implements ImportFailure {
    }

    record CategoryNameMissing() implements ImportFailure {
    }

    record CategoryDuplicated(String categoryName) implements ImportFailure {
    }

    record CategoryDomainError(String categoryName, DomainFailure failure) implements ImportFailure {
    }

    record SpecificFieldNameMissing(String categoryName) implements ImportFailure {
    }

    record SpecificFieldTypeInvalid(String fieldName, String categoryName, String rawType) implements ImportFailure {
    }

    record SpecificFieldDomainError(String fieldName, String categoryName, DomainFailure failure) implements ImportFailure {
    }

    record ProposalCategoryMissing(String title) implements ImportFailure {
    }

    record ProposalCategoryNotFound(String title, String categoryName) implements ImportFailure {
    }

    record ProposalDuplicatedInFile(String title) implements ImportFailure {
    }

    record ProposalValidation(String title, ValidationError validationError) implements ImportFailure {
    }

    record ProposalDomainError(String title, DomainFailure failure) implements ImportFailure {
    }
}
