package it.unibs.ingsoft.application.batch.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Value object che contiene il risultato di un'operazione di importazione batch.
 * Traccia i conteggi di successo per entità e un elenco di messaggi di errore strutturati.
 */
public final class ImportResult {

    private final List<String> errori = new ArrayList<>();
    private int campiComuniImportati;
    private int categorieImportate;
    private int proposteImportate;

    public void incrementCampiComuni() {
        campiComuniImportati++;
    }

    public void incrementCategorie() {
        categorieImportate++;
    }

    public void incrementProposte() {
        proposteImportate++;
    }

    public void addErrore(String messaggio) {
        errori.add(messaggio);
    }

    public int getCampiComuniImportati() {
        return campiComuniImportati;
    }

    public int getCategorieImportate() {
        return categorieImportate;
    }

    public int getProposteImportate() {
        return proposteImportate;
    }

    public List<String> getErrori() {
        return Collections.unmodifiableList(errori);
    }

    public boolean hasErrors() {
        return !errori.isEmpty();
    }

    public int totaleImportati() {
        return campiComuniImportati + categorieImportate + proposteImportate;
    }
}
