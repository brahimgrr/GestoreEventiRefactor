package it.unibs.ingsoft.application.proposta;

import it.unibs.ingsoft.domain.*;
import it.unibs.ingsoft.domain.error.DomainErrorCode;
import it.unibs.ingsoft.domain.error.DomainException;
import it.unibs.ingsoft.persistence.interfaces.IBachecaRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Gestisce salvataggio in memoria e pubblicazione delle proposte valide.
 */
public final class PropostaPublication_Service {
    private final IBachecaRepository bachecaRepo;
    private final PropostaQueryService queryService;
    private final List<Proposta> proposteValide = new ArrayList<>();

    public PropostaPublication_Service(IBachecaRepository bachecaRepo) {
        this(bachecaRepo, new PropostaQueryService(bachecaRepo));
    }

    public PropostaPublication_Service(IBachecaRepository bachecaRepo, PropostaQueryService queryService) {
        this.bachecaRepo = Objects.requireNonNull(bachecaRepo);
        this.queryService = Objects.requireNonNull(queryService);
    }

    /*
    COSTRUTTORE MAI USATO
     */
    private Bacheca bacheca() {
        return bachecaRepo.load();
    }

    public void salvaProposta(Proposta proposta) {
        proposta.verificaSalvabile();
        rilevaDuplicatoAlSalvataggio(proposta);
        proposteValide.add(proposta);
    }

    public List<Proposta> getProposteValide() {
        return Collections.unmodifiableList(proposteValide);
    }

    void rimuoviPropostaValida(Proposta proposta) {
        proposteValide.remove(proposta);
    }

    public void clearProposteValide() {
        proposteValide.clear();
    }

    public void pubblicaProposta(Proposta proposta) {
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        proposta.verificaPubblicabile(oggi);
        Bacheca bacheca = bachecaRepo.load();
        rilevaDuplicato(proposta, bacheca);

        proposta.pubblica(oggi);
        bacheca.addProposta(proposta);
        bachecaRepo.save(bacheca);
    }

    private void rilevaDuplicatoAlSalvataggio(Proposta proposta) {
        String chiave = proposta.getChiaveIdentita();

        boolean inBacheca = queryService.getTutteLeProposte().stream()
                .anyMatch(e -> e.getChiaveIdentita().equals(chiave));
        boolean inValide = proposteValide.stream()
                .anyMatch(e -> e.getChiaveIdentita().equals(chiave));

        if (inBacheca || inValide) {
            throw new DomainException(DomainErrorCode.PROPOSTA_DUPLICATA);
        }
    }

    private void rilevaDuplicato(Proposta proposta, Bacheca bacheca) {
        String chiave = proposta.getChiaveIdentita();

        if (bacheca.containsChiaveIdentita(chiave)) {
            throw new DomainException(DomainErrorCode.PROPOSTA_DUPLICATA);
        }
    }
}
