package it.unibs.ingsoft.presentation.view.interfaces.configuratore.campo;

import it.unibs.ingsoft.application.catalogo.dto.CampoDefinitionRequest;
import it.unibs.ingsoft.application.catalogo.dto.CampoObbligatorietaRequest;
import it.unibs.ingsoft.domain.catalogo.Campo;
import it.unibs.ingsoft.domain.catalogo.Categoria;

import java.util.List;
import java.util.Optional;

public interface ICampoConfigView {
    enum FieldAction {
        AGGIUNGI,
        RIMUOVI,
        CAMBIA_OBBLIGATORIETA,
        TORNA
    }

    FieldAction scegliAzioneCampiComuni(List<Campo> campi);

    FieldAction scegliAzioneCampiSpecifici(Categoria categoria);

    Optional<CampoDefinitionRequest> acquisisciNuovoCampo();

    Optional<Campo> selezionaCampoDaRimuovere(List<Campo> campi);

    boolean confermaRimozioneCampo(Campo campo);

    Optional<CampoObbligatorietaRequest> acquisisciObbligatorietaCampo(List<Campo> campi);
}
