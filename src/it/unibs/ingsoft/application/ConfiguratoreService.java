package it.unibs.ingsoft.application;

import it.unibs.ingsoft.application.batch.BatchImportService;
import it.unibs.ingsoft.application.batch.dto.ImportResult;
import it.unibs.ingsoft.application.catalogo.CatalogoService;
import it.unibs.ingsoft.application.catalogo.dto.CampoBaseExtraRequest;
import it.unibs.ingsoft.application.catalogo.dto.CampoDefinitionRequest;
import it.unibs.ingsoft.application.catalogo.dto.CampoObbligatorietaRequest;
import it.unibs.ingsoft.application.catalogo.dto.CatalogoOperationResult;
import it.unibs.ingsoft.application.proposta.PropostaService;
import it.unibs.ingsoft.application.proposta.dto.PropostaValidationResult;
import it.unibs.ingsoft.domain.Campo;
import it.unibs.ingsoft.domain.Categoria;
import it.unibs.ingsoft.domain.Proposta;
import it.unibs.ingsoft.domain.StatoProposta;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Facade dei casi d'uso disponibili al configuratore.
 */
public final class ConfiguratoreService {
    private final CatalogoService catalogoService;
    private final PropostaService propostaService;
    private final BatchImportService batchImportService;

    public ConfiguratoreService(CatalogoService catalogoService,
                                PropostaService propostaService,
                                BatchImportService batchImportService) {
        this.catalogoService = Objects.requireNonNull(catalogoService);
        this.propostaService = Objects.requireNonNull(propostaService);
        this.batchImportService = Objects.requireNonNull(batchImportService);
    }

    public boolean isPrimaConfigurazioneNecessaria() {
        return catalogoService.isPrimaConfigurazioneNecessaria();
    }

    public List<Campo> getCampiBasePredefiniti() {
        return catalogoService.getCampiBasePredefiniti();
    }

    public void configuraCampiBase(List<CampoBaseExtraRequest> extra) {
        catalogoService.configuraCampiBase(extra);
    }

    public List<Campo> getCampiBase() {
        return catalogoService.getCampiBase();
    }

    public List<Campo> getCampiComuni() {
        return catalogoService.getCampiComuni();
    }

    public List<Categoria> getCategorie() {
        return catalogoService.getCategorie();
    }

    public Categoria createCategoria(String nome) {
        return catalogoService.createCategoria(nome);
    }

    public CatalogoOperationResult rimuoviCategoria(String nome) {
        return catalogoService.rimuoviCategoria(nome);
    }

    public void addCampoComune(CampoDefinitionRequest request) {
        catalogoService.addCampoComune(request);
    }

    public CatalogoOperationResult rimuoviCampoComune(String nome) {
        return catalogoService.rimuoviCampoComune(nome);
    }

    public CatalogoOperationResult setObbligatorietaCampoComune(CampoObbligatorietaRequest request) {
        return catalogoService.setObbligatorietaCampoComune(request);
    }

    public void addCampoSpecifico(String categoria, CampoDefinitionRequest request) {
        catalogoService.addCampoSpecifico(categoria, request);
    }

    public CatalogoOperationResult rimuoviCampoSpecifico(String categoria, String nome) {
        return catalogoService.rimuoviCampoSpecifico(categoria, nome);
    }

    public CatalogoOperationResult setObbligatorietaCampoSpecifico(String categoria, CampoObbligatorietaRequest request) {
        return catalogoService.setObbligatorietaCampoSpecifico(categoria, request);
    }

    public Proposta creaProposta(Categoria categoria) {
        return propostaService.creaProposta(categoria, getCampiBase(), getCampiComuni());
    }

    public List<String> validaCampo(Proposta proposta, Map<String, String> valoriCorrenti, String nomeCampo, String valore) {
        return propostaService.validaCampo(proposta, valoriCorrenti, nomeCampo, valore);
    }

    public PropostaValidationResult applicaValoriEValida(Proposta proposta, Map<String, String> valori) {
        return propostaService.applicaValoriEValida(proposta, valori);
    }

    public void salvaProposta(Proposta proposta) {
        propostaService.salvaProposta(proposta);
    }

    public List<Proposta> getProposteValide() {
        return propostaService.getProposteValide();
    }

    public void pubblicaProposta(Proposta proposta) {
        propostaService.pubblicaProposta(proposta);
        propostaService.rimuoviPropostaValida(proposta);
    }

    public Map<String, List<Proposta>> getBachecaPerCategoria() {
        return propostaService.getBachecaPerCategoria();
    }

    public List<Proposta> getProposteRitirabili() {
        return propostaService.getProposteRitirabili();
    }

    public void ritiraProposta(Proposta proposta) {
        propostaService.ritiraProposta(proposta);
    }

    public Map<StatoProposta, List<Proposta>> getPropostePerStato() {
        return propostaService.getPropostePerStato();
    }

    public void clearProposteValide() {
        propostaService.clearProposteValide();
    }

    public ImportResult importa(Path path) throws IOException {
        return batchImportService.importa(path);
    }
}
