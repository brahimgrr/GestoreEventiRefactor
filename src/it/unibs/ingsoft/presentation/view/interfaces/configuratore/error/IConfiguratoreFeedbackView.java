package it.unibs.ingsoft.presentation.view.interfaces.configuratore.error;

import it.unibs.ingsoft.shared.error.Failure;

public interface IConfiguratoreFeedbackView {
    void mostraErrore(Failure failure);

    void mostraOperazioneAnnullata();
}
