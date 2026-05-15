package it.unibs.ingsoft.presentation.view.cli.common.error;

import it.unibs.ingsoft.domain.model.catalogo.CatalogFailure;

public final class CatalogFailureCliMessages {
    private CatalogFailureCliMessages() {
    }

    public static void registerInto(FailureMessageRegistry registry) {
        registry
                .register(CatalogFailure.FieldNameInvalid.class, (failure, messages) ->
                        "Il nome del campo non puo essere vuoto.")
                .register(CatalogFailure.FieldTypeInvalid.class, (failure, messages) ->
                        "Il tipo del campo non puo essere null.")
                .register(CatalogFailure.FieldDataTypeInvalid.class, (failure, messages) ->
                        "Il tipo dato del campo non puo essere null.")
                .register(CatalogFailure.ExtraFieldDataInvalid.class, (failure, messages) ->
                        "I dati dei campi extra non possono essere null.")
                .register(CatalogFailure.ExtraFieldDimensionsMismatch.class, (failure, messages) ->
                        "Nomi e tipi dei campi extra devono avere la stessa dimensione.")
                .register(CatalogFailure.CategoryNameInvalid.class, (failure, messages) ->
                        "Il nome della categoria non puo essere vuoto.")
                .register(CatalogFailure.CategoryFieldNotSpecific.class, (failure, messages) ->
                        "Solo campi di tipo SPECIFICO possono essere aggiunti a una categoria.")
                .register(CatalogFailure.CategoryFieldDuplicated.class, (failure, messages) ->
                        "La categoria \"" + failure.categoryName() + "\" ha gia un campo chiamato \""
                                + failure.fieldName() + "\".")
                .register(CatalogFailure.BaseFieldsAlreadyFixed.class, (failure, messages) ->
                        "Campi base gia fissati.")
                .register(CatalogFailure.FieldDuplicated.class, (failure, messages) ->
                        failure.fieldName() == null || failure.fieldName().isBlank()
                                ? "Campo gia esistente."
                                : "Campo gia esistente: " + failure.fieldName())
                .register(CatalogFailure.NameDuplicated.class, (failure, messages) ->
                        "Duplicato: " + failure.name())
                .register(CatalogFailure.CategoryDuplicated.class, (failure, messages) ->
                        "Categoria gia esistente.")
                .register(CatalogFailure.CategoryNotFound.class, (failure, messages) ->
                        "Categoria non trovata.");
    }
}
