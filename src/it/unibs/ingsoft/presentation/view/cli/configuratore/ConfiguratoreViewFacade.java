package it.unibs.ingsoft.presentation.view.cli.configuratore;

import it.unibs.ingsoft.application.batch.dto.ImportResult;
import it.unibs.ingsoft.application.catalogo.dto.CampoBaseExtraRequest;
import it.unibs.ingsoft.application.catalogo.dto.CampoDefinitionRequest;
import it.unibs.ingsoft.application.catalogo.dto.CampoObbligatorietaRequest;
import it.unibs.ingsoft.application.catalogo.dto.CatalogoOperationResult;
import it.unibs.ingsoft.application.proposta.dto.PropostaValidationResult;
import it.unibs.ingsoft.domain.model.catalogo.Campo;
import it.unibs.ingsoft.domain.model.catalogo.Categoria;
import it.unibs.ingsoft.domain.model.proposta.Proposta;
import it.unibs.ingsoft.domain.model.proposta.StatoProposta;
import it.unibs.ingsoft.shared.error.Failure;
import it.unibs.ingsoft.presentation.view.interfaces.configuratore.IConfiguratoreViewFacade;
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
import it.unibs.ingsoft.presentation.view.interfaces.configuratore.proposta.ProposalFieldValidator;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class ConfiguratoreViewFacade implements IConfiguratoreViewFacade {
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

    public ConfiguratoreViewFacade(
            IConfiguratoreView mainView,
            ICatalogoConfigView catalogoView,
            ICategoriaConfigView categoriaView,
            ICampoConfigView campoView,
            IPropostaCreationView propostaCreationView,
            IPropostaPublicationView propostaPublicationView,
            IPropostaLifecycleView propostaLifecycleView,
            IPropostaBrowsingView propostaBrowsingView,
            IBatchImportView batchImportView,
            IConfiguratoreFeedbackView feedbackView) {
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
    }

    @Override
    public MainAction scegliAzionePrincipale() {
        return mainView.scegliAzionePrincipale();
    }

    @Override
    public Optional<List<CampoBaseExtraRequest>> acquisisciCampiBaseExtra(List<Campo> campiPredefiniti) {
        return catalogoView.acquisisciCampiBaseExtra(campiPredefiniti);
    }

    @Override
    public void mostraPrimaConfigurazioneRichiesta() {
        catalogoView.mostraPrimaConfigurazioneRichiesta();
    }

    @Override
    public void mostraCatalogo(List<Campo> base, List<Campo> comuni, List<Categoria> categorie) {
        catalogoView.mostraCatalogo(base, comuni, categorie);
    }

    @Override
    public void mostraEsitoCatalogo(CatalogoOperationResult result) {
        catalogoView.mostraEsitoCatalogo(result);
    }

    @Override
    public CategoryAction scegliAzioneCategorie(List<Categoria> categorie) {
        return categoriaView.scegliAzioneCategorie(categorie);
    }

    @Override
    public Optional<String> acquisisciNomeCategoria() {
        return categoriaView.acquisisciNomeCategoria();
    }

    @Override
    public Optional<Categoria> selezionaCategoriaDaRimuovere(List<Categoria> categorie) {
        return categoriaView.selezionaCategoriaDaRimuovere(categorie);
    }

    @Override
    public Optional<Categoria> selezionaCategoriaPerCampiSpecifici(List<Categoria> categorie) {
        return categoriaView.selezionaCategoriaPerCampiSpecifici(categorie);
    }

    @Override
    public boolean confermaRimozioneCategoria(Categoria categoria) {
        return categoriaView.confermaRimozioneCategoria(categoria);
    }

    @Override
    public FieldAction scegliAzioneCampiComuni(List<Campo> campi) {
        return campoView.scegliAzioneCampiComuni(campi);
    }

    @Override
    public FieldAction scegliAzioneCampiSpecifici(Categoria categoria) {
        return campoView.scegliAzioneCampiSpecifici(categoria);
    }

    @Override
    public Optional<CampoDefinitionRequest> acquisisciNuovoCampo() {
        return campoView.acquisisciNuovoCampo();
    }

    @Override
    public Optional<Campo> selezionaCampoDaRimuovere(List<Campo> campi) {
        return campoView.selezionaCampoDaRimuovere(campi);
    }

    @Override
    public boolean confermaRimozioneCampo(Campo campo) {
        return campoView.confermaRimozioneCampo(campo);
    }

    @Override
    public Optional<CampoObbligatorietaRequest> acquisisciObbligatorietaCampo(List<Campo> campi) {
        return campoView.acquisisciObbligatorietaCampo(campi);
    }

    @Override
    public Optional<Categoria> selezionaCategoriaPerProposta(List<Categoria> categorie) {
        return propostaCreationView.selezionaCategoriaPerProposta(categorie);
    }

    @Override
    public Optional<Map<String, String>> acquisisciValoriProposta(
            Proposta proposta,
            ProposalFieldValidator validator) {
        return propostaCreationView.acquisisciValoriProposta(proposta, validator);
    }

    @Override
    public Optional<Map<String, String>> correggiValoriProposta(
            Proposta proposta,
            PropostaValidationResult result,
            ProposalFieldValidator validator) {
        return propostaCreationView.correggiValoriProposta(proposta, result, validator);
    }

    @Override
    public void mostraPropostaSalvata(Proposta proposta) {
        propostaCreationView.mostraPropostaSalvata(proposta);
    }

    @Override
    public Optional<Proposta> selezionaPropostaDaPubblicare(List<Proposta> proposte) {
        return propostaPublicationView.selezionaPropostaDaPubblicare(proposte);
    }

    @Override
    public boolean confermaPubblicazione(Proposta proposta) {
        return propostaPublicationView.confermaPubblicazione(proposta);
    }

    @Override
    public void mostraPropostaPubblicata(Proposta proposta) {
        propostaPublicationView.mostraPropostaPubblicata(proposta);
    }

    @Override
    public Optional<Proposta> selezionaPropostaDaRitirare(List<Proposta> proposte) {
        return propostaLifecycleView.selezionaPropostaDaRitirare(proposte);
    }

    @Override
    public boolean confermaRitiro(Proposta proposta) {
        return propostaLifecycleView.confermaRitiro(proposta);
    }

    @Override
    public void mostraBacheca(Map<String, List<Proposta>> bacheca) {
        propostaBrowsingView.mostraBacheca(bacheca);
    }

    @Override
    public void mostraArchivioProposte(Map<StatoProposta, List<Proposta>> archivio) {
        propostaBrowsingView.mostraArchivioProposte(archivio);
    }

    @Override
    public Optional<Path> acquisisciPercorsoImportazione() {
        return batchImportView.acquisisciPercorsoImportazione();
    }

    @Override
    public void mostraRisultatoImportazione(ImportResult result) {
        batchImportView.mostraRisultatoImportazione(result);
    }

    @Override
    public void mostraErrore(Failure failure) {
        feedbackView.mostraErrore(failure);
    }

    @Override
    public void mostraOperazioneAnnullata() {
        feedbackView.mostraOperazioneAnnullata();
    }
}
