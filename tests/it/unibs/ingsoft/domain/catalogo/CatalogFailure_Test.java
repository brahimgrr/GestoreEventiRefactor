package it.unibs.ingsoft.domain.catalogo;

import it.unibs.ingsoft.domain.shared.error.DomainFailure;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/*
è un interfaccia. Ha senso testarla? secondo me ha piu senso solo testare le classi figlie concrete.
 */
class CatalogFailure_Test {
    @Test
    void failureSenzaCampi_sonoDomainFailure() {
        assertAll(
                () -> assertInstanceOf(DomainFailure.class, new CatalogFailure.FieldNameInvalid()),
                () -> assertInstanceOf(DomainFailure.class, new CatalogFailure.FieldTypeInvalid()),
                () -> assertInstanceOf(DomainFailure.class, new CatalogFailure.FieldDataTypeInvalid()),
                () -> assertInstanceOf(DomainFailure.class, new CatalogFailure.ExtraFieldDataInvalid()),
                () -> assertInstanceOf(DomainFailure.class, new CatalogFailure.ExtraFieldDimensionsMismatch()),
                () -> assertInstanceOf(DomainFailure.class, new CatalogFailure.CategoryNameInvalid()),
                () -> assertInstanceOf(DomainFailure.class, new CatalogFailure.CategoryFieldNotSpecific()),
                () -> assertInstanceOf(DomainFailure.class, new CatalogFailure.BaseFieldsAlreadyFixed())
        );
    }

    @Test
    void categoryFieldDuplicated_conservaCategoryNameEFieldName() {
        CatalogFailure.CategoryFieldDuplicated failure =
                new CatalogFailure.CategoryFieldDuplicated("Sport", "Arbitro");

        assertAll(
                () -> assertEquals("Sport", failure.categoryName()),
                () -> assertEquals("Arbitro", failure.fieldName())
        );
    }

    @Test
    void fieldDuplicated_conservaFieldName() {
        CatalogFailure.FieldDuplicated failure = new CatalogFailure.FieldDuplicated("Note");

        assertEquals("Note", failure.fieldName());
    }

    @Test
    void nameDuplicated_conservaName() {
        CatalogFailure.NameDuplicated failure = new CatalogFailure.NameDuplicated("Titolo");

        assertEquals("Titolo", failure.name());
    }

    @Test
    void categoryDuplicated_conservaCategoryName() {
        CatalogFailure.CategoryDuplicated failure = new CatalogFailure.CategoryDuplicated("Sport");

        assertEquals("Sport", failure.categoryName());
    }

    @Test
    void categoryNotFound_conservaCategoryName() {
        CatalogFailure.CategoryNotFound failure = new CatalogFailure.CategoryNotFound("Sport");

        assertEquals("Sport", failure.categoryName());
    }

    @Test
    void recordEqualityHashCodeEToString_siComportanoComeValueObject() {
        CatalogFailure.FieldDuplicated first = new CatalogFailure.FieldDuplicated("Note");
        CatalogFailure.FieldDuplicated second = new CatalogFailure.FieldDuplicated("Note");

        assertAll(
                () -> assertEquals(first, second),
                () -> assertEquals(first.hashCode(), second.hashCode()),
                () -> assertTrue(first.toString().contains("Note"))
        );
    }
}
