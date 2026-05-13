package it.unibs.ingsoft.presentation.view.cli.common.error;

import it.unibs.ingsoft.application.batch.ImportFailure;

public final class ImportFailureCliMessages {
    private ImportFailureCliMessages() {
    }

    public static void registerInto(FailureMessageRegistry registry) {
        registry
                .register(ImportFailure.FileNotFound.class, (failure, messages) ->
                        "File non trovato: " + failure.path())
                .register(ImportFailure.FileNotReadable.class, (failure, messages) ->
                        "File non leggibile: " + failure.path())
                .register(ImportFailure.InvalidJson.class, (failure, messages) ->
                        "File JSON non valido: " + failure.path())
                .register(ImportFailure.CommonFieldNameMissing.class, (failure, messages) ->
                        "[Campo comune] nome vuoto o mancante - campo ignorato.")
                .register(ImportFailure.CommonFieldDuplicated.class, (failure, messages) ->
                        "[Campo comune] '" + failure.fieldName() + "': duplicato nel file di importazione.")
                .register(ImportFailure.CommonFieldTypeInvalid.class, (failure, messages) ->
                        "[Campo comune] '" + failure.fieldName() + "': tipoDato non valido: \""
                                + failure.rawType() + "\".")
                .register(ImportFailure.CommonFieldDomainError.class, (failure, messages) ->
                        "[Campo comune] '" + failure.fieldName() + "': " + messages.message(failure.failure()))
                .register(ImportFailure.CategoryNameMissing.class, (failure, messages) ->
                        "[Categoria] nome vuoto o mancante - categoria ignorata.")
                .register(ImportFailure.CategoryDuplicated.class, (failure, messages) ->
                        "[Categoria] '" + failure.categoryName() + "': duplicata nel file di importazione.")
                .register(ImportFailure.CategoryDomainError.class, (failure, messages) ->
                        "[Categoria] '" + failure.categoryName() + "': " + messages.message(failure.failure()))
                .register(ImportFailure.SpecificFieldNameMissing.class, (failure, messages) ->
                        "[Campo specifico] in categoria '" + failure.categoryName() + "': nome vuoto - campo ignorato.")
                .register(ImportFailure.SpecificFieldTypeInvalid.class, (failure, messages) ->
                        "[Campo specifico] '" + failure.fieldName() + "' in categoria '" + failure.categoryName()
                                + "': tipoDato non valido: \"" + failure.rawType() + "\".")
                .register(ImportFailure.SpecificFieldDomainError.class, (failure, messages) ->
                        "[Campo specifico] '" + failure.fieldName() + "' in categoria '" + failure.categoryName()
                                + "': " + messages.message(failure.failure()))
                .register(ImportFailure.ProposalCategoryMissing.class, (failure, messages) ->
                        proposalPrefix(failure.title()) + "nome categoria mancante.")
                .register(ImportFailure.ProposalCategoryNotFound.class, (failure, messages) ->
                        proposalPrefix(failure.title()) + "categoria '" + failure.categoryName()
                                + "' non trovata nel catalogo.")
                .register(ImportFailure.ProposalDuplicatedInFile.class, (failure, messages) ->
                        proposalPrefix(failure.title())
                                + "duplicata nel file di importazione (stesso Titolo, Data, Ora, Luogo).")
                .register(ImportFailure.ProposalValidation.class, (failure, messages) ->
                        proposalPrefix(failure.title()) + messages.message(failure.validationError().failure()))
                .register(ImportFailure.ProposalDomainError.class, (failure, messages) ->
                        proposalPrefix(failure.title()) + messages.message(failure.failure()));
    }

    private static String proposalPrefix(String title) {
        String displayTitle = title == null || title.isBlank() ? "(senza titolo)" : title;
        return "[Proposta] '" + displayTitle + "': ";
    }
}
