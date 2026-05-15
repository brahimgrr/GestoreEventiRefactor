package it.unibs.ingsoft.presentation.view.cli.common.menu;

import it.unibs.ingsoft.application.authentication.dto.CredenzialiRequest;
import it.unibs.ingsoft.domain.model.utente.Configuratore;
import it.unibs.ingsoft.domain.model.utente.Fruitore;
import it.unibs.ingsoft.presentation.view.cli.common.error.FailureMessageRegistry;
import it.unibs.ingsoft.presentation.view.interfaces.common.IAppView;
import it.unibs.ingsoft.presentation.view.interfaces.common.OperationCancelledException;
import it.unibs.ingsoft.presentation.view.interfaces.common.auth.CredentialFieldValidator;
import it.unibs.ingsoft.presentation.view.interfaces.common.auth.IMainView;
import it.unibs.ingsoft.shared.error.Failure;

import java.util.Optional;

public final class MainCliView implements IMainView {
    private static final String[] MENU_ACCESSO = {
            "Accedi come Configuratore",
            "Accedi come Fruitore",
            "Registrati come Fruitore"
    };

    private final IAppView ui;
    private final FailureMessageRegistry messages;

    public MainCliView(IAppView ui) {
        this(ui, FailureMessageRegistry.cliDefault());
    }

    public MainCliView(IAppView ui, FailureMessageRegistry messages) {
        this.ui = ui;
        this.messages = messages;
    }

    @Override
    public AccessAction scegliAzioneAccesso() {
        ui.stampaMenu("MENU DI ACCESSO", MENU_ACCESSO, "Esci dall'applicazione");
        int choice = ui.acquisisciIntero("Scelta: ", 0, MENU_ACCESSO.length);
        return choice == 0 ? AccessAction.ESCI : AccessAction.values()[choice - 1];
    }

    @Override
    public Optional<CredenzialiRequest> acquisisciLoginConfiguratore() {
        return acquisisciLogin("LOGIN CONFIGURATORE");
    }

    @Override
    public Optional<CredenzialiRequest> acquisisciLoginFruitore() {
        return acquisisciLogin("LOGIN FRUITORE");
    }

    private Optional<CredenzialiRequest> acquisisciLogin(String titolo) {
        ui.newLine();
        ui.stampa(titolo);
        try {
            String username = ui.acquisisciStringa("Username: ");
            String password = ui.acquisisciStringa("Password: ");
            return Optional.of(new CredenzialiRequest(username, password));
        } catch (OperationCancelledException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<CredenzialiRequest> acquisisciRegistrazioneConfiguratore(
            CredentialFieldValidator usernameValidator,
            CredentialFieldValidator passwordValidator) {
        return acquisisciRegistrazione(
                "REGISTRAZIONE CONFIGURATORE",
                true,
                usernameValidator,
                passwordValidator);
    }

    @Override
    public Optional<CredenzialiRequest> acquisisciRegistrazioneFruitore(
            CredentialFieldValidator usernameValidator,
            CredentialFieldValidator passwordValidator) {
        return acquisisciRegistrazione(
                "REGISTRAZIONE FRUITORE",
                false,
                usernameValidator,
                passwordValidator);
    }

    private Optional<CredenzialiRequest> acquisisciRegistrazione(
            String titolo,
            boolean ripetiSeNonConfermata,
            CredentialFieldValidator usernameValidator,
            CredentialFieldValidator passwordValidator) {
        ui.newLine();
        ui.stampa(titolo);
        ui.stampaInfo("Username: minimo 3 caratteri, unico in tutto il sistema.");
        ui.stampaInfo("Password: minimo 4 caratteri.");
        ui.stampaInfo(IAppView.HINT_ANNULLA);
        ui.newLine();

        while (true) {
            try {
                String username = raccogliCampo("Nuovo username: ", usernameValidator).trim();
                String password = raccogliPassword(passwordValidator);

                if (!ui.acquisisciSiNo("Confermi la registrazione con username \"" + username + "\"?")) {
                    if (ripetiSeNonConfermata) {
                        ui.stampaInfo("Registrazione non confermata. Inserisci nuovamente i dati.");
                        ui.newLine();
                        continue;
                    }
                    return Optional.empty();
                }

                return Optional.of(new CredenzialiRequest(username, password));
            } catch (OperationCancelledException e) {
                return Optional.empty();
            }
        }
    }

    private String raccogliCampo(String prompt, CredentialFieldValidator validator) {
        while (true) {
            String value = ui.acquisisciStringa(prompt);
            Optional<Failure> failure = validator.validate(value);
            if (failure.isEmpty()) {
                return value;
            }
            ui.stampaErrore(messages.message(failure.get()));
        }
    }

    private String raccogliPassword(CredentialFieldValidator validator) {
        while (true) {
            String value = ui.acquisisciPassword("Nuova password: ");
            Optional<Failure> failure = validator.validate(value);
            if (failure.isEmpty()) {
                return value;
            }
            ui.stampaErrore(messages.message(failure.get()));
        }
    }

    @Override
    public void mostraApplicazioneAvviata() {
        ui.header("Gestore Eventi - Versione 5");
    }

    @Override
    public void mostraLoginFallito() {
        ui.stampaErrore("Credenziali non valide. Riprova.");
        ui.newLine();
    }

    @Override
    public void mostraLoginRiuscitoConfiguratore(Configuratore configuratore) {
        ui.stampaSuccesso("Login riuscito.");
    }

    @Override
    public void mostraLoginRiuscitoFruitore(Fruitore fruitore) {
        ui.stampaSuccesso("Login riuscito. Benvenuto, " + fruitore.getUsername() + "!");
        ui.newLine();
    }

    @Override
    public void mostraPrimoAccessoConfiguratore() {
        ui.newLine();
        ui.stampa("Primo accesso con credenziali predefinite.");
        ui.stampa("Scegli le tue credenziali personali.");
    }

    @Override
    public void mostraRegistrazioneAnnullata() {
        ui.stampaInfo("Registrazione annullata.");
        ui.newLine();
    }

    @Override
    public void mostraRegistrazioneCompletata(String username) {
        ui.stampaSuccesso("Registrazione completata. Benvenuto, " + username + "!");
        ui.newLine();
    }

    @Override
    public void mostraLogoutConfiguratore(Configuratore configuratore) {
        ui.stampa("Logout configuratore effettuato.");
        ui.newLine();
    }

    @Override
    public void mostraLogoutFruitore(Fruitore fruitore) {
        ui.stampa("Logout fruitore effettuato.");
        ui.newLine();
    }

    @Override
    public void mostraUscita() {
        ui.stampa("Arrivederci!");
    }

    @Override
    public void mostraErrore(Failure failure) {
        ui.stampaErrore(messages.message(failure));
        ui.newLine();
    }
}
