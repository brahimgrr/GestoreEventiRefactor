package it.unibs.ingsoft.application.catalogo;

import it.unibs.ingsoft.application.catalogo.dto.CampoBaseExtraRequest;
import it.unibs.ingsoft.application.catalogo.dto.CampoDefinitionRequest;
import it.unibs.ingsoft.application.catalogo.dto.CampoObbligatorietaRequest;
import it.unibs.ingsoft.application.catalogo.dto.CatalogoOperationResult;
import it.unibs.ingsoft.domain.*;
import it.unibs.ingsoft.persistence.api.ICatalogoRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Gestisce campi base, campi comuni, categorie e campi specifici del catalogo.
 * Ogni mutazione viene persistita immediatamente tramite il repository.
 */
public final class CatalogoService {

    private final ICatalogoRepository repo;

    public CatalogoService(ICatalogoRepository repo) {
        this.repo = Objects.requireNonNull(repo);
    }

    private Catalogo catalogo() {
        return repo.get();
    }

    /**
     * Verifica se un nome è già usato da un campo base o comune (case-insensitive).
     */
    public boolean nomeEsistente(String nome) {
        return catalogo().nomeEsistenteGlobale(nome);
    }

    /**
     * Indica se il catalogo deve ancora fissare i campi base iniziali.
     */
    public boolean isPrimaConfigurazioneNecessaria() {
        return catalogo().getCampiBase().isEmpty();
    }

    /**
     * Restituisce i campi base predefiniti dalla traccia.
     */
    public List<Campo> getCampiBasePredefiniti() {
        return Arrays.stream(CampoBaseDefinito.values())
                .map(CampoBaseDefinito::toCampo)
                .collect(Collectors.toList());
    }

    /**
     * Fissa i campi base predefiniti e gli eventuali campi extra.
     */
    public void configuraCampiBase(List<CampoBaseExtraRequest> extra) {
        List<CampoBaseExtraRequest> richieste = extra == null ? Collections.emptyList() : extra;
        if (richieste.isEmpty()) {
            initiateCampiBase();
            return;
        }

        try {
            addCampiBaseConExtra(
                    richieste.stream().map(CampoBaseExtraRequest::nome).collect(Collectors.toList()),
                    richieste.stream().map(CampoBaseExtraRequest::tipoDato).collect(Collectors.toList())
            );
        } catch (IllegalArgumentException | IllegalStateException e) {
            if (!catalogo().isCampiBaseFissati()) {
                initiateCampiBase();
            }
            throw e;
        }
    }

    // ---------------- CAMPI BASE ----------------

    /**
     * Fissa i soli campi base predefiniti dalla specifica, senza extra.
     *
     * @pre getCampiBase().isEmpty()
     */
    public void initiateCampiBase() {
        catalogo().fissareCampiBase(
                Arrays.stream(CampoBaseDefinito.values())
                        .map(CampoBaseDefinito::toCampo)
                        .collect(Collectors.toList()),
                null
        );
        repo.save();
    }

    /**
     * Fissa i campi base predefiniti più quelli extra specificati.
     *
     * @throws IllegalArgumentException se un nome è duplicato o già esistente globalmente
     * @pre getCampiBase().isEmpty()
     * @pre nomi != null &amp;&amp; tipi != null &amp;&amp; nomi.size() == tipi.size()
     */
    public void addCampiBaseConExtra(List<String> nomi, List<TipoDato> tipi) {
        List<Campo> extra = IntStream.range(0, nomi.size())
                .mapToObj(i -> new Campo(
                        nomi.get(i).trim(),
                        TipoCampo.BASE,
                        tipi.get(i),
                        true))
                .collect(Collectors.toList());

        catalogo().fissareCampiBase(
                Arrays.stream(CampoBaseDefinito.values())
                        .map(CampoBaseDefinito::toCampo)
                        .collect(Collectors.toList()),
                extra
        );
        repo.save();
    }

    public List<Campo> getCampiBase() {
        return catalogo().getCampiBase();
    }

    // ---------------- CAMPI COMUNI ----------------

    /**
     * @throws IllegalArgumentException se il nome è già presente globalmente
     * @pre !nomeEsistente(nome)
     */
    public void addCampoComune(String nome, TipoDato tipo, boolean obbligatorio) {
        catalogo().addCampoComune(
                new Campo(nome.trim(), TipoCampo.COMUNE, tipo, obbligatorio)
        );
        repo.save();
    }

    public void addCampoComune(CampoDefinitionRequest request) {
        Objects.requireNonNull(request);
        addCampoComune(request.nome(), request.tipoDato(), request.obbligatorio());
    }

