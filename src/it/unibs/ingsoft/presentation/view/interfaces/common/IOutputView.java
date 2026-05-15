package it.unibs.ingsoft.presentation.view.interfaces.common;

/**
 * Sotto-interfaccia ISP per le sole operazioni di output generiche.
 */
public interface IOutputView {
    void stampa(String testo);

    void newLine();

    void header(String titolo);

    void stampaSezione(String titolo);

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
     * Stampa una riga vuota e attende INVIO.
     */
    default void pausaConSpaziatura() {
        newLine();
        pausa();
    }
}
