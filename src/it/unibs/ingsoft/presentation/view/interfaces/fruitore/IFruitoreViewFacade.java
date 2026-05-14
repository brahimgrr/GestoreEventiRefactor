package it.unibs.ingsoft.presentation.view.interfaces.fruitore;

import it.unibs.ingsoft.presentation.view.interfaces.fruitore.bacheca.IBachecaView;
import it.unibs.ingsoft.presentation.view.interfaces.fruitore.notifica.ISpazioPersonaleView;
import it.unibs.ingsoft.presentation.view.interfaces.fruitore.proposta.IIscrizioneView;

public interface IFruitoreViewFacade extends
        it.unibs.ingsoft.presentation.view.interfaces.fruitore.menu.IFruitoreView,
        IBachecaView,
        IIscrizioneView,
        ISpazioPersonaleView {
}
