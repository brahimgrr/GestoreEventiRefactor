package it.unibs.ingsoft.presentation.controller;

import it.unibs.ingsoft.application.ConfiguratoreService;
import it.unibs.ingsoft.application.catalogo.dto.CatalogoOperationResult;
import it.unibs.ingsoft.domain.Campo;
import it.unibs.ingsoft.domain.Categoria;
import it.unibs.ingsoft.domain.Proposta;
import it.unibs.ingsoft.domain.error.DomainException;
import it.unibs.ingsoft.presentation.view.interfaces.configuratore.batch.IBatchImportView;
import it.unibs.ingsoft.presentation.view.interfaces.configuratore.campo.ICampoConfigView;
import it.unibs.ingsoft.presentation.view.interfaces.configuratore.catalogo.ICatalogoConfigView;
import it.unibs.ingsoft.presentation.view.interfaces.configuratore.categoria.ICategoriaConfigView;
import it.unibs.ingsoft.presentation.view.interfaces.configuratore.error.IConfiguratoreFeedbackView;
import it.unibs.ingsoft.presentation.view.interfaces.configuratore.menu.IConfiguratoreView;
import it.unibs.ingsoft.presentation.view.interfaces.configuratore.proposta.IPropostaBrowsingView;
import it.unibs.ingsoft.presentation.view.interfaces.configuratore.proposta.IPropostaCreationView;
import it.unibs.ingsoft.presentation.view.interfaces.configuratore.proposta.IPropostaLifecycleView;
import it.unibs.ingsoft.presentation.view.interfaces.configuratore.proposta.IPropostaPublicationView;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class ConfiguratoreController {
    private final IConfiguratoreView mainView;
    private final ICatalogoConfigView catalogoView;
    private final ICategoriaConfigView categoriaView;
    private final ICampoConfigView campoView;
    private final IPropostaCreationView propostaCreationView;
    private final IPropostaPublicationView propostaPublicationView;
    private final IPropostaLifecycleView propostaLifecycleView;
    private final IPropostaBrowsingView propostaBrowsingView;
    private final IBatchImportView batchImportView;
    private final IConfiguratoreFeedbackView feedbackView;
    private final ConfiguratoreService configuratoreService;

    public ConfiguratoreController(
            IConfiguratoreView mainView,
            ICatalogoConfigView catalogoView,
            ICategoriaConfigView categoriaView,
            ICampoConfigView campoView,
            IPropostaCreationView propostaCreationView,
            IPropostaPublicationView propostaPublicationView,
            IPropostaLifecycleView propostaLifecycleView,
            IPropostaBrowsingView propostaBrowsingView,
            IBatchImportView batchImportView,
            IConfiguratoreFeedbackView feedbackView,
            ConfiguratoreService configuratoreService) {
        this.mainView = Objects.requireNonNull(mainView);
        this.catalogoView = Objects.requireNonNull(catalogoView);
        this.categoriaView = Objects.requireNonNull(categoriaView);
        this.campoView = Objects.requireNonNull(campoView);
        this.propostaCreationView = Objects.requireNonNull(propostaCreationView);
        this.propostaPublicationView = Objects.requireNonNull(propostaPublicationView);
        this.propostaLifecycleView = Objects.requireNonNull(propostaLifecycleView);
        this.propostaBrowsingView = Objects.requireNonNull(propostaBrowsingView);
        this.batchImportView = Objects.requireNonNull(batchImportView);
        this.feedbackView = Objects.requireNonNull(feedbackView);
        this.configuratoreService = Objects.requireNonNull(configuratoreService);
    }

    public void run() {
        while (configuratoreService.isPrimaConfigurazioneNecessaria()) {
            catalogoView.mostraPrimaConfigurazioneRichiesta();
            configuraCampiBase();
        }
        menuPrincipale();
    }

    private void menuPrincipale() {
        while (true) {
            switch (mainView.scegliAzionePrincipale()) {
                case CAMPI_COMUNI -> gestisciCampiComuni();
                case CATEGORIE -> gestisciCategorie();
                case VISUALIZZA -> catalogoView.mostraCatalogo(
                        configuratoreService.getCampiBase(),
                        configuratoreService.getCampiComuni(),
                        configuratoreService.getCategorie());
                case CREA_PROPOSTA -> creaProposta();
                case PUBBLICA_PROPOSTA -> pubblicaProposta();
                case BACHECA -> propostaBrowsingView.mostraBacheca(configuratoreService.getBachecaPerCategoria());
                case RITIRA_PROPOSTA -> ritiraProposta();
                case ARCHIVIO -> propostaBrowsingView.mostraArchivioProposte(configuratoreService.getPropostePerStato());
                case IMPORTA -> importaDati();
                case LOGOUT -> {
                    configuratoreService.clearProposteValide();
                    return;
                }
            }
        }
    }

    private void configuraCampiBase() {
        catalogoView.acquisisciCampiBaseExtra(configuratoreService.getCampiBasePredefiniti())
                .ifPresentOrElse(
                        extra -> esegui(
                                () -> configuratoreService.configuraCampiBase(extra),
                                () -> catalogoView.mostraEsitoCatalogo(CatalogoOperationResult.SUCCESSO)),
                        feedbackView::mostraOperazioneAnnullata);
    }

    private void gestisciCategorie() {
        while (true) {
            switch (categoriaView.scegliAzioneCategorie(configuratoreService.getCategorie())) {
                case CREA -> categoriaView.acquisisciNomeCategoria()
                        .ifPresent(nome -> esegui(
                                () -> configuratoreService.createCategoria(nome),
                                () -> catalogoView.mostraEsitoCatalogo(CatalogoOperationResult.SUCCESSO)));
                case RIMUOVI -> rimuoviCategoria();
                case CAMPI_SPECIFICI -> categoriaView.selezionaCategoriaPerCampiSpecifici(configuratoreService.getCategorie())
                        .ifPresent(this::gestisciCampiSpecifici);
                case TORNA -> {
                    return;
                }
            }
        }
    }

    private void rimuoviCategoria() {
        categoriaView.selezionaCategoriaDaRimuovere(configuratoreService.getCategorie())
                .filter(categoriaView::confermaRimozioneCategoria)
                .ifPresent(categoria -> catalogoView.mostraEsitoCatalogo(
                        configuratoreService.rimuoviCategoria(categoria.getNome())));
    }

    private void gestisciCampiComuni() {
        while (true) {
            switch (campoView.scegliAzioneCampiComuni(configuratoreService.getCampiComuni())) {
                case AGGIUNGI -> campoView.acquisisciNuovoCampo()
                        .ifPresent(request -> esegui(
                                () -> configuratoreService.addCampoComune(request),
                                () -> catalogoView.mostraEsitoCatalogo(CatalogoOperationResult.SUCCESSO)));
                case RIMUOVI -> rimuoviCampoComune();
                case CAMBIA_OBBLIGATORIETA -> campoView.acquisisciObbligatorietaCampo(configuratoreService.getCampiComuni())
                        .ifPresent(request -> catalogoView.mostraEsitoCatalogo(
                                configuratoreService.setObbligatorietaCampoComune(request)));
                case TORNA -> {
                    return;
                }
            }
        }
    }

    private void gestisciCampiSpecifici(Categoria categoria) {
        while (true) {
            switch (campoView.scegliAzioneCampiSpecifici(categoria)) {
                case AGGIUNGI -> campoView.acquisisciNuovoCampo()
                        .ifPresent(request -> esegui(
                                () -> configuratoreService.addCampoSpecifico(categoria.getNome(), request),
                                () -> catalogoView.mostraEsitoCatalogo(CatalogoOperationResult.SUCCESSO)));
                case RIMUOVI -> rimuoviCampoSpecifico(categoria);
                case CAMBIA_OBBLIGATORIETA -> campoView.acquisisciObbligatorietaCampo(categoria.getCampiSpecifici())
                        .ifPresent(request -> catalogoView.mostraEsitoCatalogo(
                                configuratoreService.setObbligatorietaCampoSpecifico(categoria.getNome(), request)));
                case TORNA -> {
                    return;
                }
            }
        }
    }

    private void rimuoviCampoComune() {
        campoView.selezionaCampoDaRimuovere(configuratoreService.getCampiComuni())
                .filter(campoView::confermaRimozioneCampo)
                .map(Campo::getNome)
                .ifPresent(nome -> catalogoView.mostraEsitoCatalogo(configuratoreService.rimuoviCampoComune(nome)));
    }

    private void rimuoviCampoSpecifico(Categoria categoria) {
        campoView.selezionaCampoDaRimuovere(categoria.getCampiSpecifici())
                .filter(campoView::confermaRimozioneCampo)
                .map(Campo::getNome)
                .ifPresent(nome -> catalogoView.mostraEsitoCatalogo(
                        configuratoreService.rimuoviCampoSpecifico(categoria.getNome(), nome)));
    }

    private void creaProposta() {
        propostaCreationView.selezionaCategoriaPerProposta(configuratoreService.getCategorie())
                .ifPresent(categoria -> {
                    Proposta proposta = configuratoreService.creaProposta(categoria);
                    propostaCreationView.acquisisciValoriProposta(proposta, configuratoreService::validaCampo)
                            .ifPresentOrElse(
                                    valori -> validaESalvaProposta(proposta, valori),
                                    feedbackView::mostraOperazioneAnnullata);
                });
    }

    private void validaESalvaProposta(Proposta proposta, Map<String, String> valori) {
        var result = configuratoreService.applicaValoriEValida(proposta, valori);
        while (!result.valida()) {
            Optional<Map<String, String>> correzioni =
                    propostaCreationView.correggiValoriProposta(proposta, result, configuratoreService::validaCampo);
            if (correzioni.isEmpty()) {
                return;
            }
            result = configuratoreService.applicaValoriEValida(proposta, correzioni.get());
        }
        esegui(
                () -> configuratoreService.salvaProposta(proposta),
                () -> propostaCreationView.mostraPropostaSalvata(proposta));
    }

    private void pubblicaProposta() {
        propostaPublicationView.selezionaPropostaDaPubblicare(configuratoreService.getProposteValide())
                .filter(propostaPublicationView::confermaPubblicazione)
                .ifPresent(proposta -> esegui(
                        () -> configuratoreService.pubblicaProposta(proposta),
                        () -> propostaPublicationView.mostraPropostaPubblicata(proposta)));
    }

    private void ritiraProposta() {
        propostaLifecycleView.selezionaPropostaDaRitirare(configuratoreService.getProposteRitirabili())
                .filter(propostaLifecycleView::confermaRitiro)
                .ifPresent(proposta -> esegui(
                        () -> configuratoreService.ritiraProposta(proposta),
                        () -> catalogoView.mostraEsitoCatalogo(CatalogoOperationResult.SUCCESSO)));
    }

    private void importaDati() {
        batchImportView.acquisisciPercorsoImportazione()
                .ifPresent(path -> {
                    try {
                        batchImportView.mostraRisultatoImportazione(configuratoreService.importa(path));
                    } catch (IOException | DomainException e) {
                        feedbackView.mostraErrore(e);
                    }
                });
    }

    private void esegui(Runnable action, Runnable onSuccess) {
        try {
            action.run();
            onSuccess.run();
        } catch (IllegalArgumentException | IllegalStateException e) {
            feedbackView.mostraErrore(e);
        }
    }
}
