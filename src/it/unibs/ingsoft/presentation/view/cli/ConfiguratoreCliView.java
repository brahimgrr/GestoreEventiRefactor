package it.unibs.ingsoft.presentation.view.cli;

import it.unibs.ingsoft.application.proposta.dto.PropostaValidationResult;
import it.unibs.ingsoft.application.batch.dto.ImportResult;
import it.unibs.ingsoft.application.catalogo.dto.CampoBaseExtraRequest;
import it.unibs.ingsoft.application.catalogo.dto.CampoDefinitionRequest;
import it.unibs.ingsoft.application.catalogo.dto.CampoObbligatorietaRequest;
import it.unibs.ingsoft.application.catalogo.dto.CatalogoOperationResult;
import it.unibs.ingsoft.domain.*;
import it.unibs.ingsoft.presentation.view.interfaces.IAppView;
import it.unibs.ingsoft.presentation.view.interfaces.IConfiguratoreView;
import it.unibs.ingsoft.presentation.view.interfaces.OperationCancelledException;
import it.unibs.ingsoft.presentation.view.interfaces.ProposalFieldValidator;

import java.nio.file.Path;
import java.util.*;

public final class ConfiguratoreCliView implements IConfiguratoreView {
    private static final String[] MENU_PRINCIPALE = {
            "Gestire campi COMUNI",
            "Gestire CATEGORIE e campi SPECIFICI",
            "Visualizzare categorie e campi",
            "Creare una proposta di iniziativa",
            "Pubblicare una proposta di iniziativa",
            "Visualizzare la bacheca",
            "Ritirare una proposta",
            "Visualizzare archivio proposte",
            "Importa dati da file"
    };

    private static final String[] MENU_CATEGORIE = {
            "Crea categoria",
            "Rimuovi categoria",
            "Gestisci campi specifici di una categoria"
    };

    private static final String[] MENU_CAMPI = {
            "Aggiungi campo",
            "Rimuovi campo",
            "Cambia obbligatorieta campo"
    };

    private static final String[] MENU_DETTAGLI_PROPOSTA = {
            "Visualizza dettagli",
            "Visualizza aderenti",
            "Visualizza cronologia stati"
    };

    private final IAppView ui;

    public ConfiguratoreCliView(IAppView ui) {
        this.ui = ui;
    }

    @Override
    public MainAction scegliAzionePrincipale() {
        ui.stampaMenu("MENU PRINCIPALE CONFIGURATORE", MENU_PRINCIPALE, "Logout");
        int choice = ui.acquisisciIntero("Scelta: ", 0, MENU_PRINCIPALE.length);
        ui.newLine();
        return choice == 0 ? MainAction.LOGOUT : MainAction.values()[choice - 1];
    }

    @Override
    public CategoryAction scegliAzioneCategorie(List<Categoria> categorie) {
        ui.header("CATEGORIE");
        ui.stampaCategorie(categorie);
        ui.newLine();
        ui.stampaMenu("", MENU_CATEGORIE, "Torna");
        int choice = ui.acquisisciIntero("Scelta: ", 0, MENU_CATEGORIE.length);
        ui.newLine();
        return choice == 0 ? CategoryAction.TORNA : CategoryAction.values()[choice - 1];
    }

    @Override
    public FieldAction scegliAzioneCampiComuni(List<Campo> campi) {
        return scegliAzioneCampi("CAMPI COMUNI", campi);
    }

    @Override
    public FieldAction scegliAzioneCampiSpecifici(Categoria categoria) {
        return scegliAzioneCampi("CAMPI SPECIFICI", categoria.getCampiSpecifici());
    }

    private FieldAction scegliAzioneCampi(String titolo, List<Campo> campi) {
        ui.header(titolo);
        ui.stampaCampi(campi);
        ui.newLine();
        ui.stampaMenu("", MENU_CAMPI, "Torna");
        int choice = ui.acquisisciIntero("Scelta: ", 0, MENU_CAMPI.length);
        ui.newLine();
        return choice == 0 ? FieldAction.TORNA : FieldAction.values()[choice - 1];
    }

