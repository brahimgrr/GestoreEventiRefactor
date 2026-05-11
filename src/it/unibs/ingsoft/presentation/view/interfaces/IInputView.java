package it.unibs.ingsoft.presentation.view.interfaces;

import it.unibs.ingsoft.domain.catalogo.Categoria;
import it.unibs.ingsoft.domain.proposta.Proposta;
import it.unibs.ingsoft.domain.catalogo.TipoDato;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Sotto-interfaccia ISP per le sole operazioni di input.
 * Le implementazioni che acquisiscono solo l'input utente (es. driver di test scriptato)
 * dipendono esclusivamente da questa interfaccia ristretta.
 */
public interface IInputView {
    /**
     * Legge una stringa dall'utente. Le implementazioni devono rilevare la keyword
     * di annullamento ("annulla") e lanciare {@link OperationCancelledException}.
     */
    String acquisisciStringa(String prompt);

    /**
     * Ripete il prompt finché il predicato non è soddisfatto; mostra {@code errorMsg} in caso di fallimento.
     */
    String acquisisciStringaConValidazione(String prompt, Predicate<String> validatore, String errorMsg);

    String acquisisciPassword(String prompt);

    int acquisisciIntero(String prompt, int min, int max);

    boolean acquisisciSiNo(String prompt);

    TipoDato acquisisciTipoDato(String prompt);

    /**
     * Raccoglie interattivamente una lista di nomi con rilevamento inline dei duplicati
     * e un passo di revisione/conferma prima di restituire.
     */
    List<String> acquisisciListaNomi(String titolo);

    /**
     * Mostra una lista numerata e restituisce l'elemento selezionato,
     * o {@link Optional#empty()} se l'utente sceglie 0 (Annulla).
     */
    <T> Optional<T> selezionaElemento(String prompt, List<T> elementi);

    /**
     * Come {@link #selezionaElemento} ma aggiunge informazioni extra per ogni elemento
     * (es. "[obbligatorio]" o "[facoltativo]").
     */
    <T> Optional<T> selezionaElementoConInfo(String prompt, List<T> elementi,
                                             Function<T, String> infoMapper);

    /**
     * Mostra una lista numerata di categorie e restituisce l'indice 0-based della selezionata,
     * o empty se l'utente annulla.
     */
    OptionalInt selezionaCategoria(List<Categoria> categorie);

    /**
     * Esegue il form completo della proposta; restituisce i valori inseriti o empty se annullato.
     */
    Optional<Map<String, String>> acquisisciValoriProposta(Proposta proposta, ProposalFieldValidator validator);

    /**
     * Esegue un form di correzione limitato ai campi specificati;
     * restituisce i valori corretti o empty se annullato.
     */
    Optional<Map<String, String>> correggiCampiProposta(Proposta proposta, Set<String> nomiCampi, ProposalFieldValidator validator);
}
