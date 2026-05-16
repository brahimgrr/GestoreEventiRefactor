package it.unibs.ingsoft.application.proposta;

import it.unibs.ingsoft.application.proposta.dto.PropostaValidationResult;
import it.unibs.ingsoft.domain.error.ValidationError;
import it.unibs.ingsoft.domain.model.catalogo.Campo;
import it.unibs.ingsoft.domain.model.catalogo.Categoria;
import it.unibs.ingsoft.domain.model.proposta.Proposta;
import it.unibs.ingsoft.domain.model.proposta.StatoProposta;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Facade compatibile per creazione, validazione, pubblicazione e query delle proposte.
 */
public final class PropostaService {
    private final PropostaValidationService validationService;
    private final PropostaPublicationService publicationService;
    private final PropostaLifecycleService lifecycleService;
    private final PropostaQueryService queryService;

    public PropostaService(PropostaValidationService validationService,
                           PropostaPublicationService publicationService,
                           PropostaLifecycleService lifecycleService,
                           PropostaQueryService queryService) {
        this.validationService = Objects.requireNonNull(validationService);
        this.publicationService = Objects.requireNonNull(publicationService);
        this.lifecycleService = Objects.requireNonNull(lifecycleService);
        this.queryService = Objects.requireNonNull(queryService);
    }

    public void salvaProposta(Proposta proposta) {
        publicationService.salvaProposta(proposta);
    }

    public List<Proposta> getProposteValide() {
        return publicationService.getProposteValide();
    }

    public void clearProposteValide() {
        publicationService.clearProposteValide();
    }

    public Proposta creaProposta(Categoria categoria, List<Campo> campiBase, List<Campo> campiComuni) {
        return new Proposta(categoria, campiBase, campiComuni);
    }

    public List<ValidationError> validaCampo(Campo campo, Map<String, String> valori) {
        return validationService.validaCampo(campo, valori);
    }

    public void pubblicaProposta(Proposta proposta) {
        publicationService.pubblicaProposta(proposta);
    }

    public void controllaScadenze() {
        lifecycleService.controllaScadenze();
    }

    public void confermaProposta(Proposta proposta) {
        lifecycleService.confermaProposta(proposta);
    }

    public void ritiraProposta(Proposta proposta) {
        lifecycleService.ritiraProposta(proposta);
    }

    public void iscrivi(Proposta proposta, String username) {
        lifecycleService.iscrivi(proposta, username);
    }

    public void disiscrivi(Proposta proposta, String username) {
        lifecycleService.disiscrivi(proposta, username);
    }

    public List<Proposta> getTutteLeProposte() {
        return queryService.getTutteLeProposte();
    }

    public List<Proposta> getBacheca() {
        return queryService.getBacheca();
    }

    public List<Proposta> getProposteAperteIscritteDa(String username) {
        return queryService.getProposteAperteIscritteDa(username);
    }

    public List<Proposta> getProposteRitirabili() {
        return queryService.getProposteRitirabili();
    }

    public Map<StatoProposta, List<Proposta>> getPropostePerStato() {
        return queryService.getPropostePerStato();
    }

    public Map<String, List<Proposta>> getBachecaPerCategoria() {
        return queryService.getBachecaPerCategoria();
    }


    public PropostaValidationResult applicaValoriEValida(Proposta proposta, Map<String, String> valori) {
        return validationService.applicaValoriEValida(proposta, valori);
    }
}
