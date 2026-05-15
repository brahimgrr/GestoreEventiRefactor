package it.unibs.ingsoft.presentation.controller;

import it.unibs.ingsoft.application.ConfiguratoreService;
import it.unibs.ingsoft.application.catalogo.dto.CatalogoOperationResult;
import it.unibs.ingsoft.domain.model.catalogo.Campo;
import it.unibs.ingsoft.domain.model.catalogo.Categoria;
import it.unibs.ingsoft.domain.model.proposta.Proposta;
import it.unibs.ingsoft.shared.error.FailureException;
import it.unibs.ingsoft.presentation.view.interfaces.configuratore.IConfiguratoreViewFacade;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class ConfiguratoreController {
    private final IConfiguratoreViewFacade view;
    private final ConfiguratoreService configuratoreService;

    public ConfiguratoreController(
            IConfiguratoreViewFacade view,
            ConfiguratoreService configuratoreService) {
        this.view = Objects.requireNonNull(view);
        this.configuratoreService = Objects.requireNonNull(configuratoreService);
    }

    public void run() {
        while (configuratoreService.isPrimaConfigurazioneNecessaria()) {
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
                        configuratoreService.getCampiBase(),
                        configuratoreService.getCampiComuni(),
                        configuratoreService.getCategorie());
                case CREA_PROPOSTA -> creaProposta();
                case PUBBLICA_PROPOSTA -> pubblicaProposta();
                case BACHECA -> view.mostraBacheca(configuratoreService.getBachecaPerCategoria());
                case RITIRA_PROPOSTA -> ritiraProposta();
                case ARCHIVIO -> view.mostraArchivioProposte(configuratoreService.getPropostePerStato());
                case IMPORTA -> importaDati();
                case LOGOUT -> {
                    configuratoreService.clearProposteValide();
                    return;
                }
            }
        }
    }

    private void configuraCampiBase() {
        view.acquisisciCampiBaseExtra(configuratoreService.getCampiBasePredefiniti())
                .ifPresentOrElse(
                        extra -> esegui(
                                () -> configuratoreService.configuraCampiBase(extra),
                                () -> view.mostraEsitoCatalogo(CatalogoOperationResult.SUCCESSO)),
                        view::mostraOperazioneAnnullata);
    }

    private void gestisciCategorie() {
        while (true) {
            switch (view.scegliAzioneCategorie(configuratoreService.getCategorie())) {
                case CREA -> view.acquisisciNomeCategoria()
                        .ifPresent(nome -> esegui(
                                () -> configuratoreService.createCategoria(nome),
                                () -> view.mostraEsitoCatalogo(CatalogoOperationResult.SUCCESSO)));
                case RIMUOVI -> rimuoviCategoria();
                case CAMPI_SPECIFICI -> view.selezionaCategoriaPerCampiSpecifici(configuratoreService.getCategorie())
                        .ifPresent(this::gestisciCampiSpecifici);
                case TORNA -> {
                    return;
                }
            }
        }
    }

    private void rimuoviCategoria() {
        view.selezionaCategoriaDaRimuovere(configuratoreService.getCategorie())
                .filter(view::confermaRimozioneCategoria)
                .ifPresent(categoria -> view.mostraEsitoCatalogo(
                        configuratoreService.rimuoviCategoria(categoria.getNome())));
    }

    private void gestisciCampiComuni() {
        while (true) {
            switch (view.scegliAzioneCampiComuni(configuratoreService.getCampiComuni())) {
                case AGGIUNGI -> view.acquisisciNuovoCampo()
                        .ifPresent(request -> esegui(
                                () -> configuratoreService.addCampoComune(request),
                                () -> view.mostraEsitoCatalogo(CatalogoOperationResult.SUCCESSO)));
                case RIMUOVI -> rimuoviCampoComune();
                case CAMBIA_OBBLIGATORIETA -> view.acquisisciObbligatorietaCampo(configuratoreService.getCampiComuni())
                        .ifPresent(request -> view.mostraEsitoCatalogo(
                                configuratoreService.setObbligatorietaCampoComune(request)));
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
                                () -> configuratoreService.addCampoSpecifico(categoria.getNome(), request),
                                () -> view.mostraEsitoCatalogo(CatalogoOperationResult.SUCCESSO)));
                case RIMUOVI -> rimuoviCampoSpecifico(categoria);
                case CAMBIA_OBBLIGATORIETA -> view.acquisisciObbligatorietaCampo(categoria.getCampiSpecifici())
                        .ifPresent(request -> view.mostraEsitoCatalogo(
                                configuratoreService.setObbligatorietaCampoSpecifico(categoria.getNome(), request)));
                case TORNA -> {
                    return;
                }
            }
        }
    }

    private void rimuoviCampoComune() {
        view.selezionaCampoDaRimuovere(configuratoreService.getCampiComuni())
                .filter(view::confermaRimozioneCampo)
                .map(Campo::getNome)
                .ifPresent(nome -> view.mostraEsitoCatalogo(configuratoreService.rimuoviCampoComune(nome)));
    }

    private void rimuoviCampoSpecifico(Categoria categoria) {
        view.selezionaCampoDaRimuovere(categoria.getCampiSpecifici())
                .filter(view::confermaRimozioneCampo)
                .map(Campo::getNome)
                .ifPresent(nome -> view.mostraEsitoCatalogo(
                        configuratoreService.rimuoviCampoSpecifico(categoria.getNome(), nome)));
    }

    private void creaProposta() {
        view.selezionaCategoriaPerProposta(configuratoreService.getCategorie())
                .ifPresent(categoria -> {
                    Proposta proposta = configuratoreService.creaProposta(categoria);
                    view.acquisisciValoriProposta(proposta, configuratoreService::validaCampo)
                            .ifPresentOrElse(
                                    valori -> validaESalvaProposta(proposta, valori),
                                    view::mostraOperazioneAnnullata);
                });
    }

    private void validaESalvaProposta(Proposta proposta, Map<String, String> valori) {
        var result = configuratoreService.applicaValoriEValida(proposta, valori);
        while (!result.valida()) {
            Optional<Map<String, String>> correzioni =
                    view.correggiValoriProposta(proposta, result, configuratoreService::validaCampo);
            if (correzioni.isEmpty()) {
                return;
            }
            result = configuratoreService.applicaValoriEValida(proposta, correzioni.get());
        }
        esegui(
                () -> configuratoreService.salvaProposta(proposta),
                () -> view.mostraPropostaSalvata(proposta));
    }

    private void pubblicaProposta() {
        view.selezionaPropostaDaPubblicare(configuratoreService.getProposteValide())
                .filter(view::confermaPubblicazione)
                .ifPresent(proposta -> esegui(
                        () -> configuratoreService.pubblicaProposta(proposta),
                        () -> view.mostraPropostaPubblicata(proposta)));
    }

    private void ritiraProposta() {
        view.selezionaPropostaDaRitirare(configuratoreService.getProposteRitirabili())
                .filter(view::confermaRitiro)
                .ifPresent(proposta -> esegui(
                        () -> configuratoreService.ritiraProposta(proposta),
                        () -> view.mostraEsitoCatalogo(CatalogoOperationResult.SUCCESSO)));
    }

    private void importaDati() {
        view.acquisisciPercorsoImportazione()
                .ifPresent(path -> {
                    try {
                        view.mostraRisultatoImportazione(configuratoreService.importa(path));
                    } catch (FailureException e) {
                        view.mostraErrore(e.failure());
                    }
                });
    }

    private void esegui(Runnable action, Runnable onSuccess) {
        try {
            action.run();
            onSuccess.run();
        } catch (FailureException e) {
            view.mostraErrore(e.failure());
        }
    }
}
