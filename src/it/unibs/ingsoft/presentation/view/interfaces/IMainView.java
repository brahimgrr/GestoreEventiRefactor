package it.unibs.ingsoft.presentation.view.interfaces;

import it.unibs.ingsoft.application.authentication.dto.CredenzialiRequest;
import it.unibs.ingsoft.domain.Configuratore;
import it.unibs.ingsoft.domain.Fruitore;

import java.util.Optional;

public interface IMainView {
    enum AccessAction {
        LOGIN_CONFIGURATORE,
        LOGIN_FRUITORE,
        REGISTRA_FRUITORE,
        ESCI
    }

    AccessAction scegliAzioneAccesso();

    Optional<CredenzialiRequest> acquisisciLoginConfiguratore();

    Optional<CredenzialiRequest> acquisisciLoginFruitore();

    Optional<CredenzialiRequest> acquisisciRegistrazioneConfiguratore(
            CredentialFieldValidator usernameValidator,
            CredentialFieldValidator passwordValidator);

    Optional<CredenzialiRequest> acquisisciRegistrazioneFruitore(
            CredentialFieldValidator usernameValidator,
            CredentialFieldValidator passwordValidator);

    void mostraApplicazioneAvviata();

    void mostraLoginFallito();

    void mostraLoginRiuscitoConfiguratore(Configuratore configuratore);

    void mostraLoginRiuscitoFruitore(Fruitore fruitore);

    void mostraPrimoAccessoConfiguratore();

    void mostraRegistrazioneAnnullata();

    void mostraRegistrazioneCompletata(String username);

    void mostraLogoutConfiguratore(Configuratore configuratore);

    void mostraLogoutFruitore(Fruitore fruitore);

    void mostraUscita();

    void mostraErrore(Exception e);
}
