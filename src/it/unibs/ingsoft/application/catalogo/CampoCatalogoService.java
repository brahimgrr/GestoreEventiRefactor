package it.unibs.ingsoft.application.catalogo;

import it.unibs.ingsoft.application.catalogo.dto.CampoBaseExtraRequest;
import it.unibs.ingsoft.application.catalogo.dto.CampoDefinitionRequest;
import it.unibs.ingsoft.application.catalogo.dto.CampoObbligatorietaRequest;
import it.unibs.ingsoft.application.catalogo.dto.CatalogoOperationResult;
import it.unibs.ingsoft.domain.Campo;
import it.unibs.ingsoft.domain.Catalogo;
import it.unibs.ingsoft.domain.Categoria;
import it.unibs.ingsoft.domain.TipoDato;
import it.unibs.ingsoft.domain.factory.CampoFactory;
import it.unibs.ingsoft.persistence.api.ICatalogoRepository;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Gestisce la configurazione dei campi del catalogo.
 */
public final class CampoCatalogoService {
    private final ICatalogoRepository repo;
    private final CampoFactory campoFactory;

    public CampoCatalogoService(ICatalogoRepository repo, CampoFactory campoFactory) {
        this.repo = Objects.requireNonNull(repo);
        this.campoFactory = Objects.requireNonNull(campoFactory);
    }

    private Catalogo catalogo() {
        return repo.get();
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

    public void initiateCampiBase() {
        catalogo().fissareCampiBase(
                campoFactory.creaCampiBase(),
                null
        );
        repo.save();
    }

    public void addCampiBaseConExtra(List<String> nomi, List<TipoDato> tipi) {
        List<Campo> extra = campoFactory.creaCampiBaseExtra(nomi, tipi);

        catalogo().fissareCampiBase(
                campoFactory.creaCampiBase(),
                extra
        );
        repo.save();
    }

    public List<Campo> getCampiBase() {
        return catalogo().getCampiBase();
    }

    public void addCampoComune(String nome, TipoDato tipo, boolean obbligatorio) {
        catalogo().addCampoComune(
                campoFactory.creaCampoComune(nome, tipo, obbligatorio)
        );
        repo.save();
    }

    public void addCampoComune(CampoDefinitionRequest request) {
        Objects.requireNonNull(request);
        addCampoComune(request.nome(), request.tipoDato(), request.obbligatorio());
    }

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

    public void addCampoSpecifico(String categoria, String nome, TipoDato tipo, boolean obbligatorio) {
        catalogo().addCampoSpecifico(
                categoria,
                campoFactory.creaCampoSpecifico(nome, tipo, obbligatorio)
        );
        repo.save();
    }

    public void addCampoSpecifico(String categoria, CampoDefinitionRequest request) {
        Objects.requireNonNull(request);
        addCampoSpecifico(categoria, request.nome(), request.tipoDato(), request.obbligatorio());
    }

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
