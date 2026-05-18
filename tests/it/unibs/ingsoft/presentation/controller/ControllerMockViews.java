package it.unibs.ingsoft.presentation.controller;

import it.unibs.ingsoft.application.batch.dto.ImportResult;
import it.unibs.ingsoft.application.catalogo.dto.CampoBaseExtraRequest;
import it.unibs.ingsoft.application.catalogo.dto.CampoDefinitionRequest;
import it.unibs.ingsoft.application.catalogo.dto.CampoObbligatorietaRequest;
import it.unibs.ingsoft.application.catalogo.dto.CatalogoOperationResult;
import it.unibs.ingsoft.application.proposta.dto.PropostaValidationResult;
import it.unibs.ingsoft.domain.catalogo.Campo;
import it.unibs.ingsoft.domain.catalogo.Categoria;
import it.unibs.ingsoft.domain.catalogo.TipoDato;
import it.unibs.ingsoft.domain.notifica.Notifica;
import it.unibs.ingsoft.domain.proposta.Proposta;
import it.unibs.ingsoft.domain.proposta.StatoProposta;
import it.unibs.ingsoft.domain.shared.error.Failure;
import it.unibs.ingsoft.domain.utente.Fruitore;
import it.unibs.ingsoft.presentation.view.interfaces.configuratore.IConfiguratoreViewFacade;
import it.unibs.ingsoft.presentation.view.interfaces.configuratore.menu.IConfiguratoreView;
import it.unibs.ingsoft.presentation.view.interfaces.configuratore.proposta.ProposalFieldValidator;
import it.unibs.ingsoft.presentation.view.interfaces.fruitore.IFruitoreViewFacade;
import it.unibs.ingsoft.presentation.view.interfaces.fruitore.menu.IFruitoreView;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

final class MockConfiguratoreView implements IConfiguratoreViewFacade {
    final ArrayDeque<IConfiguratoreView.MainAction> mainActions = new ArrayDeque<>();
    final ArrayDeque<CategoryAction> categoryActions = new ArrayDeque<>();
    final ArrayDeque<FieldAction> commonFieldActions = new ArrayDeque<>();
    final ArrayDeque<FieldAction> specificFieldActions = new ArrayDeque<>();
    Optional<List<CampoBaseExtraRequest>> campiBaseExtra = Optional.of(List.of());
    Optional<String> nomeCategoria = Optional.of("Sport");
    Optional<CampoDefinitionRequest> nuovoCampo = Optional.empty();
    Optional<Map<String, String>> valoriProposta = Optional.empty();
    Optional<Map<String, String>> correzioniProposta = Optional.empty();
    Optional<Path> percorsoImportazione = Optional.empty();
    boolean confermaRimozioneCategoria = true;
    boolean confermaRimozioneCampo = true;
    boolean confermaPubblicazione = true;
    boolean confermaRitiro = true;
    int primaConfigurazioneRichiesta;
    int catalogoMostrato;
    int esitiCatalogo;
    int operazioniAnnullate;
    int erroriMostrati;
    int proposteSalvate;
    int propostePubblicate;
    int bachecheMostrate;
    int archiviMostrati;
    int risultatiImportazione;

    MockConfiguratoreView(IConfiguratoreView.MainAction... actions) {
        mainActions.addAll(Arrays.asList(actions));
    }

    @Override
    public IConfiguratoreView.MainAction scegliAzionePrincipale() {
        return mainActions.isEmpty() ? IConfiguratoreView.MainAction.LOGOUT : mainActions.removeFirst();
    }

    @Override
    public Optional<List<CampoBaseExtraRequest>> acquisisciCampiBaseExtra(List<Campo> campiPredefiniti) {
        return campiBaseExtra;
    }

    @Override
    public void mostraPrimaConfigurazioneRichiesta() {
        primaConfigurazioneRichiesta++;
    }

    @Override
    public void mostraCatalogo(List<Campo> base, List<Campo> comuni, List<Categoria> categorie) {
        catalogoMostrato++;
    }

    @Override
    public void mostraEsitoCatalogo(CatalogoOperationResult result) {
        esitiCatalogo += result == null ? 0 : 1;
    }

    @Override
    public CategoryAction scegliAzioneCategorie(List<Categoria> categorie) {
        return categoryActions.isEmpty() ? CategoryAction.TORNA : categoryActions.removeFirst();
    }

    @Override
    public Optional<String> acquisisciNomeCategoria() {
        return nomeCategoria;
    }

    @Override
    public Optional<Categoria> selezionaCategoriaDaRimuovere(List<Categoria> categorie) {
        return categorie.isEmpty() ? Optional.empty() : Optional.of(categorie.get(0));
    }

    @Override
    public Optional<Categoria> selezionaCategoriaPerCampiSpecifici(List<Categoria> categorie) {
        return categorie.isEmpty() ? Optional.empty() : Optional.of(categorie.get(0));
    }

    @Override
    public boolean confermaRimozioneCategoria(Categoria categoria) {
        return confermaRimozioneCategoria;
    }

    @Override
    public FieldAction scegliAzioneCampiComuni(List<Campo> campi) {
        return commonFieldActions.isEmpty() ? FieldAction.TORNA : commonFieldActions.removeFirst();
    }

    @Override
    public FieldAction scegliAzioneCampiSpecifici(Categoria categoria) {
        return specificFieldActions.isEmpty() ? FieldAction.TORNA : specificFieldActions.removeFirst();
    }