    @Override
    public Optional<List<CampoBaseExtraRequest>> acquisisciCampiBaseExtra(List<Campo> campiPredefiniti) {
        ui.header("PRIMA CONFIGURAZIONE - Campi base");
        ui.newLine();
        ui.stampa("I seguenti campi base sono gia presenti (definiti dalla traccia):");
        ui.stampaCampi(campiPredefiniti);
        ui.newLine();
        ui.stampaAvviso("Puoi aggiungere campi base EXTRA (obbligatori e immutabili).");
        ui.stampaAvviso("Questi campi NON potranno essere modificati o rimossi in futuro.");
        ui.newLine();

        try {
            if (!ui.acquisisciSiNo("Vuoi aggiungere campi base extra?")) {
                return Optional.of(List.of());
            }

            ui.stampa("Inserisci i nomi dei campi extra (riga vuota per terminare):");
            List<String> nomiInput = ui.acquisisciListaNomi("Campi base extra");
            List<CampoBaseExtraRequest> richieste = new ArrayList<>();

            for (String nome : nomiInput) {
                TipoDato tipoDato = ui.acquisisciTipoDato("Tipo per \"" + nome + "\":");
                richieste.add(new CampoBaseExtraRequest(nome, tipoDato));
            }
            return Optional.of(richieste);
        } catch (OperationCancelledException e) {
            mostraOperazioneAnnullata();
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> acquisisciNomeCategoria() {
        try {
            ui.stampaInfo(IAppView.HINT_ANNULLA);
            return Optional.of(ui.acquisisciStringaConValidazione(
                    "Nome nuova categoria: ",
                    n -> !n.isBlank(),
                    "Il nome non puo essere vuoto."
            ));
        } catch (OperationCancelledException e) {
            mostraOperazioneAnnullata();
            return Optional.empty();
        }
    }

    @Override
    public Optional<Categoria> selezionaCategoriaDaRimuovere(List<Categoria> categorie) {
        return ui.selezionaElemento("Seleziona categoria da rimuovere:", categorie);
    }

    @Override
    public Optional<Categoria> selezionaCategoriaPerCampiSpecifici(List<Categoria> categorie) {
        return ui.selezionaElemento("Seleziona categoria:", categorie);
    }

    @Override
    public Optional<Categoria> selezionaCategoriaPerProposta(List<Categoria> categorie) {
        if (categorie.isEmpty()) {
            ui.stampa("Nessuna categoria disponibile. Crea almeno una categoria prima.");
            ui.newLine();
            ui.pausa();
            return Optional.empty();
        }

        ui.stampaSezione("Categorie disponibili");
        OptionalInt scelta = ui.selezionaCategoria(categorie);
        return scelta.isEmpty() ? Optional.empty() : Optional.of(categorie.get(scelta.getAsInt()));
    }

    @Override
    public boolean confermaRimozioneCategoria(Categoria categoria) {
        try {
            return ui.acquisisciSiNo("Rimuovere '" + categoria.getNome() + "' e tutti i suoi campi specifici?");
        } catch (OperationCancelledException e) {
            mostraOperazioneAnnullata();
            return false;
        }
    }

    @Override
    public Optional<CampoDefinitionRequest> acquisisciNuovoCampo() {
        try {
            ui.stampaInfo(IAppView.HINT_ANNULLA);
            String nome = ui.acquisisciStringaConValidazione(
                    "Nome campo: ",
                    n -> !n.isBlank(),
                    "Il nome non puo essere vuoto."
            );
            TipoDato tipo = ui.acquisisciTipoDato("Tipo di dato:");
            boolean obbligatorio = ui.acquisisciSiNo("Obbligatorio?");

            if (!ui.acquisisciSiNo("Aggiungere '" + nome + "' [" + tipo + ", " +
                    (obbligatorio ? "obbligatorio" : "facoltativo") + "]?")) {
                mostraOperazioneAnnullata();
                return Optional.empty();
            }

            return Optional.of(new CampoDefinitionRequest(nome, tipo, obbligatorio));
        } catch (OperationCancelledException e) {
            mostraOperazioneAnnullata();
            return Optional.empty();
        }
    }

    @Override
    public Optional<Campo> selezionaCampoDaRimuovere(List<Campo> campi) {
        return ui.selezionaElemento("Seleziona campo da rimuovere:", campi);
    }

    @Override
    public boolean confermaRimozioneCampo(Campo campo) {
        try {
            return ui.acquisisciSiNo("Rimuovere '" + campo.getNome() + "'?");
        } catch (OperationCancelledException e) {
            mostraOperazioneAnnullata();
            return false;
        }
    }

    @Override
    public Optional<CampoObbligatorietaRequest> acquisisciObbligatorietaCampo(List<Campo> campi) {
        Optional<Campo> campo = ui.selezionaElemento("Seleziona campo:", campi);
        if (campo.isEmpty()) {
            mostraOperazioneAnnullata();
            return Optional.empty();
        }

        try {
            return Optional.of(new CampoObbligatorietaRequest(
                    campo.get().getNome(),
                    ui.acquisisciSiNo("Impostare come obbligatorio?")
            ));
        } catch (OperationCancelledException e) {
            mostraOperazioneAnnullata();
            return Optional.empty();
        }
    }

    @Override
    public Optional<Proposta> selezionaPropostaDaRitirare(List<Proposta> proposte) {
        if (proposte.isEmpty()) {
            ui.stampa("Nessuna proposta aperta o confermata disponibile per il ritiro.");
            ui.pausaConSpaziatura();
            return Optional.empty();
        }

        ui.header("RITIRO PROPOSTA");
        stampaElencoProposteConStato(proposte);
        ui.stampa("Seleziona la proposta da ritirare (0 per tornare indietro).");
        int choice = ui.acquisisciIntero("Scelta: ", 0, proposte.size());
        return choice == 0 ? Optional.empty() : Optional.of(proposte.get(choice - 1));
    }

    @Override
    public boolean confermaRitiro(Proposta proposta) {
        ui.mostraRiepilogoProposta(proposta);
        ui.newLine();
        ui.stampaAvviso("Il ritiro e una misura eccezionale, da adottare solo per cause di forza maggiore.");
        try {
            return ui.acquisisciSiNo("Vuoi davvero ritirare questa proposta?");
        } catch (OperationCancelledException e) {
            mostraOperazioneAnnullata();
            return false;
        }
    }

    @Override
    public Optional<Proposta> selezionaPropostaDaPubblicare(List<Proposta> proposte) {
        ui.header("PUBBLICA PROPOSTA");
        if (proposte.isEmpty()) {
            ui.stampa("Nessuna proposta valida da pubblicare.");
            ui.newLine();
            ui.pausa();
            return Optional.empty();
        }

        ui.stampa("Proposte valide disponibili:");
        for (int i = 0; i < proposte.size(); i++) {
            Proposta p = proposte.get(i);
            String titolo = p.getValoriCampi().getOrDefault(AppConstants.CAMPO_TITOLO, "(senza titolo)");
            String categoria = p.getCategoria().getNome();
            ui.stampa("  " + (i + 1) + ". " + titolo + "  [" + categoria + "]");
        }
        ui.stampa("  0. Torna");
        ui.newLine();

        int choice = ui.acquisisciIntero("Scelta: ", 0, proposte.size());
        return choice == 0 ? Optional.empty() : Optional.of(proposte.get(choice - 1));
    }

    @Override
    public boolean confermaPubblicazione(Proposta proposta) {
        ui.newLine();
        ui.mostraRiepilogoProposta(proposta);
        try {
            return ui.acquisisciSiNo("Vuoi pubblicare questa proposta in bacheca?");
        } catch (OperationCancelledException e) {
            mostraOperazioneAnnullata();
            return false;
        }
    }

    @Override
    public Optional<Path> acquisisciPercorsoImportazione() {
        ui.header("IMPORTAZIONE BATCH");
        ui.stampaInfo("Importa categorie, campi comuni e proposte da un file JSON.");
        ui.stampaInfo(IAppView.HINT_ANNULLA);
        ui.newLine();

        try {
            String percorso = ui.acquisisciStringa("Percorso del file JSON: ");
            if (percorso == null || percorso.isBlank()) {
                ui.stampaErrore("Percorso non valido.");
                ui.pausaConSpaziatura();
                return Optional.empty();
            }

            ui.stampa("Importazione in corso...");
            ui.newLine();
            return Optional.of(Path.of(percorso.trim()));
        } catch (OperationCancelledException e) {
            mostraOperazioneAnnullata();
            return Optional.empty();
        }
    }

    @Override
    public Optional<Map<String, String>> acquisisciValoriProposta(Proposta proposta, ProposalFieldValidator validator) {
        ui.header("CREA PROPOSTA");
        ui.newLine();
        ui.stampa("Digita 'annulla' per abortire l'operazione.");
        ui.stampa("(*) = obbligatorio | il tipo e indicato tra [  ]");
        ui.newLine();
        return ui.acquisisciValoriProposta(proposta, validator);
    }

    @Override
    public Optional<Map<String, String>> correggiValoriProposta(
            Proposta proposta,
            PropostaValidationResult result,
            ProposalFieldValidator validator) {
        ui.newLine();
        ui.stampa("La proposta NON e valida per i seguenti motivi:");
        for (String errore : result.errori()) {
            ui.stampaErrore(errore);
        }
        ui.newLine();

        try {
            if (!ui.acquisisciSiNo("Vuoi correggere i campi errati?")) {
                ui.stampa("Proposta scartata.");
                ui.newLine();
                ui.pausa();
                return Optional.empty();
            }

            Set<String> nomiConErrore = result.campiConErrore().stream()
                    .map(Campo::getNome)
                    .collect(java.util.stream.Collectors.toSet());
            Optional<Map<String, String>> correzioni =
                    ui.correggiCampiProposta(proposta, nomiConErrore, validator);
            if (correzioni.isEmpty()) {
                ui.stampa("Proposta scartata.");
                ui.newLine();
                ui.pausa();
            }
            return correzioni;
        } catch (OperationCancelledException e) {
            ui.stampa("Proposta scartata.");
            ui.newLine();
            ui.pausa();
            return Optional.empty();
        }
    }

    @Override
    public void mostraPrimaConfigurazioneRichiesta() {
        ui.header("PRIMA CONFIGURAZIONE");
        ui.stampaInfo("Non sono ancora stati definiti i campi base.");
        ui.stampaInfo("Il primo configuratore deve inserirli prima di procedere.");
    }

    @Override
    public void mostraCatalogo(List<Campo> base, List<Campo> comuni, List<Categoria> categorie) {
        ui.header("VISUALIZZAZIONE");
        ui.stampaSezione("Campi BASE");
        ui.stampaCampi(base);
        ui.stampaSezione("Campi COMUNI");
        ui.stampaCampi(comuni);
        ui.stampaSezione("Categorie");
        ui.stampaCategorie(categorie);
        ui.newLine();
        ui.pausaConSpaziatura();
    }

    @Override
    public void mostraBacheca(Map<String, List<Proposta>> bacheca) {
        ui.header("BACHECA");
        ui.mostraBacheca(bacheca);
        ui.newLine();
        ui.pausa();
    }

    @Override
    public void mostraArchivioProposte(Map<StatoProposta, List<Proposta>> archivio) {
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
                    .map(this::labelPropostaArchivio)
                    .toArray(String[]::new);

            while (true) {
                ui.stampaMenu("PROPOSTE IN STATO " + statoScelto.name(), menuProposte, "Torna");
                int sceltaProposta = ui.acquisisciIntero("Scelta: ", 0, proposteStato.size());
                if (sceltaProposta == 0) break;

                menuDettagliProposta(proposteStato.get(sceltaProposta - 1));
            }
        }
    }

    @Override
    public void mostraRisultatoImportazione(ImportResult result) {
        ui.stampaSezione("RISULTATO IMPORTAZIONE");
        ui.stampa("  Campi comuni importati: " + result.getCampiComuniImportati());
        ui.stampa("  Categorie importate:    " + result.getCategorieImportate());
        ui.stampa("  Proposte importate:     " + result.getProposteImportate());
        ui.newLine();

        if (result.hasErrors()) {
            ui.stampaAvviso("Errori riscontrati (" + result.getErrori().size() + "):");
            for (String errore : result.getErrori()) {
                ui.stampaErrore(errore);
            }
            ui.newLine();
        }

        if (result.totaleImportati() == 0 && !result.hasErrors()) {
            ui.stampaInfo("Il file non contiene dati da importare.");
        } else if (result.totaleImportati() > 0) {
            ui.stampaSuccesso("Importazione completata: " + result.totaleImportati() + " elementi importati.");
            if (result.getProposteImportate() > 0) {
                ui.stampaInfo("Le proposte valide possono essere pubblicate dal menu 'Pubblicare una proposta di iniziativa'.");
            }
        }
        ui.pausaConSpaziatura();
    }

    @Override
    public void mostraErrore(Exception e) {
        ui.stampaErrore(e.getMessage());
    }

    @Override
    public void mostraEsitoCatalogo(CatalogoOperationResult result) {
        switch (result) {
            case SUCCESSO:
                ui.stampaSuccesso("Operazione completata.");
                break;
            case NON_TROVATO:
                ui.stampaErrore("Elemento non trovato.");
                break;
            case NESSUNA_MODIFICA:
                ui.stampaAvviso("Nessuna modifica.");
                break;
        }
    }

    @Override
    public void mostraOperazioneAnnullata() {
        ui.stampaInfo("Operazione annullata.");
    }

    @Override
    public void mostraPropostaSalvata(Proposta proposta) {
        ui.newLine();
        ui.mostraRiepilogoProposta(proposta);
        ui.stampaSuccesso("Proposta valida salvata. Puoi pubblicarla dal menu 'Pubblicare una proposta di iniziativa'.");
        ui.newLine();
        ui.pausa();
    }

    @Override
    public void mostraPropostaPubblicata(Proposta proposta) {
        ui.stampaSuccesso("Proposta pubblicata in bacheca!");
        ui.newLine();
        ui.pausa();
    }

    private void stampaElencoProposteConStato(List<Proposta> proposte) {
        int indice = 1;
        for (Proposta proposta : proposte) {
            String titolo = proposta.getValoriCampi().getOrDefault(AppConstants.CAMPO_TITOLO, "Senza Titolo");
            String stato = proposta.getStato().name();
            ui.stampa(String.format(" %d) %s [%s] (Data evento: %s)",
                    indice++, titolo, stato, proposta.getDataEvento()));
        }
        ui.newLine();
    }

    private String labelPropostaArchivio(Proposta proposta) {
        return proposta.getValoriCampi().getOrDefault(AppConstants.CAMPO_TITOLO, "(senza titolo)")
                + " - " + proposta.getCategoria().getNome();
    }

    private void menuDettagliProposta(Proposta proposta) {
        String titolo = proposta.getValoriCampi().getOrDefault(AppConstants.CAMPO_TITOLO, "?");
        while (true) {
            ui.stampaMenu("PROPOSTA - " + titolo, MENU_DETTAGLI_PROPOSTA, "Torna");
            int scelta = ui.acquisisciIntero("Scelta: ", 0, MENU_DETTAGLI_PROPOSTA.length);
            switch (scelta) {
                case 1:
                    ui.mostraRiepilogoProposta(proposta);
                    ui.pausaConSpaziatura();
                    break;
                case 2:
                    ui.mostraAderenti(proposta.getListaAderenti());
                    ui.pausaConSpaziatura();
                    break;
                case 3:
                    ui.mostraCronologiaStati(proposta.getStateHistory());
                    ui.pausaConSpaziatura();
                    break;
                case 0:
                    return;
            }
        }
    }
}
