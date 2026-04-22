package it.unibs.ingsoft.presentation.controller;

import it.unibs.ingsoft.application.PropostaService;
import it.unibs.ingsoft.domain.*;
import it.unibs.ingsoft.v5.domain.*;
import it.unibs.ingsoft.presentation.view.contract.IAppView;
import it.unibs.ingsoft.presentation.view.contract.OperationCancelledException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Gestisce il flusso di lavoro della proposta per il Configuratore:
 * creazione, compilazione del form, ciclo di validazione, pubblicazione e visualizzazione in bacheca.
 *
 * <p>Estratto da {@link ConfiguratoreController} in modo che ogni classe abbia un
 * unico motivo per cambiare: questa classe cambia solo quando il ciclo di vita della proposta
 * o la sua UI cambia; {@code ConfiguratoreController} cambia solo quando
 * i menu di gestione dei campi/categorie cambiano.</p>
 *
 * <p>Dipende solo da {@link IAppView} e {@link PropostaService}.
 * La selezione della categoria è delegata al chiamante ({@link ConfiguratoreController}).
 */
public final class PropostaController {
    private static final String[] MENU_DETTAGLI_PROPOSTA = {
            "Visualizza dettagli",
            "Visualizza aderenti",
            "Visualizza cronologia stati"
    };
    private final IAppView ui;
    private final PropostaService ps;

    public PropostaController(IAppView ui, PropostaService ps) {
        this.ui = ui;
        this.ps = ps;
    }

    /**
     * Avvia il flusso di creazione della proposta per la categoria indicata.
     */
    public void avviaCreazione(Categoria categoria, List<Campo> campiBase, List<Campo> campiComuni) {
        ui.header("CREA PROPOSTA");

        Proposta proposta;
        try {
            proposta = ps.creaProposta(categoria, campiBase, campiComuni);
        } catch (IllegalArgumentException e) {
            ui.stampaErrore(e.getMessage());
            ui.newLine();
            ui.pausa();
            return;
        }

        ui.newLine();
        ui.stampa("Digita 'annulla' per abortire l'operazione.");
        ui.stampa("(*) = obbligatorio | il tipo è indicato tra [  ]");
        ui.newLine();

        try {
            Optional<Map<String, String>> formResult = ui.acquisisciValoriProposta(proposta, ps::validaCampo);
            if (formResult.isEmpty()) {
                ui.stampa("Operazione annullata.");
                ui.newLine();
                ui.pausa();
                return;
            }
            proposta.putAllValoriCampi(formResult.get());

            List<String> errori = ps.validaProposta(proposta);
            boolean abortito = correggiFincheValida(proposta, errori);
            if (abortito) return;

            mostraRiepilogoEPubblica(proposta);
        } catch (OperationCancelledException e) {
            ui.stampa("Operazione annullata.");
            ui.newLine();
            ui.pausa();
        }
    }

    /**
     * Mostra la bacheca (proposte APERTA raggruppate per categoria).
     */
    public void mostraBacheca() {
        ui.header("BACHECA");
        ui.mostraBacheca(ps.getBachecaPerCategoria());
        ui.newLine();
        ui.pausa();
    }

    /**
     * Ciclo di validazione e correzione: ripete finché la proposta è valida o l'utente annulla.
     *
     * @return {@code true} se l'utente ha scartato la proposta, {@code false} se è valida
     */
    private boolean correggiFincheValida(Proposta proposta, List<String> errori) {
        while (!errori.isEmpty()) {
            ui.newLine();
            ui.stampa("La proposta NON è valida per i seguenti motivi:");
            for (String err : errori)
                ui.stampaErrore(err);
            ui.newLine();

            if (!ui.acquisisciSiNo("Vuoi correggere i campi errati?")) {
                ui.stampa("Proposta scartata.");
                ui.newLine();
                ui.pausa();
                return true;
            }

            Set<String> nomiConErrore = ps.getCampiConErrore(proposta, errori).stream()
                    .map(Campo::getNome)
                    .collect(Collectors.toSet());

            Optional<Map<String, String>> corrResult = ui.correggiCampiProposta(proposta, nomiConErrore, ps::validaCampo);
            if (corrResult.isEmpty()) {
                ui.stampa("Proposta scartata.");
                ui.newLine();
                ui.pausa();
                return true;
            }

            proposta.putAllValoriCampi(corrResult.get());
            errori = ps.validaProposta(proposta);
        }
        return false;
    }

    /**
     * Mostra il riepilogo e salva la proposta in memoria per la pubblicazione differita.
     */
    private void mostraRiepilogoEPubblica(Proposta proposta) {
        ui.newLine();
        ui.mostraRiepilogoProposta(proposta);

        try {
            ps.salvaProposta(proposta);
            ui.stampaSuccesso("Proposta valida salvata. Puoi pubblicarla dal menu 'Pubblicare una proposta di iniziativa'.");
        } catch (IllegalStateException e) {
            ui.stampaErrore(e.getMessage());
        }

        ui.newLine();
        ui.pausa();
    }

