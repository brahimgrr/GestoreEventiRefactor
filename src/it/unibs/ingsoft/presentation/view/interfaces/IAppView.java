package it.unibs.ingsoft.presentation.view.interfaces;

/**
 * Interfaccia di view composita usata dai controller.
 * I componenti che necessitano solo di output dipendono da {@link IOutputView};
 * quelli che necessitano solo di input dipendono da {@link IInputView}.
 */
public interface IAppView extends IOutputView, IInputView {
    /**
     * Hint mostrato all'inizio di ogni form che accetta testo libero.
     */
    String HINT_ANNULLA = "Digita 'annulla' per annullare.";
}
