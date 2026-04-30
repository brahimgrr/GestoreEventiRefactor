package it.unibs.ingsoft.presentation.controller;

import it.unibs.ingsoft.application.CatalogoOperationResult;
import it.unibs.ingsoft.application.CatalogoService;
import it.unibs.ingsoft.application.PropostaService;
import it.unibs.ingsoft.application.PropostaValidationResult;
import it.unibs.ingsoft.application.StateTransitionService;
import it.unibs.ingsoft.application.batch.BatchImportService;
import it.unibs.ingsoft.domain.Campo;
import it.unibs.ingsoft.domain.Categoria;
import it.unibs.ingsoft.domain.Proposta;
import it.unibs.ingsoft.presentation.view.contract.ConfiguratoreView;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class ConfiguratoreController {
    private final ConfiguratoreView view;
    private final CatalogoService catalogoService;
    private final PropostaService propostaService;
    private final StateTransitionService stateTransitionService;
    private final BatchImportService batchImportService;

    public ConfiguratoreController(
            ConfiguratoreView view,
            CatalogoService catalogoService,
            PropostaService propostaService,
            StateTransitionService stateTransitionService,
            BatchImportService batchImportService) {
        this.view = Objects.requireNonNull(view);
        this.catalogoService = Objects.requireNonNull(catalogoService);
        this.propostaService = Objects.requireNonNull(propostaService);
        this.stateTransitionService = Objects.requireNonNull(stateTransitionService);
        this.batchImportService = Objects.requireNonNull(batchImportService);
    }

    public void run() {
        while (catalogoService.isPrimaConfigurazioneNecessaria()) {
            view.mostraPrimaConfigurazioneRichiesta();
            configuraCampiBase();
        }
        menuPrincipale();
    }

    private void menuPrincipale() {
        while (true) {
            switch (view.scegliAzionePrincipale()) {
                case CAMPI_COMUNI -> gestisciCampiComuni();
                case CATEGORIE -> gestisciCategorie();
                case VISUALIZZA -> view.mostraCatalogo(
                        catalogoService.getCampiBase(),
                        catalogoService.getCampiComuni(),
                        catalogoService.getCategorie());
                case CREA_PROPOSTA -> creaProposta();
                case PUBBLICA_PROPOSTA -> pubblicaProposta();
                case BACHECA -> view.mostraBacheca(propostaService.getBachecaPerCategoria());
                case RITIRA_PROPOSTA -> ritiraProposta();
                case ARCHIVIO -> view.mostraArchivioProposte(propostaService.getPropostePerStato());
                case IMPORTA -> importaDati();
                case LOGOUT -> {
                    return;
                }
            }
        }
    }

    private void configuraCampiBase() {
        view.acquisisciCampiBaseExtra(catalogoService.getCampiBasePredefiniti())
                .ifPresentOrElse(
                        extra -> esegui(
                                () -> catalogoService.configuraCampiBase(extra),
                                () -> view.mostraEsitoCatalogo(CatalogoOperationResult.SUCCESSO)),
                        view::mostraOperazioneAnnullata);
    }

    private void gestisciCategorie() {
        while (true) {
            switch (view.scegliAzioneCategorie(catalogoService.getCategorie())) {
                case CREA -> view.acquisisciNomeCategoria()
                        .ifPresent(nome -> esegui(
                                () -> catalogoService.createCategoria(nome),
                                () -> view.mostraEsitoCatalogo(CatalogoOperationResult.SUCCESSO)));
                case RIMUOVI -> rimuoviCategoria();
                case CAMPI_SPECIFICI -> view.selezionaCategoriaPerCampiSpecifici(catalogoService.getCategorie())
                        .ifPresent(this::gestisciCampiSpecifici);
                case TORNA -> {
                    return;
                }
            }
        }
    }

    private void rimuoviCategoria() {
        view.selezionaCategoriaDaRimuovere(catalogoService.getCategorie())
                .filter(view::confermaRimozioneCategoria)
                .ifPresent(categoria -> view.mostraEsitoCatalogo(
                        catalogoService.rimuoviCategoria(categoria.getNome())));
    }

    private void gestisciCampiComuni() {
        while (true) {
            switch (view.scegliAzioneCampiComuni(catalogoService.getCampiComuni())) {
                case AGGIUNGI -> view.acquisisciNuovoCampo()
                        .ifPresent(request -> esegui(
                                () -> catalogoService.addCampoComune(request),
                                () -> view.mostraEsitoCatalogo(CatalogoOperationResult.SUCCESSO)));
                case RIMUOVI -> rimuoviCampoComune();
                case CAMBIA_OBBLIGATORIETA -> view.acquisisciObbligatorietaCampo(catalogoService.getCampiComuni())
                        .ifPresent(request -> view.mostraEsitoCatalogo(
                                catalogoService.setObbligatorietaCampoComune(request)));
                case TORNA -> {
                    return;
                }
            }
        }
    }

    private void gestisciCampiSpecifici(Categoria categoria) {
        while (true) {
            switch (view.scegliAzioneCampiSpecifici(categoria)) {
                case AGGIUNGI -> view.acquisisciNuovoCampo()
                        .ifPresent(request -> esegui(
                                () -> catalogoService.addCampoSpecifico(categoria.getNome(), request),
                                () -> view.mostraEsitoCatalogo(CatalogoOperationResult.SUCCESSO)));
                case RIMUOVI -> rimuoviCampoSpecifico(categoria);
                case CAMBIA_OBBLIGATORIETA -> view.acquisisciObbligatorietaCampo(categoria.getCampiSpecifici())
                        .ifPresent(request -> view.mostraEsitoCatalogo(
                                catalogoService.setObbligatorietaCampoSpecifico(categoria.getNome(), request)));
                case TORNA -> {
                    return;
                }
            }
        }
    }

    private void rimuoviCampoComune() {
        view.selezionaCampoDaRimuovere(catalogoService.getCampiComuni())
                .filter(view::confermaRimozioneCampo)
                .map(Campo::getNome)
                .ifPresent(nome -> view.mostraEsitoCatalogo(catalogoService.rimuoviCampoComune(nome)));
    }

    private void rimuoviCampoSpecifico(Categoria categoria) {
        view.selezionaCampoDaRimuovere(categoria.getCampiSpecifici())
                .filter(view::confermaRimozioneCampo)
                .map(Campo::getNome)
                .ifPresent(nome -> view.mostraEsitoCatalogo(
                        catalogoService.rimuoviCampoSpecifico(categoria.getNome(), nome)));
    }

    private void creaProposta() {
        view.selezionaCategoriaPerProposta(catalogoService.getCategorie())
                .ifPresent(categoria -> {
                    Proposta proposta = propostaService.creaProposta(
                            categoria,
                            catalogoService.getCampiBase(),
                            catalogoService.getCampiComuni());
                    view.acquisisciValoriProposta(proposta, propostaService::validaCampo)
                            .ifPresentOrElse(
                                    valori -> validaESalvaProposta(proposta, valori),
                                    view::mostraOperazioneAnnullata);
                });
    }

    private void validaESalvaProposta(Proposta proposta, Map<String, String> valori) {
        PropostaValidationResult result = propostaService.applicaValoriEValida(proposta, valori);
        while (!result.valida()) {
            Optional<Map<String, String>> correzioni =
                    view.correggiValoriProposta(proposta, result, propostaService::validaCampo);
            if (correzioni.isEmpty()) {
                return;
            }
            result = propostaService.applicaValoriEValida(proposta, correzioni.get());
        }
        esegui(
                () -> propostaService.salvaProposta(proposta),
                () -> view.mostraPropostaSalvata(proposta));
    }

    private void pubblicaProposta() {
        view.selezionaPropostaDaPubblicare(propostaService.getProposteValide())
                .filter(view::confermaPubblicazione)
                .ifPresent(proposta -> esegui(
                        () -> {
                            propostaService.pubblicaProposta(proposta);
                            propostaService.rimuoviPropostaValida(proposta);
                        },
                        () -> view.mostraPropostaPubblicata(proposta)));
    }

    private void ritiraProposta() {
        view.selezionaPropostaDaRitirare(propostaService.getProposteRitirabili())
                .filter(view::confermaRitiro)
                .ifPresent(proposta -> esegui(
                        () -> stateTransitionService.ritiraProposta(proposta),
                        () -> view.mostraEsitoCatalogo(CatalogoOperationResult.SUCCESSO)));
    }

    private void importaDati() {
        view.acquisisciPercorsoImportazione()
                .ifPresent(path -> {
                    try {
                        view.mostraRisultatoImportazione(batchImportService.importa(path));
                    } catch (IOException e) {
                        view.mostraErrore(e);
                    }
                });
    }

    private void esegui(Runnable action, Runnable onSuccess) {
        try {
            action.run();
            onSuccess.run();
        } catch (IllegalArgumentException | IllegalStateException e) {
            view.mostraErrore(e);
        }
    }
}
