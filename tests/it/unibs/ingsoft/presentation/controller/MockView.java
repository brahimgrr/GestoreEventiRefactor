package it.unibs.ingsoft.presentation.controller;

import it.unibs.ingsoft.application.authentication.dto.CredenzialiRequest;
import it.unibs.ingsoft.domain.shared.error.Failure;
import it.unibs.ingsoft.domain.utente.Configuratore;
import it.unibs.ingsoft.domain.utente.Fruitore;
import it.unibs.ingsoft.presentation.view.interfaces.common.auth.CredentialFieldValidator;
import it.unibs.ingsoft.presentation.view.interfaces.common.auth.IMainView;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

final class MockView implements IMainView {
    final ArrayDeque<AccessAction> actions = new ArrayDeque<>();
    final ArrayDeque<Optional<CredenzialiRequest>> configuratoreLogins = new ArrayDeque<>();
    final ArrayDeque<Optional<CredenzialiRequest>> fruitoreLogins = new ArrayDeque<>();
    final ArrayDeque<Optional<CredenzialiRequest>> configuratoreRegistrations = new ArrayDeque<>();
    final ArrayDeque<Optional<CredenzialiRequest>> fruitoreRegistrations = new ArrayDeque<>();
    int appStarted;
    int loginFailed;
    int configuratoreLoginSuccess;
    int fruitoreLoginSuccess;
    int firstAccessShown;
    int registrationCancelled;
    int registrationCompleted;
    int configuratoreLogoutShown;
    int fruitoreLogoutShown;
    int exitShown;
    int errorsShown;

    MockView(AccessAction... actions) {
        this.actions.addAll(Arrays.asList(actions));
    }

    @Override
    public AccessAction scegliAzioneAccesso() {
        return actions.isEmpty() ? AccessAction.ESCI : actions.removeFirst();
    }

    @Override
    public Optional<CredenzialiRequest> acquisisciLoginConfiguratore() {
        return configuratoreLogins.isEmpty() ? Optional.empty() : configuratoreLogins.removeFirst();
    }

    @Override
    public Optional<CredenzialiRequest> acquisisciLoginFruitore() {
        return fruitoreLogins.isEmpty() ? Optional.empty() : fruitoreLogins.removeFirst();
    }

    @Override
    public Optional<CredenzialiRequest> acquisisciRegistrazioneConfiguratore(
            CredentialFieldValidator usernameValidator,
            CredentialFieldValidator passwordValidator) {
        return configuratoreRegistrations.isEmpty()
                ? Optional.empty()
                : configuratoreRegistrations.removeFirst();
    }

    @Override
    public Optional<CredenzialiRequest> acquisisciRegistrazioneFruitore(
            CredentialFieldValidator usernameValidator,
            CredentialFieldValidator passwordValidator) {
        return fruitoreRegistrations.isEmpty()
                ? Optional.empty()
                : fruitoreRegistrations.removeFirst();
    }

    @Override
    public void mostraApplicazioneAvviata() {
        appStarted++;
    }

    @Override
    public void mostraLoginFallito() {
        loginFailed++;
    }

    @Override
    public void mostraLoginRiuscitoConfiguratore(Configuratore configuratore) {
        Objects.requireNonNull(configuratore);
        configuratoreLoginSuccess++;
    }

    @Override
    public void mostraLoginRiuscitoFruitore(Fruitore fruitore) {
        Objects.requireNonNull(fruitore);
        fruitoreLoginSuccess++;
    }

    @Override
    public void mostraPrimoAccessoConfiguratore() {
        firstAccessShown++;
    }

    @Override
    public void mostraRegistrazioneAnnullata() {
        registrationCancelled++;
    }

    @Override
    public void mostraRegistrazioneCompletata(String username) {
        registrationCompleted += username == null ? 0 : 1;
    }

    @Override
    public void mostraLogoutConfiguratore(Configuratore configuratore) {
        Objects.requireNonNull(configuratore);
        configuratoreLogoutShown++;
    }

    @Override
    public void mostraLogoutFruitore(Fruitore fruitore) {
        Objects.requireNonNull(fruitore);
        fruitoreLogoutShown++;
    }

    @Override
    public void mostraUscita() {
        exitShown++;
    }

    @Override
    public void mostraErrore(Failure failure) {
        errorsShown += failure == null ? 0 : 1;
    }
}
