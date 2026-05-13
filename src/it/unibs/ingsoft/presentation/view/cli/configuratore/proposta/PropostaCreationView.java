package it.unibs.ingsoft.presentation.view.cli.configuratore.proposta;

import it.unibs.ingsoft.application.proposta.dto.PropostaValidationResult;
import it.unibs.ingsoft.domain.catalogo.Campo;
import it.unibs.ingsoft.domain.catalogo.Categoria;
import it.unibs.ingsoft.domain.proposta.Proposta;
import it.unibs.ingsoft.presentation.view.cli.common.proposta.PropostaRenderer;
import it.unibs.ingsoft.presentation.view.cli.configuratore.categoria.CategoriaRenderer;
import it.unibs.ingsoft.presentation.view.cli.configuratore.proposta.ValidationErrorMessageMapper;
import it.unibs.ingsoft.presentation.view.interfaces.common.IAppView;
import it.unibs.ingsoft.presentation.view.interfaces.configuratore.proposta.IPropostaCreationView;
import it.unibs.ingsoft.presentation.view.interfaces.common.OperationCancelledException;
import it.unibs.ingsoft.presentation.view.interfaces.configuratore.proposta.ProposalFieldValidator;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class PropostaCreationView implements IPropostaCreationView {
    private final IAppView ui;
    private final CategoriaRenderer categoriaRenderer;
    private final PropostaFormView propostaFormView;
    private final PropostaRenderer propostaRenderer;

    public PropostaCreationView(
            IAppView ui,
            CategoriaRenderer categoriaRenderer,
            PropostaFormView propostaFormView,
            PropostaRenderer propostaRenderer) {
        this.ui = ui;
        this.categoriaRenderer = categoriaRenderer;
        this.propostaFormView = propostaFormView;
        this.propostaRenderer = propostaRenderer;
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
        var scelta = categoriaRenderer.selezionaCategoria(categorie);
        return scelta.isEmpty() ? Optional.empty() : Optional.of(categorie.get(scelta.getAsInt()));
    }

    @Override
    public Optional<Map<String, String>> acquisisciValoriProposta(Proposta proposta, ProposalFieldValidator validator) {
        ui.header("CREA PROPOSTA");
        ui.newLine();
        ui.stampa("Digita 'annulla' per abortire l'operazione.");
        ui.stampa("(*) = obbligatorio | il tipo e indicato tra [  ]");
        ui.newLine();
        return propostaFormView.acquisisciValoriProposta(proposta, validator);
    }

    @Override
    public Optional<Map<String, String>> correggiValoriProposta(
            Proposta proposta,
            PropostaValidationResult result,
            ProposalFieldValidator validator) {
        ui.newLine();
        ui.stampa("La proposta NON e valida per i seguenti motivi:");
        for (var errore : result.errori()) {
            ui.stampaErrore(ValidationErrorMessageMapper.message(errore));
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
                    .collect(Collectors.toSet());
            Optional<Map<String, String>> correzioni =
                    propostaFormView.correggiCampiProposta(proposta, nomiConErrore, validator);
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
    public void mostraPropostaSalvata(Proposta proposta) {
        ui.newLine();
        propostaRenderer.mostraRiepilogoProposta(proposta);
        ui.stampaSuccesso("Proposta valida salvata. Puoi pubblicarla dal menu 'Pubblicare una proposta di iniziativa'.");
        ui.newLine();
        ui.pausa();
    }
}
