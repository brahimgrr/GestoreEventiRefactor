package it.unibs.ingsoft.presentation.view.cli.configuratore.proposta;

import it.unibs.ingsoft.domain.error.ValidationError;
import it.unibs.ingsoft.domain.model.catalogo.Campo;
import it.unibs.ingsoft.domain.model.proposta.Proposta;
import it.unibs.ingsoft.presentation.view.cli.common.error.FailureMessageRegistry;
import it.unibs.ingsoft.presentation.view.interfaces.common.IAppView;
import it.unibs.ingsoft.presentation.view.interfaces.common.OperationCancelledException;
import it.unibs.ingsoft.presentation.view.interfaces.configuratore.proposta.PropostaCampoValidator;

import java.util.*;

public final class PropostaFormView {
    private final IAppView ui;
    private final FailureMessageRegistry messages;

    public PropostaFormView(IAppView ui) {
        this(ui, FailureMessageRegistry.cliDefault());
    }

    public PropostaFormView(IAppView ui, FailureMessageRegistry messages) {
        this.ui = ui;
        this.messages = messages;
    }

    public Optional<Map<String, String>> acquisisciValoriProposta(
            Proposta proposta,
            PropostaCampoValidator validator) {
        return eseguiForm(proposta, proposta.getCampi(), validator);
    }

    public Optional<Map<String, String>> correggiCampiProposta(
            Proposta proposta,
            Set<String> nomiCampi,
            PropostaCampoValidator validator) {
        List<Campo> campiDaCorreggere = proposta.getCampi().stream()
                .filter(c -> nomiCampi.contains(c.getNome()))
                .toList();
        return eseguiForm(proposta, campiDaCorreggere, validator);
    }

    private Optional<Map<String, String>> eseguiForm(
            Proposta proposta,
            List<Campo> campi,
            PropostaCampoValidator validator) {
        Map<String, String> ctx = new LinkedHashMap<>(proposta.getValoriCampi());
        int i = 0;

        while (i < campi.size()) {
            Campo campo = campi.get(i);
            String nome = campo.getNome();
            String current = ctx.get(nome);

            String obbLabel = campo.isObbligatorio() ? "(*) " : "";
            String attualeLabel = (current != null && !current.isBlank())
                    ? " [attuale: " + current + "]"
                    : "";
            String prompt = "[" + (i + 1) + "/" + campi.size() + "] " + obbLabel
                    + nome + " [" + campo.getTipoDato() + "]" + attualeLabel + ": ";

            String raw;
            try {
                raw = ui.acquisisciStringa(prompt).trim();
            } catch (OperationCancelledException e) {
                return Optional.empty();
            }

            if (raw.isBlank()) {
                if (current != null && !current.isBlank()) {
                    ui.stampaSuccesso("  Campo invariato: " + current);
                    i++;
                } else if (!campo.isObbligatorio()) {
                    i++;
                } else {
                    ui.stampaErrore("  Campo obbligatorio. Inserire un valore.");
                }
                continue;
            }

            Map<String, String> candidateValues = new LinkedHashMap<>(ctx);
            candidateValues.put(nome, raw);
            List<ValidationError> validationErrors = validator.valida(
                    campo,
                    Collections.unmodifiableMap(candidateValues)
            );
            if (!validationErrors.isEmpty()) {
                for (ValidationError validationError : validationErrors)
                    ui.stampaErrore("  " + messages.message(validationError.failure()));
                continue;
            }

            ctx.put(nome, raw);
            ui.stampaSuccesso("");
            i++;
        }

        return Optional.of(ctx);
    }
}
