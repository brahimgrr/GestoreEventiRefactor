package it.unibs.ingsoft.presentation.view.interfaces;

import it.unibs.ingsoft.application.proposta.dto.PropostaValidationResult;
import it.unibs.ingsoft.application.batch.ImportResult;
import it.unibs.ingsoft.application.catalogo.dto.CampoBaseExtraRequest;
import it.unibs.ingsoft.application.catalogo.dto.CampoDefinitionRequest;
import it.unibs.ingsoft.application.catalogo.dto.CampoObbligatorietaRequest;
import it.unibs.ingsoft.application.catalogo.dto.CatalogoOperationResult;
import it.unibs.ingsoft.domain.Campo;
import it.unibs.ingsoft.domain.Categoria;
import it.unibs.ingsoft.domain.Proposta;
import it.unibs.ingsoft.domain.StatoProposta;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IConfiguratoreView {
    enum MainAction {
        CAMPI_COMUNI,
        CATEGORIE,
        VISUALIZZA,
        CREA_PROPOSTA,
        PUBBLICA_PROPOSTA,
        BACHECA,
        RITIRA_PROPOSTA,
        ARCHIVIO,
        IMPORTA,
        LOGOUT
    }

    enum CategoryAction {
        CREA,
        RIMUOVI,
        CAMPI_SPECIFICI,
        TORNA
    }

    enum FieldAction {
        AGGIUNGI,
        RIMUOVI,
        CAMBIA_OBBLIGATORIETA,
        TORNA
    }

    MainAction scegliAzionePrincipale();

    CategoryAction scegliAzioneCategorie(List<Categoria> categorie);

    FieldAction scegliAzioneCampiComuni(List<Campo> campi);

    FieldAction scegliAzioneCampiSpecifici(Categoria categoria);

    Optional<List<CampoBaseExtraRequest>> acquisisciCampiBaseExtra(List<Campo> campiPredefiniti);

    Optional<String> acquisisciNomeCategoria();

    Optional<Categoria> selezionaCategoriaDaRimuovere(List<Categoria> categorie);

    Optional<Categoria> selezionaCategoriaPerCampiSpecifici(List<Categoria> categorie);

    Optional<Categoria> selezionaCategoriaPerProposta(List<Categoria> categorie);

    boolean confermaRimozioneCategoria(Categoria categoria);

    Optional<CampoDefinitionRequest> acquisisciNuovoCampo();

    Optional<Campo> selezionaCampoDaRimuovere(List<Campo> campi);

    boolean confermaRimozioneCampo(Campo campo);

    Optional<CampoObbligatorietaRequest> acquisisciObbligatorietaCampo(List<Campo> campi);

    Optional<Proposta> selezionaPropostaDaRitirare(List<Proposta> proposte);

    boolean confermaRitiro(Proposta proposta);

    Optional<Proposta> selezionaPropostaDaPubblicare(List<Proposta> proposte);

    boolean confermaPubblicazione(Proposta proposta);

    Optional<Path> acquisisciPercorsoImportazione();

    Optional<Map<String, String>> acquisisciValoriProposta(Proposta proposta, ProposalFieldValidator validator);

    Optional<Map<String, String>> correggiValoriProposta(
            Proposta proposta,
            PropostaValidationResult result,
            ProposalFieldValidator validator);

    void mostraPrimaConfigurazioneRichiesta();

    void mostraCatalogo(List<Campo> base, List<Campo> comuni, List<Categoria> categorie);

    void mostraBacheca(Map<String, List<Proposta>> bacheca);

    void mostraArchivioProposte(Map<StatoProposta, List<Proposta>> archivio);

    void mostraRisultatoImportazione(ImportResult result);

    void mostraErrore(Exception e);

    void mostraEsitoCatalogo(CatalogoOperationResult result);

    void mostraOperazioneAnnullata();

    void mostraPropostaSalvata(Proposta proposta);

    void mostraPropostaPubblicata(Proposta proposta);
}
