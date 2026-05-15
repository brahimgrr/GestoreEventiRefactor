package it.unibs.ingsoft.domain.model.catalogo;

import it.unibs.ingsoft.domain.error.DomainFailure;

public sealed interface CatalogFailure extends DomainFailure {

    record FieldNameInvalid() implements CatalogFailure {
    }

    record FieldTypeInvalid() implements CatalogFailure {
    }

    record FieldDataTypeInvalid() implements CatalogFailure {
    }

    record ExtraFieldDataInvalid() implements CatalogFailure {
    }

    record ExtraFieldDimensionsMismatch() implements CatalogFailure {
    }

    record CategoryNameInvalid() implements CatalogFailure {
    }

    record CategoryFieldNotSpecific() implements CatalogFailure {
    }

    record CategoryFieldDuplicated(String categoryName, String fieldName) implements CatalogFailure {
    }

    record BaseFieldsAlreadyFixed() implements CatalogFailure {
    }

    record FieldDuplicated(String fieldName) implements CatalogFailure {
    }

    record NameDuplicated(String name) implements CatalogFailure {
    }

    record CategoryDuplicated(String categoryName) implements CatalogFailure {
    }

    record CategoryNotFound(String categoryName) implements CatalogFailure {
    }
}