    @Override
    public Optional<CampoDefinitionRequest> acquisisciNuovoCampo() {
        return nuovoCampo.or(() -> Optional.of(new CampoDefinitionRequest(
                "Campo " + (esitiCatalogo + 1),
                TipoDato.STRINGA,
                false)));
    }

    @Override
    public Optional<Campo> selezionaCampoDaRimuovere(List<Campo> campi) {
        return campi.isEmpty() ? Optional.empty() : Optional.of(campi.get(0));
    }

    @Override
    public boolean confermaRimozioneCampo(Campo campo) {
        return confermaRimozioneCampo;
    }

    @Override
    public Optional<CampoObbligatorietaRequest> acquisisciObbligatorietaCampo(List<Campo> campi) {
        return campi.isEmpty()
                ? Optional.empty()
                : Optional.of(new CampoObbligatorietaRequest(campi.get(0).getNome(), true));
    }

    @Override
    public Optional<Categoria> selezionaCategoriaPerProposta(List<Categoria> categorie) {
        return categorie.isEmpty() ? Optional.empty() : Optional.of(categorie.get(0));
    }

    @Override
    public Optional<Map<String, String>> acquisisciValoriProposta(Proposta proposta, ProposalFieldValidator validator) {
        return valoriProposta;
    }

    @Override
    public Optional<Map<String, String>> correggiValoriProposta(
            Proposta proposta,
            PropostaValidationResult result,
            ProposalFieldValidator validator) {
        return correzioniProposta;
    }

    @Override
    public void mostraPropostaSalvata(Proposta proposta) {
        proposteSalvate++;
    }

    @Override
    public Optional<Proposta> selezionaPropostaDaPubblicare(List<Proposta> proposte) {
        return proposte.isEmpty() ? Optional.empty() : Optional.of(proposte.get(0));
    }

    @Override
    public boolean confermaPubblicazione(Proposta proposta) {
        return confermaPubblicazione;
    }

    @Override
    public void mostraPropostaPubblicata(Proposta proposta) {
        propostePubblicate++;
    }

    @Override
    public Optional<Proposta> selezionaPropostaDaRitirare(List<Proposta> proposte) {
        return proposte.isEmpty() ? Optional.empty() : Optional.of(proposte.get(0));
    }

    @Override
    public boolean confermaRitiro(Proposta proposta) {
        return confermaRitiro;
    }

    @Override
    public void mostraBacheca(Map<String, List<Proposta>> bacheca) {
        bachecheMostrate++;
    }

    @Override
    public void mostraArchivioProposte(Map<StatoProposta, List<Proposta>> archivio) {
        archiviMostrati++;
    }

    @Override
    public Optional<Path> acquisisciPercorsoImportazione() {
        return percorsoImportazione;
    }

    @Override
    public void mostraRisultatoImportazione(ImportResult result) {
        risultatiImportazione++;
    }

    @Override
    public void mostraErrore(Failure failure) {
        erroriMostrati += failure == null ? 0 : 1;
    }

    @Override
    public void mostraOperazioneAnnullata() {
        operazioniAnnullate++;
    }
}

final class MockFruitoreView implements IFruitoreViewFacade {
    final ArrayDeque<IFruitoreView.MainAction> mainActions = new ArrayDeque<>();
    final ArrayDeque<Optional<Notifica>> notificheSelezionate = new ArrayDeque<>();
    boolean confermaIscrizione = true;
    boolean confermaDisiscrizione = true;
    boolean confermaEliminazioneNotifica = true;
    int iscrizioniMostrate;
    int disiscrizioniMostrate;
    int notificheEliminate;
    int erroriMostrati;

    MockFruitoreView(IFruitoreView.MainAction... actions) {
        mainActions.addAll(Arrays.asList(actions));
    }

    @Override
    public IFruitoreView.MainAction scegliAzionePrincipale(Fruitore fruitore) {
        return mainActions.isEmpty() ? IFruitoreView.MainAction.LOGOUT : mainActions.removeFirst();
    }

    @Override
    public Optional<Proposta> selezionaPropostaDaBacheca(Map<String, List<Proposta>> bacheca) {
        return bacheca.values().stream()
                .flatMap(List::stream)
                .findFirst();
    }

    @Override
    public boolean confermaIscrizione(Proposta proposta) {
        return confermaIscrizione;
    }

    @Override
    public Optional<Proposta> selezionaPropostaDaDisdire(List<Proposta> proposte) {
        return proposte.isEmpty() ? Optional.empty() : Optional.of(proposte.get(0));
    }

    @Override
    public boolean confermaDisiscrizione(Proposta proposta) {
        return confermaDisiscrizione;
    }

    @Override
    public void mostraIscrizioneEffettuata(Proposta proposta) {
        iscrizioniMostrate++;
    }

    @Override
    public void mostraDisiscrizioneEffettuata(Proposta proposta) {
        disiscrizioniMostrate++;
    }

    @Override
    public void mostraErrore(Failure failure) {
        erroriMostrati += failure == null ? 0 : 1;
    }

    @Override
    public Optional<Notifica> selezionaNotificaDaEliminare(Fruitore fruitore, List<Notifica> notifiche) {
        if (!notificheSelezionate.isEmpty()) {
            return notificheSelezionate.removeFirst();
        }
        return notifiche.isEmpty() ? Optional.empty() : Optional.of(notifiche.get(0));
    }

    @Override
    public boolean confermaEliminazioneNotifica(Notifica notifica) {
        return confermaEliminazioneNotifica;
    }

    @Override
    public void mostraNotificaEliminata(Notifica notifica) {
        notificheEliminate++;
    }
}
