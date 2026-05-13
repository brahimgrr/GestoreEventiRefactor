package it.unibs.ingsoft.presentation.view.interfaces.configuratore.batch;

import it.unibs.ingsoft.application.batch.dto.ImportResult;

import java.nio.file.Path;
import java.util.Optional;

public interface IBatchImportView {
    Optional<Path> acquisisciPercorsoImportazione();

    void mostraRisultatoImportazione(ImportResult result);
}
