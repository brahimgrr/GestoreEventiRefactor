package it.unibs.ingsoft.presentation.view.interfaces.common;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Sotto-interfaccia ISP per le sole operazioni di input generiche.
 */
public interface IInputView {
    /**
     * Legge una stringa dall'utente. Le implementazioni devono rilevare la keyword
     * di annullamento ("annulla") e lanciare {@link OperationCancelledException}.
     */
    String acquisisciStringa(String prompt);

    /**
     * Ripete il prompt finche il predicato non e soddisfatto; mostra {@code errorMsg}
     * in caso di fallimento.
     */
    String acquisisciStringaConValidazione(String prompt, Predicate<String> validatore, String errorMsg);

    String acquisisciPassword(String prompt);

    int acquisisciIntero(String prompt, int min, int max);

    boolean acquisisciSiNo(String prompt);

    /**
     * Raccoglie interattivamente una lista di nomi con rilevamento inline dei duplicati
     * e un passo di revisione/conferma prima di restituire.
     */
    List<String> acquisisciListaNomi(String titolo);

    /**
     * Mostra una lista numerata usando il mapper fornito per l'etichetta di ogni elemento.
     */
    <T> Optional<T> selezionaElemento(String prompt, List<T> elementi,
                                      Function<T, String> labelMapper);

}
