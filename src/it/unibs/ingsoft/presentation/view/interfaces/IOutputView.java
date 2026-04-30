package it.unibs.ingsoft.presentation.view.interfaces;

import it.unibs.ingsoft.domain.Campo;
import it.unibs.ingsoft.domain.Categoria;
import it.unibs.ingsoft.domain.Proposta;
import it.unibs.ingsoft.domain.PropostaStateChange;

import java.util.List;
import java.util.Map;

/**
 * Sotto-interfaccia ISP per le sole operazioni di output.
 * Le implementazioni che producono solo output (es. logger, spy da test)
 * dipendono esclusivamente da questa interfaccia ristretta.
 */
public interface IOutputView {
    void stampa(String testo);

    void newLine();

    void header(String titolo);

    void stampaSezione(String titolo);

    void stampaCampi(List<Campo> campi);

    void stampaCategorie(List<Categoria> categorie);

    /**
     * Mostra le categorie con i relativi campi specifici elencati sotto ciascuna.
     */
    void stampaCategorieDettaglio(Map<String, List<String>> categorieConCampi);

    /**
     * Mostra un menu numerato; {@code 0} esce/torna con l'etichetta "Esci".
     */
    void stampaMenu(String titolo, String[] voci);

    /**
     * Mostra un menu numerato; {@code 0} esce/torna con l'etichetta {@code uscitaLabel}.
     */
    void stampaMenu(String titolo, String[] voci, String uscitaLabel);

    void pausa();

    void stampaSuccesso(String msg);

    void stampaErrore(String msg);

    void stampaAvviso(String msg);

    void stampaInfo(String msg);

    /**
     * Mostra la bacheca organizzata per categoria.
     */
    void mostraBacheca(Map<String, List<Proposta>> bacheca);

    /**
     * Mostra il riepilogo di una singola proposta.
     */
    void mostraRiepilogoProposta(Proposta proposta);

    /**
     * Mostra la lista degli aderenti di una proposta.
     */
    void mostraAderenti(List<String> aderenti);

    /**
     * Mostra la cronologia dei cambi di stato di una proposta.
     */
    void mostraCronologiaStati(List<PropostaStateChange> history);

    /**
     * Stampa una riga vuota e attende INVIO — comodo pattern di fine azione.
     */
    default void pausaConSpaziatura() {
        newLine();
        pausa();
    }
}
