package it.unibs.ingsoft.presentation.view.cli;

import it.unibs.ingsoft.domain.error.DomainErrorCode;
import it.unibs.ingsoft.domain.error.ImportError;

public final class ImportErrorMessageMapper {
    private ImportErrorMessageMapper() {
    }

    public static String message(ImportError error) {
        return switch (error.code()) {
            case IMPORT_CAMPO_COMUNE_NOME_MANCANTE ->
                    "[Campo comune] nome vuoto o mancante - campo ignorato.";
            case IMPORT_CAMPO_COMUNE_DUPLICATO ->
                    "[Campo comune] '" + error.detail(0) + "': duplicato nel file di importazione.";
            case IMPORT_CAMPO_COMUNE_TIPO_DATO_INVALIDO ->
                    "[Campo comune] '" + error.detail(0) + "': tipoDato non valido: \""
                            + error.detail(1) + "\".";
            case IMPORT_CAMPO_COMUNE_ERRORE_DOMINIO ->
                    "[Campo comune] '" + error.detail(0) + "': "
                            + DomainErrorMessageMapper.message(error.domainException());
            case IMPORT_CATEGORIA_NOME_MANCANTE ->
                    "[Categoria] nome vuoto o mancante - categoria ignorata.";
            case IMPORT_CATEGORIA_DUPLICATA ->
                    "[Categoria] '" + error.detail(0) + "': duplicata nel file di importazione.";
            case IMPORT_CATEGORIA_ERRORE_DOMINIO ->
                    "[Categoria] '" + error.detail(0) + "': "
                            + DomainErrorMessageMapper.message(error.domainException());
            case IMPORT_CAMPO_SPECIFICO_NOME_MANCANTE ->
                    "[Campo specifico] in categoria '" + error.detail(0) + "': nome vuoto - campo ignorato.";
            case IMPORT_CAMPO_SPECIFICO_TIPO_DATO_INVALIDO ->
                    "[Campo specifico] '" + error.detail(0) + "' in categoria '" + error.detail(1)
                            + "': tipoDato non valido: \"" + error.detail(2) + "\".";
            case IMPORT_CAMPO_SPECIFICO_ERRORE_DOMINIO ->
                    "[Campo specifico] '" + error.detail(0) + "' in categoria '" + error.detail(1)
                            + "': " + DomainErrorMessageMapper.message(error.domainException());
            case IMPORT_PROPOSTA_CATEGORIA_MANCANTE ->
                    propostaPrefix(error) + "nome categoria mancante.";
            case IMPORT_PROPOSTA_CATEGORIA_NON_TROVATA ->
                    propostaPrefix(error) + "categoria '" + error.detail(1) + "' non trovata nel catalogo.";
            case IMPORT_PROPOSTA_DUPLICATA_FILE ->
                    propostaPrefix(error)
                            + "duplicata nel file di importazione (stesso Titolo, Data, Ora, Luogo).";
            case IMPORT_PROPOSTA_CAMPO_TIPO_NON_VALIDO ->
                    propostaPrefix(error) + "campo \"" + error.detail(1) + "\": "
                            + ValidationErrorMessageMapper.message(error.validationError());
            case IMPORT_PROPOSTA_VALIDAZIONE ->
                    propostaPrefix(error) + ValidationErrorMessageMapper.message(error.validationError());
            case IMPORT_PROPOSTA_ERRORE_DOMINIO ->
                    propostaPrefix(error) + DomainErrorMessageMapper.message(error.domainException());
            default -> fallback(error);
        };
    }

    private static String propostaPrefix(ImportError error) {
        String titolo = error.detail(0);
        if (titolo == null || titolo.isBlank()) {
            titolo = "(senza titolo)";
        }
        return "[Proposta] '" + titolo + "': ";
    }

    private static String fallback(ImportError error) {
        DomainErrorCode code = error.code();
        return code == null ? "" : DomainErrorMessageMapper.message(code, error.details());
    }
}