    /**
     * Permette al configuratore di navigare l'archivio completo delle proposte:
     * seleziona uno stato, poi una proposta, poi visualizza dettagli, aderenti o cronologia.
     */
    public void visualizzaArchivioProposte() {
        Map<StatoProposta, List<Proposta>> archivio = ps.getPropostePerStato();
        if (archivio.isEmpty()) {
            ui.stampa("  Archivio vuoto.");
            ui.pausaConSpaziatura();
            return;
        }

        List<StatoProposta> statiPresenti = new ArrayList<>(archivio.keySet());
        String[] menuStati = statiPresenti.stream()
                .map(s -> s.name() + " (" + archivio.get(s).size() + ")")
                .toArray(String[]::new);

        while (true) {
            ui.stampaMenu("ARCHIVIO PROPOSTE", menuStati, "Torna");
            int sceltaStato = ui.acquisisciIntero("Scelta: ", 0, statiPresenti.size());
            if (sceltaStato == 0) return;

            StatoProposta statoScelto = statiPresenti.get(sceltaStato - 1);
            List<Proposta> proposteStato = archivio.get(statoScelto);

            String[] menuProposte = proposteStato.stream()
                    .map(p -> p.getValoriCampi().getOrDefault(AppConstants.CAMPO_TITOLO, "(senza titolo)")
                            + " - " + p.getCategoria().getNome())
                    .toArray(String[]::new);

            while (true) {
                ui.stampaMenu("PROPOSTE IN STATO " + statoScelto.name(), menuProposte, "Torna");
                int sceltaProposta = ui.acquisisciIntero("Scelta: ", 0, proposteStato.size());
                if (sceltaProposta == 0) break;

                menuDettagliProposta(proposteStato.get(sceltaProposta - 1));
            }
        }
    }

    private void menuDettagliProposta(Proposta p) {
        String titolo = p.getValoriCampi().getOrDefault(AppConstants.CAMPO_TITOLO, "?");
        while (true) {
            ui.stampaMenu("PROPOSTA — " + titolo, MENU_DETTAGLI_PROPOSTA, "Torna");
            int scelta = ui.acquisisciIntero("Scelta: ", 0, MENU_DETTAGLI_PROPOSTA.length);
            switch (scelta) {
                case 1:
                    ui.mostraRiepilogoProposta(p);
                    ui.pausaConSpaziatura();
                    break;
                case 2:
                    ui.mostraAderenti(p.getListaAderenti());
                    ui.pausaConSpaziatura();
                    break;
                case 3:
                    ui.mostraCronologiaStati(p.getStateHistory());
                    ui.pausaConSpaziatura();
                    break;
                case 0:
                    return;
            }
        }
    }

    /**
     * Elenca le proposte valide salvate, permette di selezionarne una e la pubblica.
     */
    public void pubblicaPropostaSalvata() {
        ui.header("PUBBLICA PROPOSTA");

        List<Proposta> valide = ps.getProposteValide();
        if (valide.isEmpty()) {
            ui.stampa("Nessuna proposta valida da pubblicare.");
            ui.newLine();
            ui.pausa();
            return;
        }

        List<String> labels = new java.util.ArrayList<>();
        for (int i = 0; i < valide.size(); i++) {
            Proposta p = valide.get(i);
            String titolo = p.getValoriCampi().getOrDefault("Titolo", "(senza titolo)");
            String cat = p.getCategoria().getNome();
            labels.add((i + 1) + ". " + titolo + "  [" + cat + "]");
        }

        ui.stampa("Proposte valide disponibili:");
        for (String label : labels)
            ui.stampa("  " + label);
        ui.stampa("  0. Torna");
        ui.newLine();

        try {
            int scelta = ui.acquisisciIntero("Scelta: ", 0, valide.size());
            if (scelta == 0)
                return;

            Proposta selezionata = valide.get(scelta - 1);

            ui.newLine();
            ui.mostraRiepilogoProposta(selezionata);

            if (ui.acquisisciSiNo("Vuoi pubblicare questa proposta in bacheca?")) {
                try {
                    ps.pubblicaProposta(selezionata);
                    ps.rimuoviPropostaValida(selezionata);
                    ui.stampaSuccesso("Proposta pubblicata in bacheca!");
                } catch (Exception e) {
                    ui.stampaErrore(e.getMessage());
                }
            } else {
                ui.stampa("Pubblicazione annullata. La proposta resta disponibile per la pubblicazione.");
            }

            ui.newLine();
            ui.pausa();
        } catch (OperationCancelledException e) {
            ui.stampa("Operazione annullata.");
            ui.newLine();
            ui.pausa();
        }
    }
}
