package it.unibs.ingsoft.presentation.view.cli.configuratore.error;

import it.unibs.ingsoft.shared.error.Failure;
import it.unibs.ingsoft.presentation.view.cli.common.error.FailureMessageRegistry;
import it.unibs.ingsoft.presentation.view.interfaces.common.IAppView;
import it.unibs.ingsoft.presentation.view.interfaces.configuratore.error.IConfiguratoreFeedbackView;

public final class ConfiguratoreFeedbackView implements IConfiguratoreFeedbackView {
    private final IAppView ui;
    private final FailureMessageRegistry messages;

    public ConfiguratoreFeedbackView(IAppView ui) {
        this(ui, FailureMessageRegistry.cliDefault());
    }

    public ConfiguratoreFeedbackView(IAppView ui, FailureMessageRegistry messages) {
        this.ui = ui;
        this.messages = messages;
    }

    @Override
    public void mostraErrore(Failure failure) {
        ui.stampaErrore(messages.message(failure));
    }

    @Override
    public void mostraOperazioneAnnullata() {
        ui.stampaInfo("Operazione annullata.");
    }
}
