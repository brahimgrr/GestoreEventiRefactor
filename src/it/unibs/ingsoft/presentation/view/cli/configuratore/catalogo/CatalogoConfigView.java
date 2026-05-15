package it.unibs.ingsoft.presentation.view.cli.configuratore.catalogo;

import it.unibs.ingsoft.application.catalogo.dto.CampoBaseExtraRequest;
import it.unibs.ingsoft.application.catalogo.dto.CatalogoOperationResult;
import it.unibs.ingsoft.domain.model.catalogo.Campo;
import it.unibs.ingsoft.domain.model.catalogo.Categoria;
import it.unibs.ingsoft.domain.model.catalogo.TipoDato;
import it.unibs.ingsoft.presentation.view.cli.configuratore.campo.CampoRenderer;
import it.unibs.ingsoft.presentation.view.cli.configuratore.categoria.CategoriaRenderer;
import it.unibs.ingsoft.presentation.view.interfaces.common.IAppView;
import it.unibs.ingsoft.presentation.view.interfaces.configuratore.catalogo.ICatalogoConfigView;
import it.unibs.ingsoft.presentation.view.interfaces.common.OperationCancelledException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class CatalogoConfigView implements ICatalogoConfigView {
    private final IAppView ui;
    private final CampoRenderer campoRenderer;
    private final CategoriaRenderer categoriaRenderer;

    public CatalogoConfigView(
            IAppView ui,
            CampoRenderer campoRenderer,
            CategoriaRenderer categoriaRenderer) {
        this.ui = ui;
        this.campoRenderer = campoRenderer;
        this.categoriaRenderer = categoriaRenderer;
    }

    @Override
    public Optional<List<CampoBaseExtraRequest>> acquisisciCampiBaseExtra(List<Campo> campiPredefiniti) {
        ui.header("PRIMA CONFIGURAZIONE - Campi base");
        ui.newLine();
        ui.stampa("I seguenti campi base sono gia presenti (definiti dalla traccia):");
        campoRenderer.stampaCampi(campiPredefiniti);
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
                TipoDato tipoDato = campoRenderer.acquisisciTipoDato("Tipo per \"" + nome + "\":");
                richieste.add(new CampoBaseExtraRequest(nome, tipoDato));
            }
            return Optional.of(richieste);
        } catch (OperationCancelledException e) {
            mostraOperazioneAnnullata();
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
        campoRenderer.stampaCampi(base);
        ui.stampaSezione("Campi COMUNI");
        campoRenderer.stampaCampi(comuni);
        ui.stampaSezione("Categorie");
        categoriaRenderer.stampaCategorie(categorie);
        ui.newLine();
        ui.pausaConSpaziatura();
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

    private void mostraOperazioneAnnullata() {
        ui.stampaInfo("Operazione annullata.");
    }
}
