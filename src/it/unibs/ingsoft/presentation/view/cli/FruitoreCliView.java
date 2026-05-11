package it.unibs.ingsoft.presentation.view.cli;

import it.unibs.ingsoft.domain.shared.AppConstants;
import it.unibs.ingsoft.domain.utente.Fruitore;
import it.unibs.ingsoft.domain.notifica.Notifica;
import it.unibs.ingsoft.domain.proposta.Proposta;
import it.unibs.ingsoft.presentation.view.interfaces.IAppView;
import it.unibs.ingsoft.presentation.view.interfaces.IFruitoreView;
import it.unibs.ingsoft.presentation.view.interfaces.OperationCancelledException;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class FruitoreCliView implements IFruitoreView {
    private static final String[] MENU_PRINCIPALE = {
            "Visualizza bacheca (per categoria)",
            "Disdici iscrizione a una proposta",
            "Spazio Personale (Notifiche)"
    };

    private static final DateTimeFormatter NOTIFICA_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final IAppView ui;

    public FruitoreCliView(IAppView ui) {
        this.ui = ui;
    }

    @Override
    public MainAction scegliAzionePrincipale(Fruitore fruitore) {
        ui.stampaMenu("MENU PRINCIPALE FRUITORE", MENU_PRINCIPALE, "Logout");
        int choice = ui.acquisisciIntero("Scelta: ", 0, MENU_PRINCIPALE.length);
        ui.newLine();
        return choice == 0 ? MainAction.LOGOUT : MainAction.values()[choice - 1];
    }

    @Override
    public Optional<Proposta> selezionaPropostaDaBacheca(Map<String, List<Proposta>> bacheca) {
        if (bacheca.isEmpty()) {
            ui.stampa("La bacheca e vuota. Nessuna proposta aperta al momento.");
            ui.pausaConSpaziatura();
            return Optional.empty();
        }

        ui.header("BACHECA PROPOSTE");
        List<Proposta> proposteSelezionabili = new ArrayList<>();
        int indice = 1;

        for (Map.Entry<String, List<Proposta>> entry : bacheca.entrySet()) {
            ui.stampaSezione("Categoria: " + entry.getKey());
            for (Proposta proposta : entry.getValue()) {
                proposteSelezionabili.add(proposta);
                ui.stampa(String.format(" %d) %s (Scadenza: %s, %s)",
                        indice++,
                        titolo(proposta),
                        proposta.getTermineIscrizione(),
                        postiLiberi(proposta)));
            }
            ui.newLine();
        }

        ui.stampa("Digita il numero della proposta per i dettagli o per iscriverti (0 per tornare indietro).");
        int choice = ui.acquisisciIntero("Scelta: ", 0, proposteSelezionabili.size());
        return choice == 0 ? Optional.empty() : Optional.of(proposteSelezionabili.get(choice - 1));
    }

    @Override
    public boolean confermaIscrizione(Proposta proposta) {
        ui.header("DETTAGLI PROPOSTA");
        ui.mostraRiepilogoProposta(proposta);
        ui.newLine();
        try {
            boolean confermata = ui.acquisisciSiNo("Vuoi iscriverti a questa proposta?");
            if (!confermata) {
                ui.pausaConSpaziatura();
            }
            return confermata;
        } catch (OperationCancelledException e) {
            ui.pausaConSpaziatura();
            return false;
        }
    }

    @Override
    public Optional<Proposta> selezionaPropostaDaDisdire(List<Proposta> proposte) {
        if (proposte.isEmpty()) {
            ui.stampa("Non sei iscritto a nessuna proposta aperta.");
            ui.pausaConSpaziatura();
            return Optional.empty();
        }

        ui.header("DISDICI ISCRIZIONE");
        for (int i = 0; i < proposte.size(); i++) {
            Proposta proposta = proposte.get(i);
            ui.stampa(String.format(" %d) %s (Scadenza: %s)",
                    i + 1,
                    titolo(proposta),
                    proposta.getTermineIscrizione()));
        }
        ui.newLine();

        ui.stampa("Seleziona la proposta da cui disdire l'iscrizione (0 per tornare indietro).");
        int choice = ui.acquisisciIntero("Scelta: ", 0, proposte.size());
        return choice == 0 ? Optional.empty() : Optional.of(proposte.get(choice - 1));
    }

    @Override
    public boolean confermaDisiscrizione(Proposta proposta) {
        ui.mostraRiepilogoProposta(proposta);
        ui.newLine();
        try {
            boolean confermata = ui.acquisisciSiNo("Vuoi davvero disdire l'iscrizione a questa proposta?");
            if (!confermata) {
                ui.pausaConSpaziatura();
            }
            return confermata;
        } catch (OperationCancelledException e) {
            ui.pausaConSpaziatura();
            return false;
        }
    }

    @Override
    public Optional<Notifica> selezionaNotificaDaEliminare(Fruitore fruitore, List<Notifica> notifiche) {
        ui.header("SPAZIO PERSONALE DI " + fruitore.getUsername());

        if (notifiche.isEmpty()) {
            ui.stampa("Nessuna notifica presente.");
            ui.newLine();
            ui.pausa();
            return Optional.empty();
        }

        for (int i = 0; i < notifiche.size(); i++) {
            Notifica notifica = notifiche.get(i);
            ui.stampa((i + 1) + ". [" + notifica.dataCreazione().format(NOTIFICA_FMT) + "] "
                    + NotificaMessageMapper.message(notifica));
        }
        ui.newLine();

        ui.stampa("Digita il numero di una notifica per eliminarla (0 per tornare indietro).");
        int choice = ui.acquisisciIntero("Scelta: ", 0, notifiche.size());
        return choice == 0 ? Optional.empty() : Optional.of(notifiche.get(choice - 1));
    }

    @Override
    public boolean confermaEliminazioneNotifica(Notifica notifica) {
        try {
            return ui.acquisisciSiNo("Confermi l'eliminazione di questa notifica?");
        } catch (OperationCancelledException e) {
            return false;
        }
    }

    @Override
    public void mostraIscrizioneEffettuata(Proposta proposta) {
        ui.stampaSuccesso("Iscrizione effettuata con successo!");
        ui.pausaConSpaziatura();
    }

    @Override
    public void mostraDisiscrizioneEffettuata(Proposta proposta) {
        ui.stampaSuccesso("Iscrizione disdetta con successo.");
        ui.pausaConSpaziatura();
    }

    @Override
    public void mostraNotificaEliminata(Notifica notifica) {
        ui.stampaSuccesso("Notifica eliminata.");
        ui.newLine();
    }

    @Override
    public void mostraErrore(Exception e) {
        ui.stampaErrore(DomainErrorMessageMapper.message(e));
        ui.pausaConSpaziatura();
    }

    private String titolo(Proposta proposta) {
        return proposta.getValoriCampi().getOrDefault(AppConstants.CAMPO_TITOLO, "Senza Titolo");
    }

    private String postiLiberi(Proposta proposta) {
        return (proposta.getNumeroPartecipanti() - proposta.getListaAderenti().size()) + " posti liberi";
    }
}
