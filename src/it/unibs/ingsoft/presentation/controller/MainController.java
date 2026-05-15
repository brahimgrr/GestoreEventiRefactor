package it.unibs.ingsoft.presentation.controller;

import it.unibs.ingsoft.application.authentication.AuthenticationService;
import it.unibs.ingsoft.application.authentication.dto.CredenzialiRequest;
import it.unibs.ingsoft.domain.model.utente.Configuratore;
import it.unibs.ingsoft.domain.model.utente.Fruitore;
import it.unibs.ingsoft.presentation.view.interfaces.common.auth.IMainView;
import it.unibs.ingsoft.shared.error.FailureException;

import java.util.Objects;
import java.util.Optional;

public final class MainController {
    private final IMainView view;
    private final AuthenticationService auth;
    private final ConfiguratoreController configuratoreController;
    private final FruitoreController fruitoreController;

    public MainController(
            IMainView view,
            AuthenticationService auth,
            ConfiguratoreController configuratoreController,
            FruitoreController fruitoreController
    ) {
        this.view = Objects.requireNonNull(view);
        this.auth = Objects.requireNonNull(auth);
        this.configuratoreController = Objects.requireNonNull(configuratoreController);
        this.fruitoreController = Objects.requireNonNull(fruitoreController);
    }

    public void run() {
        view.mostraApplicazioneAvviata();
        while (true) {
            switch (view.scegliAzioneAccesso()) {
                case LOGIN_CONFIGURATORE -> avviaSessioneConfiguratore();
                case LOGIN_FRUITORE -> avviaSessioneFruitore();
                case REGISTRA_FRUITORE -> registraFruitore();
                case ESCI -> {
                    view.mostraUscita();
                    return;
                }
            }
        }
    }

    private void avviaSessioneConfiguratore() {
        Configuratore configuratore = loginConfiguratore();
        if (configuratore == null) {
            return;
        }
        configuratoreController.run();
        view.mostraLogoutConfiguratore(configuratore);
    }

    private void avviaSessioneFruitore() {
        Fruitore fruitore = loginFruitore();
        if (fruitore == null) {
            return;
        }
        fruitoreController.run(fruitore);
        view.mostraLogoutFruitore(fruitore);
    }

    public Configuratore loginConfiguratore() {
        while (true) {
            Optional<CredenzialiRequest> input = view.acquisisciLoginConfiguratore();
            if (input.isEmpty()) {
                return null;
            }

            Optional<Configuratore> result = auth.login(input.get().username(), input.get().password());
            if (result.isEmpty()) {
                view.mostraLoginFallito();
                continue;
            }

            Configuratore configuratore = result.get();
            view.mostraLoginRiuscitoConfiguratore(configuratore);
            if (!auth.isConfiguratorePredefinito(configuratore)) {
                return configuratore;
            }

            view.mostraPrimoAccessoConfiguratore();
            Configuratore registrato = registraConfiguratore();
            if (registrato != null) {
                return registrato;
            }
            view.mostraRegistrazioneAnnullata();
        }
    }

    public Fruitore loginFruitore() {
        while (true) {
            Optional<CredenzialiRequest> input = view.acquisisciLoginFruitore();
            if (input.isEmpty()) {
                return null;
            }

            Optional<Fruitore> result = auth.loginFruitore(input.get().username(), input.get().password());
            if (result.isPresent()) {
                view.mostraLoginRiuscitoFruitore(result.get());
                return result.get();
            }
            view.mostraLoginFallito();
        }
    }

    public Fruitore registraFruitore() {
        while (true) {
            Optional<CredenzialiRequest> input =
                    view.acquisisciRegistrazioneFruitore(auth::validaNuovoUsername, auth::validaNuovaPassword);
            if (input.isEmpty()) {
                view.mostraRegistrazioneAnnullata();
                return null;
            }

            try {
                Fruitore registered = auth.registraNuovoFruitore(input.get().username(), input.get().password());
                view.mostraRegistrazioneCompletata(registered.getUsername());
                return registered;
            } catch (FailureException e) {
                view.mostraErrore(e.failure());
            }
        }
    }

    private Configuratore registraConfiguratore() {
        while (true) {
            Optional<CredenzialiRequest> input =
                    view.acquisisciRegistrazioneConfiguratore(auth::validaNuovoUsername, auth::validaNuovaPassword);
            if (input.isEmpty()) {
                return null;
            }

            try {
                Configuratore registered = auth
                        .registraNuovoConfiguratore(input.get().username(), input.get().password());
                view.mostraRegistrazioneCompletata(registered.getUsername());
                return registered;
            } catch (FailureException e) {
                view.mostraErrore(e.failure());
            }
        }
    }
}
