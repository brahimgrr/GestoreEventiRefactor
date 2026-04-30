package it.unibs.ingsoft.testsupport;

import it.unibs.ingsoft.domain.*;
import it.unibs.ingsoft.presentation.view.interfaces.IAppView;
import it.unibs.ingsoft.presentation.view.interfaces.ProposalFieldValidator;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public final class ScriptedAppView implements IAppView {
    private final Queue<String> strings = new ArrayDeque<>();
    private final Queue<Integer> integers = new ArrayDeque<>();
    private final Queue<Boolean> booleans = new ArrayDeque<>();
    private final Queue<TipoDato> tipoDati = new ArrayDeque<>();
    private final List<String> output = new ArrayList<>();

    public ScriptedAppView strings(String... values) {
        strings.addAll(Arrays.asList(values));
        return this;
    }

    public ScriptedAppView integers(Integer... values) {
        integers.addAll(Arrays.asList(values));
        return this;
    }

    public ScriptedAppView booleans(Boolean... values) {
        booleans.addAll(Arrays.asList(values));
        return this;
    }

    public ScriptedAppView tipoDati(TipoDato... values) {
        tipoDati.addAll(Arrays.asList(values));
        return this;
    }

    public List<String> output() {
        return Collections.unmodifiableList(output);
    }

    @Override
    public void stampa(String testo) {
        output.add(testo);
    }

    @Override
    public void newLine() {
        output.add("");
    }

    @Override
    public void header(String titolo) {
        output.add("# " + titolo);
    }

    @Override
    public void stampaSezione(String titolo) {
        output.add("## " + titolo);
    }

    @Override
    public void stampaCampi(List<Campo> campi) {
        output.add("campi:" + campi.size());
    }

    @Override
    public void stampaCategorie(List<Categoria> categorie) {
        output.add("categorie:" + categorie.size());
    }

    @Override
    public void stampaCategorieDettaglio(Map<String, List<String>> categorieConCampi) {
        output.add("categorie-dettaglio:" + categorieConCampi.size());
    }

    @Override
    public void stampaMenu(String titolo, String[] voci) {
        output.add("menu:" + titolo + ":" + voci.length);
    }

    @Override
    public void stampaMenu(String titolo, String[] voci, String uscitaLabel) {
        output.add("menu:" + titolo + ":" + voci.length + ":" + uscitaLabel);
    }

    @Override
    public void pausa() {
        output.add("pausa");
    }

    @Override
    public void stampaSuccesso(String msg) {
        output.add("OK:" + msg);
    }

    @Override
    public void stampaErrore(String msg) {
        output.add("ERR:" + msg);
    }

    @Override
    public void stampaAvviso(String msg) {
        output.add("WARN:" + msg);
    }

    @Override
    public void stampaInfo(String msg) {
        output.add("INFO:" + msg);
    }

    @Override
    public void mostraBacheca(Map<String, List<Proposta>> bacheca) {
        output.add("bacheca:" + bacheca.size());
    }

    @Override
    public void mostraRiepilogoProposta(Proposta proposta) {
        output.add("proposta:" + proposta.getValoriCampi().getOrDefault(AppConstants.CAMPO_TITOLO, ""));
    }

    @Override
    public void mostraAderenti(List<String> aderenti) {
        output.add("aderenti:" + aderenti.size());
    }

    @Override
    public void mostraCronologiaStati(List<PropostaStateChange> history) {
        output.add("history:" + history.size());
    }

    @Override
    public String acquisisciStringa(String prompt) {
        if (strings.isEmpty()) {
            throw new AssertionError("No scripted string for prompt: " + prompt);
        }
        return strings.remove();
    }

    @Override
    public String acquisisciStringaConValidazione(String prompt, Predicate<String> validatore, String errorMsg) {
        while (true) {
            String value = acquisisciStringa(prompt);
            if (validatore.test(value)) {
                return value;
            }
            stampaErrore(errorMsg);
        }
    }

    @Override
    public String acquisisciPassword(String prompt) {
        return acquisisciStringa(prompt);
    }

    @Override
    public int acquisisciIntero(String prompt, int min, int max) {
        if (integers.isEmpty()) {
            throw new AssertionError("No scripted integer for prompt: " + prompt);
        }
        int value = integers.remove();
        if (value < min || value > max) {
            throw new AssertionError("Scripted integer out of range: " + value);
        }
        return value;
    }

    @Override
    public boolean acquisisciSiNo(String prompt) {
        if (booleans.isEmpty()) {
            throw new AssertionError("No scripted boolean for prompt: " + prompt);
        }
        return booleans.remove();
    }

    @Override
    public TipoDato acquisisciTipoDato(String prompt) {
        if (tipoDati.isEmpty()) {
            throw new AssertionError("No scripted TipoDato for prompt: " + prompt);
        }
        return tipoDati.remove();
    }

    @Override
    public List<String> acquisisciListaNomi(String titolo) {
        List<String> values = new ArrayList<>();
        while (!strings.isEmpty()) {
            values.add(strings.remove());
        }
        return values;
    }

    @Override
    public <T> Optional<T> selezionaElemento(String prompt, List<T> elementi) {
        if (elementi.isEmpty()) {
            return Optional.empty();
        }
        int choice = acquisisciIntero(prompt, 0, elementi.size());
        return choice == 0 ? Optional.empty() : Optional.of(elementi.get(choice - 1));
    }

    @Override
    public <T> Optional<T> selezionaElementoConInfo(String prompt, List<T> elementi, Function<T, String> infoMapper) {
        return selezionaElemento(prompt, elementi);
    }

    @Override
    public OptionalInt selezionaCategoria(List<Categoria> categorie) {
        if (categorie.isEmpty()) {
            return OptionalInt.empty();
        }
        int choice = acquisisciIntero("categoria", 0, categorie.size());
        return choice == 0 ? OptionalInt.empty() : OptionalInt.of(choice - 1);
    }

    @Override
    public Optional<Map<String, String>> acquisisciValoriProposta(Proposta proposta, ProposalFieldValidator validator) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, String>> correggiCampiProposta(Proposta proposta, Set<String> nomiCampi, ProposalFieldValidator validator) {
        return Optional.empty();
    }
}
