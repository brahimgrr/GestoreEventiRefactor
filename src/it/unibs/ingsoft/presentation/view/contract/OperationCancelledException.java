package it.unibs.ingsoft.presentation.view.contract;

/**
 * Lanciata quando l'utente annulla esplicitamente un'operazione in corso.
 * Questa eccezione risiede nel pacchetto del contratto di vista in modo che i controller
 * possano catturarla senza dipendere da alcuna implementazione concreta della vista (es. ConsoleUI).
 */
public class OperationCancelledException extends RuntimeException {
    public OperationCancelledException() {
        super("Operazione annullata dall'utente.");
    }
}
