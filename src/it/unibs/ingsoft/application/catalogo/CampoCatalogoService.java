package it.unibs.ingsoft.application.catalogo;

import it.unibs.ingsoft.application.catalogo.dto.CampoBaseExtraRequest;
import it.unibs.ingsoft.application.catalogo.dto.CampoDefinitionRequest;
import it.unibs.ingsoft.application.catalogo.dto.CampoObbligatorietaRequest;
import it.unibs.ingsoft.application.catalogo.dto.CatalogoOperationResult;
import it.unibs.ingsoft.domain.model.catalogo.*;
import it.unibs.ingsoft.domain.repository.CatalogoRepository;
import it.unibs.ingsoft.shared.error.FailureException;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Gestisce la configurazione dei campi del catalogo.
 */
public final class CampoCatalogoService {
    private final CatalogoRepository repo;
    private final CampoFactory campoFactory;

    public CampoCatalogoService(CatalogoRepository repo, CampoFactory campoFactory) {
        this.repo = Objects.requireNonNull(repo);
        this.campoFactory = Objects.requireNonNull(campoFactory);
    }

    private Catalogo catalogo() {
        return repo.load();
    }

    public boolean nomeEsistente(String nome) {
        return catalogo().nomeEsistenteGlobale(nome);
    }

    public boolean isPrimaConfigurazioneNecessaria() {
        return catalogo().getCampiBase().isEmpty();
    }

    public List<Campo> getCampiBasePredefiniti() {
        return campoFactory.creaCampiBase();
    }

    public void configuraCampiBase(List<CampoBaseExtraRequest> extra) {
        List<CampoBaseExtraRequest> richieste = extra == null ? Collections.emptyList() : extra;
        if (richieste.isEmpty()) {
            initiateCampiBase();
            return;
        }

        try {
            Catalogo catalogo = repo.load();
            addCampiBaseConExtra(
                    catalogo,
                    richieste.stream().map(CampoBaseExtraRequest::nome).collect(Collectors.toList()),
                    richieste.stream().map(CampoBaseExtraRequest::tipoDato).collect(Collectors.toList())
            );
            repo.save(catalogo);
        } catch (FailureException e) {
            Catalogo fallback = repo.load();
            if (!fallback.isCampiBaseFissati()) {
                initiateCampiBase(fallback);
                repo.save(fallback);
            }
            throw e;
        }
    }

    public void initiateCampiBase() {
        Catalogo catalogo = repo.load();
        initiateCampiBase(catalogo);
        repo.save(catalogo);
    }

    private void initiateCampiBase(Catalogo catalogo) {
        catalogo.fissareCampiBase(
                campoFactory.creaCampiBase(),
                null
        );
    }

    public void addCampiBaseConExtra(List<String> nomi, List<TipoDato> tipi) {
        Catalogo catalogo = repo.load();
        addCampiBaseConExtra(catalogo, nomi, tipi);
        repo.save(catalogo);
    }

    private void addCampiBaseConExtra(Catalogo catalogo, List<String> nomi, List<TipoDato> tipi) {
        List<Campo> extra = campoFactory.creaCampiBaseExtra(nomi, tipi);

        catalogo.fissareCampiBase(
                campoFactory.creaCampiBase(),
                extra
        );
    }

    public List<Campo> getCampiBase() {
        return catalogo().getCampiBase();
    }

    public void addCampoComune(String nome, TipoDato tipo, boolean obbligatorio) {
        Catalogo catalogo = repo.load();
        catalogo.addCampoComune(
                campoFactory.creaCampoComune(nome, tipo, obbligatorio)
        );
        repo.save(catalogo);
    }

    public void addCampoComune(CampoDefinitionRequest request) {
        Objects.requireNonNull(request);
        addCampoComune(request.nome(), request.tipoDato(), request.obbligatorio());
    }

    public boolean removeCampoComune(String nome) {
        Catalogo catalogo = repo.load();
        boolean changed = catalogo.removeCampoComune(nome);
        if (changed) repo.save(catalogo);
        return changed;
    }

    public CatalogoOperationResult rimuoviCampoComune(String nome) {
        return removeCampoComune(nome)
                ? CatalogoOperationResult.SUCCESSO
                : CatalogoOperationResult.NON_TROVATO;
    }

    public CatalogoOperationResult setObbligatorietaCampoComune(CampoObbligatorietaRequest request) {
        Objects.requireNonNull(request);
        Catalogo catalogo = repo.load();
        for (Campo campo : catalogo.getCampiComuni()) {
            if (campo.getNome().equalsIgnoreCase(request.nomeCampo()) &&
                    campo.isObbligatorio() == request.obbligatorio()) {
                return CatalogoOperationResult.NESSUNA_MODIFICA;
            }
        }

        boolean changed = catalogo.updateCampoComune(request.nomeCampo(), request.obbligatorio());
        if (changed) {
            repo.save(catalogo);
            return CatalogoOperationResult.SUCCESSO;
        }
        return CatalogoOperationResult.NON_TROVATO;
    }

    public List<Campo> getCampiComuni() {
        return catalogo().getCampiComuni();
    }

    public void addCampoSpecifico(String categoria, String nome, TipoDato tipo, boolean obbligatorio) {
        Catalogo catalogo = repo.load();
        catalogo.addCampoSpecifico(
                categoria,
                campoFactory.creaCampoSpecifico(nome, tipo, obbligatorio)
        );
        repo.save(catalogo);
    }

    public void addCampoSpecifico(String categoria, CampoDefinitionRequest request) {
        Objects.requireNonNull(request);
        addCampoSpecifico(categoria, request.nome(), request.tipoDato(), request.obbligatorio());
    }

    public boolean removeCampoSpecifico(String categoria, String nome) {
        Catalogo catalogo = repo.load();
        boolean changed = catalogo.removeCampoSpecifico(categoria, nome);
        if (changed) repo.save(catalogo);
        return changed;
    }

    public CatalogoOperationResult rimuoviCampoSpecifico(String categoria, String nome) {
        return removeCampoSpecifico(categoria, nome)
                ? CatalogoOperationResult.SUCCESSO
                : CatalogoOperationResult.NON_TROVATO;
    }

    public CatalogoOperationResult setObbligatorietaCampoSpecifico(String categoria, CampoObbligatorietaRequest request) {
        Objects.requireNonNull(request);
        Catalogo catalogo = repo.load();
        Categoria cat = catalogo.getCategoriaOrThrow(categoria);
        for (Campo campo : cat.getCampiSpecifici()) {
            if (campo.getNome().equalsIgnoreCase(request.nomeCampo()) &&
                    campo.isObbligatorio() == request.obbligatorio()) {
                return CatalogoOperationResult.NESSUNA_MODIFICA;
            }
        }

        boolean changed = catalogo.updateCampoSpecifico(categoria, request.nomeCampo(), request.obbligatorio());
        if (changed) {
            repo.save(catalogo);
            return CatalogoOperationResult.SUCCESSO;
        }
        return CatalogoOperationResult.NON_TROVATO;
    }
}
