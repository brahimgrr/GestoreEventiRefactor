package it.unibs.ingsoft.presentation.controller;

import it.unibs.ingsoft.application.AuthenticationService;
import it.unibs.ingsoft.domain.Configuratore;
import it.unibs.ingsoft.domain.Fruitore;
import it.unibs.ingsoft.presentation.view.contract.IAppView;
import it.unibs.ingsoft.presentation.view.contract.OperationCancelledException;

/**
 * Gestisce il login del configuratore e la registrazione al primo accesso.
 * Non contiene logica di business: si occupa solo dell'interazione UI per l'autenticazione.
 */
public final class AuthController {
    private final IAppView ui;
    private final AuthenticationService auth;

    public AuthController(IAppView ui, AuthenticationService auth) {
        this.ui = ui;
        this.auth = auth;
    }

    /**
     * Ciclo di login: ripete fino a credenziali valide.
     * Se vengono usate le credenziali predefinite, forza la registrazione di credenziali personali.
     *
     * @return il configuratore autenticato
     */
    public Configuratore loginConfiguratore() {
        while (true) {
            ui.newLine();
            ui.stampa("LOGIN CONFIGURATORE");
            String u = ui.acquisisciStringa("Username: ");
            String p = ui.acquisisciStringa("Password: ");

            var result = auth.login(u, p);

            if (result.isEmpty()) {
                ui.stampaErrore("Credenziali non valide. Riprova.");
                ui.newLine();
                continue;
            }

            Configuratore logged = result.get();
            ui.stampaSuccesso("Login riuscito.");

            if (AuthenticationService.USERNAME_PREDEFINITO.equals(logged.getUsername())) {
                ui.newLine();
                ui.stampa("Primo accesso con credenziali predefinite.");
                ui.stampa("Scegli le tue credenziali personali.");
                try {
                    Configuratore registered = registrazioneInterattiva();
                    ui.newLine();
                    return registered;
                } catch (OperationCancelledException e) {
                    ui.stampaInfo("Registrazione annullata. Effettua nuovamente il login.");
                    ui.newLine();
                    continue;
                }
            }

            ui.newLine();
            return logged;
        }
    }

    public Fruitore loginFruitore() {
        while (true) {
            ui.newLine();
            ui.stampa("LOGIN FRUITORE");
            String u = ui.acquisisciStringa("Username: ");
            String p = ui.acquisisciStringa("Password: ");

            var result = auth.loginFruitore(u, p);

            if (result.isEmpty()) {
                ui.stampaErrore("Credenziali non valide. Riprova.");
                ui.newLine();
                continue;
            }

            Fruitore logged = result.get();
            ui.stampaSuccesso("Login riuscito. Benvenuto, " + logged.getUsername() + "!");
            ui.newLine();
            return logged;
        }
    }

    public Fruitore registraFruitore() {
        ui.newLine();
        ui.stampa("REGISTRAZIONE FRUITORE");
        ui.stampaInfo("Username: minimo 3 caratteri, unico in tutto il sistema.");
        ui.stampaInfo("Password: minimo 4 caratteri.");
        ui.stampaInfo(IAppView.HINT_ANNULLA);
        ui.newLine();

        while (true) {
            String newU = raccogliUsername();
            String newP = raccogliPassword();

            try {
                if (!ui.acquisisciSiNo("Confermi la registrazione con username \"" + newU + "\"?"))
                    throw new OperationCancelledException();
                Fruitore registered = auth.registraNuovoFruitore(newU, newP);
                ui.stampaSuccesso("Registrazione completata. Benvenuto, " + newU + "!");
                return registered;
            } catch (IllegalArgumentException e) {
                ui.stampaErrore(e.getMessage());
                ui.newLine();
            } catch (OperationCancelledException e) {
                ui.stampaInfo("Registrazione annullata.");
                return null;
            }
        }
    }

    /**
     * Guida l'utente alla registrazione delle credenziali personali con ri-prompt campo per campo.
     *
     * @throws OperationCancelledException se l'utente annulla
     */
    private Configuratore registrazioneInterattiva() {
        ui.stampaInfo("Username: minimo 3 caratteri, non può essere '" +
                AuthenticationService.USERNAME_PREDEFINITO + "'.");
        ui.stampaInfo("Password: minimo 4 caratteri.");
        ui.stampaInfo(IAppView.HINT_ANNULLA);
        ui.newLine();

        while (true) {
            String newU = raccogliUsername();
            String newP = raccogliPassword();

            try {
                if (!ui.acquisisciSiNo("Confermi la registrazione con username \"" + newU + "\"?")) {
                    ui.stampaInfo("Registrazione non confermata. Inserisci nuovamente i dati.");
                    ui.newLine();
                    continue;
                }
                Configuratore registered = auth.registraNuovoConfiguratore(newU, newP);
                ui.stampaSuccesso("Registrazione completata. Benvenuto, " + newU + "!");
                return registered;
            } catch (IllegalArgumentException e) {
                ui.stampaErrore(e.getMessage());
                ui.newLine();
            }
        }
    }

    /**
     * Acquisisce e valida inline lo username: ri-chiede solo questo campo in caso di errore.
     */
    private String raccogliUsername() {
        while (true) {
            String newU = ui.acquisisciStringaConValidazione(
                    "Nuovo username: ",
                    u -> !u.isBlank() && u.trim().length() >= 3,
                    "Username troppo corto (minimo 3 caratteri)."
            ).trim();

            if (newU.equalsIgnoreCase(AuthenticationService.USERNAME_PREDEFINITO)) {
                ui.stampaErrore("Username riservato. Scegli un nome diverso.");
                continue;
            }

            if (auth.esisteUsername(newU)) {
                ui.stampaErrore("Username già in uso. Scegli un nome diverso.");
                continue;
            }

            return newU;
        }
    }

    /**
     * Acquisisce e valida inline la password: ri-chiede solo questo campo in caso di errore.
     */
    private String raccogliPassword() {
        while (true) {
            String newP = ui.acquisisciPassword("Nuova password: ");

            if (newP == null || newP.isBlank() || newP.trim().length() < 4) {
                ui.stampaErrore("Password troppo corta (minimo 4 caratteri).");
                continue;
            }

            return newP;
        }
    }
}
