package it.unibs.ingsoft.presentation.view.cli.configuratore.error;

import it.unibs.ingsoft.presentation.view.cli.common.error.DomainErrorMessageMapper;
import it.unibs.ingsoft.presentation.view.interfaces.common.IAppView;
import it.unibs.ingsoft.presentation.view.interfaces.configuratore.error.IConfiguratoreFeedbackView;

public final class ConfiguratoreFeedbackView implements IConfiguratoreFeedbackView {
    private final IAppView ui;

    public ConfiguratoreFeedbackView(IAppView ui) {
        this.ui = ui;
    }

    @Override
    public void mostraErrore(Exception e) {
        ui.stampaErrore(DomainErrorMessageMapper.message(e));
    }

    @Override
    public void mostraOperazioneAnnullata() {
        ui.stampaInfo("Operazione annullata.");
    }
}