    /**
     * @return {@code true} se rimosso, {@code false} se non trovato
     */
    public boolean removeCampoComune(String nome) {
        boolean changed = catalogo().removeCampoComune(nome);
        if (changed) repo.save();
        return changed;
    }

    public CatalogoOperationResult rimuoviCampoComune(String nome) {
        return removeCampoComune(nome)
                ? CatalogoOperationResult.SUCCESSO
                : CatalogoOperationResult.NON_TROVATO;
    }

    /**
     * @return {@code true} se aggiornato, {@code false} se non trovato
     */
    public boolean setObbligatorietaCampoComune(String nome, boolean obbligatorio) {
        boolean changed = catalogo().updateCampoComune(nome, obbligatorio);
        if (changed) repo.save();
        return changed;
    }

    public CatalogoOperationResult setObbligatorietaCampoComune(CampoObbligatorietaRequest request) {
        Objects.requireNonNull(request);
        for (Campo campo : getCampiComuni()) {
            if (campo.getNome().equalsIgnoreCase(request.nomeCampo()) &&
                    campo.isObbligatorio() == request.obbligatorio()) {
                return CatalogoOperationResult.NESSUNA_MODIFICA;
            }
        }

        return setObbligatorietaCampoComune(request.nomeCampo(), request.obbligatorio())
                ? CatalogoOperationResult.SUCCESSO
                : CatalogoOperationResult.NON_TROVATO;
    }

    public List<Campo> getCampiComuni() {
        return catalogo().getCampiComuni();
    }

    // ---------------- CATEGORIE ----------------

    /**
     * @throws IllegalArgumentException se la categoria esiste già
     * @pre nome != null &amp;&amp; !nome.isBlank()
     */
    public Categoria createCategoria(String nome) {
        Categoria c = catalogo().addCategoria(nome);
        repo.save();
        return c;
    }

    /**
     * @return {@code true} se rimossa, {@code false} se non trovata
     */
    public boolean removeCategoria(String nome) {
        boolean changed = catalogo().removeCategoria(nome);
        if (changed) repo.save();
        return changed;
    }

    public CatalogoOperationResult rimuoviCategoria(String nome) {
        return removeCategoria(nome)
                ? CatalogoOperationResult.SUCCESSO
                : CatalogoOperationResult.NON_TROVATO;
    }

    public List<Categoria> getCategorie() {
        return catalogo().getCategorie();
    }

    // ---------------- CAMPI SPECIFICI ----------------

    /**
     * @throws IllegalArgumentException se il nome è già presente globalmente o la categoria non esiste
     * @pre !nomeEsistente(nome)
     */
    public void addCampoSpecifico(String categoria, String nome, TipoDato tipo, boolean obbligatorio) {
        catalogo().addCampoSpecifico(
                categoria,
                new Campo(nome.trim(), TipoCampo.SPECIFICO, tipo, obbligatorio)
        );
        repo.save();
    }

    public void addCampoSpecifico(String categoria, CampoDefinitionRequest request) {
        Objects.requireNonNull(request);
        addCampoSpecifico(categoria, request.nome(), request.tipoDato(), request.obbligatorio());
    }

    /**
     * @return {@code true} se rimosso, {@code false} se non trovato
     */
    public boolean removeCampoSpecifico(String categoria, String nome) {
        boolean changed = catalogo().removeCampoSpecifico(categoria, nome);
        if (changed) repo.save();
        return changed;
    }

    public CatalogoOperationResult rimuoviCampoSpecifico(String categoria, String nome) {
        return removeCampoSpecifico(categoria, nome)
                ? CatalogoOperationResult.SUCCESSO
                : CatalogoOperationResult.NON_TROVATO;
    }

    /**
     * @return {@code true} se aggiornato, {@code false} se non trovato
     */
    public boolean setObbligatorietaCampoSpecifico(String categoria, String nome, boolean obbligatorio) {
        boolean changed = catalogo().updateCampoSpecifico(categoria, nome, obbligatorio);
        if (changed) repo.save();
        return changed;
    }

    public CatalogoOperationResult setObbligatorietaCampoSpecifico(String categoria, CampoObbligatorietaRequest request) {
        Objects.requireNonNull(request);
        Categoria cat = catalogo().getCategoriaOrThrow(categoria);
        for (Campo campo : cat.getCampiSpecifici()) {
            if (campo.getNome().equalsIgnoreCase(request.nomeCampo()) &&
                    campo.isObbligatorio() == request.obbligatorio()) {
                return CatalogoOperationResult.NESSUNA_MODIFICA;
            }
        }

        return setObbligatorietaCampoSpecifico(categoria, request.nomeCampo(), request.obbligatorio())
                ? CatalogoOperationResult.SUCCESSO
                : CatalogoOperationResult.NON_TROVATO;
    }
}
