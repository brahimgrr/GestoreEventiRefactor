package it.unibs.ingsoft.application.proposta;

import it.unibs.ingsoft.application.proposta.dto.PropostaValidationResult;
import it.unibs.ingsoft.domain.AppConstants;
import it.unibs.ingsoft.domain.Campo;
import it.unibs.ingsoft.domain.Categoria;
import it.unibs.ingsoft.domain.Proposta;
import it.unibs.ingsoft.domain.StatoProposta;
import it.unibs.ingsoft.domain.validation.ValidationError;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Facade compatibile per creazione, validazione, pubblicazione e query delle proposte.
 */
public final class PropostaService {
    public static final String CAMPO_TITOLO = AppConstants.CAMPO_TITOLO;
    public static final String CAMPO_TERMINE_ISCRIZIONE = AppConstants.CAMPO_TERMINE_ISCRIZIONE;
    public static final String CAMPO_DATA = AppConstants.CAMPO_DATA;
    public static final String CAMPO_DATA_CONCLUSIVA = AppConstants.CAMPO_DATA_CONCLUSIVA;
    public static final String CAMPO_ORA = AppConstants.CAMPO_ORA;
    public static final String CAMPO_LUOGO = AppConstants.CAMPO_LUOGO;
    public static final String CAMPO_QUOTA = AppConstants.CAMPO_QUOTA;
    public static final String CAMPO_NUM_PARTECIPANTI = AppConstants.CAMPO_NUM_PARTECIPANTI;

    private final PropostaCreationService creationService;
    private final PropostaValidationService validationService;
    private final PropostaPublicationService publicationService;
    private final PropostaLifecycleService lifecycleService;
    private final PropostaQueryService queryService;

    public PropostaService(PropostaCreationService creationService,
                           PropostaValidationService validationService,
                           PropostaPublicationService publicationService,
                           PropostaLifecycleService lifecycleService,
                           PropostaQueryService queryService) {
        this.creationService = Objects.requireNonNull(creationService);
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
        return creationService.creaProposta(categoria, campiBase, campiComuni);
    }

    public List<ValidationError> validaProposta(Proposta proposta) {
        return validationService.validaProposta(proposta);
    }

    public List<ValidationError> validaCampo(Proposta proposta, Map<String, String> valoriCorrenti, String nomeCampo, String valore) {
        return validationService.validaCampo(proposta, valoriCorrenti, nomeCampo, valore);
    }

    public void pubblicaProposta(Proposta proposta) {
        publicationService.pubblicaProposta(proposta);
        publicationService.rimuoviPropostaValida(proposta);
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

    public List<Campo> getCampiConErrore(Proposta proposta, List<ValidationError> errori) {
        return validationService.getCampiConErrore(proposta, errori);
    }

    public PropostaValidationResult applicaValoriEValida(Proposta proposta, Map<String, String> valori) {
        return validationService.applicaValoriEValida(proposta, valori);
    }
}
