package it.unibs.ingsoft.application.catalogo;

import it.unibs.ingsoft.application.catalogo.dto.CampoBaseExtraRequest;
import it.unibs.ingsoft.application.catalogo.dto.CampoDefinitionRequest;
import it.unibs.ingsoft.application.catalogo.dto.CampoObbligatorietaRequest;
import it.unibs.ingsoft.application.catalogo.dto.CatalogoOperationResult;
import it.unibs.ingsoft.domain.Campo;
import it.unibs.ingsoft.domain.Categoria;
import it.unibs.ingsoft.domain.TipoDato;
import it.unibs.ingsoft.domain.factory.CampoFactory;
import it.unibs.ingsoft.persistence.interfaces.ICatalogoRepository;

import java.util.List;
import java.util.Objects;

/**
 * Facade compatibile per campi e categorie del catalogo.
 */
public final class CatalogoService {
    private final CampoCatalogoService campoCatalogoService;
    private final CategoriaCatalogoService categoriaCatalogoService;

    public CatalogoService(ICatalogoRepository repo) {
        this(repo, CampoFactory.getInstance());
    }

    public CatalogoService(ICatalogoRepository repo, CampoFactory campoFactory) {
        this(
                new CampoCatalogoService(repo, campoFactory),
                new CategoriaCatalogoService(repo)
        );
    }

    public CatalogoService(CampoCatalogoService campoCatalogoService,
                           CategoriaCatalogoService categoriaCatalogoService) {
        this.campoCatalogoService = Objects.requireNonNull(campoCatalogoService);
        this.categoriaCatalogoService = Objects.requireNonNull(categoriaCatalogoService);
    }

    public boolean nomeEsistente(String nome) {
        return campoCatalogoService.nomeEsistente(nome);
    }

    public boolean isPrimaConfigurazioneNecessaria() {
        return campoCatalogoService.isPrimaConfigurazioneNecessaria();
    }

    public List<Campo> getCampiBasePredefiniti() {
        return campoCatalogoService.getCampiBasePredefiniti();
    }

    public void configuraCampiBase(List<CampoBaseExtraRequest> extra) {
        campoCatalogoService.configuraCampiBase(extra);
    }

    public void initiateCampiBase() {
        campoCatalogoService.initiateCampiBase();
    }

    public void addCampiBaseConExtra(List<String> nomi, List<TipoDato> tipi) {
        campoCatalogoService.addCampiBaseConExtra(nomi, tipi);
    }

    public List<Campo> getCampiBase() {
        return campoCatalogoService.getCampiBase();
    }

    public void addCampoComune(String nome, TipoDato tipo, boolean obbligatorio) {
        campoCatalogoService.addCampoComune(nome, tipo, obbligatorio);
    }

    public void addCampoComune(CampoDefinitionRequest request) {
        campoCatalogoService.addCampoComune(request);
    }

    public CatalogoOperationResult rimuoviCampoComune(String nome) {
        return campoCatalogoService.rimuoviCampoComune(nome);
    }

    public CatalogoOperationResult setObbligatorietaCampoComune(CampoObbligatorietaRequest request) {
        return campoCatalogoService.setObbligatorietaCampoComune(request);
    }

    public List<Campo> getCampiComuni() {
        return campoCatalogoService.getCampiComuni();
    }

    public Categoria createCategoria(String nome) {
        return categoriaCatalogoService.createCategoria(nome);
    }

    public CatalogoOperationResult rimuoviCategoria(String nome) {
        return categoriaCatalogoService.rimuoviCategoria(nome);
    }

    public List<Categoria> getCategorie() {
        return categoriaCatalogoService.getCategorie();
    }

    public void addCampoSpecifico(String categoria, String nome, TipoDato tipo, boolean obbligatorio) {
        campoCatalogoService.addCampoSpecifico(categoria, nome, tipo, obbligatorio);
    }

    public void addCampoSpecifico(String categoria, CampoDefinitionRequest request) {
        campoCatalogoService.addCampoSpecifico(categoria, request);
    }

    public CatalogoOperationResult rimuoviCampoSpecifico(String categoria, String nome) {
        return campoCatalogoService.rimuoviCampoSpecifico(categoria, nome);
    }

    public CatalogoOperationResult setObbligatorietaCampoSpecifico(String categoria, CampoObbligatorietaRequest request) {
        return campoCatalogoService.setObbligatorietaCampoSpecifico(categoria, request);
    }
}
