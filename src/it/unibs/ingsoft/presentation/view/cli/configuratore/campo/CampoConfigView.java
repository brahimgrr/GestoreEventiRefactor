package it.unibs.ingsoft.presentation.view.cli.configuratore.campo;

import it.unibs.ingsoft.application.catalogo.dto.CampoDefinitionRequest;
import it.unibs.ingsoft.application.catalogo.dto.CampoObbligatorietaRequest;
import it.unibs.ingsoft.domain.model.catalogo.Campo;
import it.unibs.ingsoft.domain.model.catalogo.Categoria;
import it.unibs.ingsoft.domain.model.catalogo.TipoDato;
import it.unibs.ingsoft.presentation.view.interfaces.common.IAppView;
import it.unibs.ingsoft.presentation.view.interfaces.common.OperationCancelledException;
import it.unibs.ingsoft.presentation.view.interfaces.configuratore.campo.ICampoConfigView;

import java.util.List;
import java.util.Optional;

public final class CampoConfigView implements ICampoConfigView {
    private static final String[] MENU_CAMPI = {
            "Aggiungi campo",
            "Rimuovi campo",
            "Cambia obbligatorieta campo"
    };

    private final IAppView ui;
    private final CampoRenderer campoRenderer;

    public CampoConfigView(IAppView ui, CampoRenderer campoRenderer) {
        this.ui = ui;
        this.campoRenderer = campoRenderer;
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
        campoRenderer.stampaCampi(campi);
        ui.newLine();
        ui.stampaMenu("", MENU_CAMPI, "Torna");
        int choice = ui.acquisisciIntero("Scelta: ", 0, MENU_CAMPI.length);
        ui.newLine();
        return choice == 0 ? FieldAction.TORNA : FieldAction.values()[choice - 1];
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
            TipoDato tipo = campoRenderer.acquisisciTipoDato("Tipo di dato:");
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
        return ui.selezionaElemento("Seleziona campo da rimuovere:", campi, campoRenderer::formatCampo);
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
        Optional<Campo> campo = ui.selezionaElemento("Seleziona campo:", campi, campoRenderer::formatCampo);
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

    private void mostraOperazioneAnnullata() {
        ui.stampaInfo("Operazione annullata.");
    }
}
