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
public final class PropostaPublicationService {
    private final IBachecaRepository bachecaRepo;
    private final PropostaQueryService queryService;
    private final List<Proposta> proposteValide = new ArrayList<>();

    public PropostaPublicationService(IBachecaRepository bachecaRepo) {
        this(bachecaRepo, new PropostaQueryService(bachecaRepo));
    }

    public PropostaPublicationService(IBachecaRepository bachecaRepo, PropostaQueryService queryService) {
        this.bachecaRepo = Objects.requireNonNull(bachecaRepo);
        this.queryService = Objects.requireNonNull(queryService);
    }

    private Bacheca bacheca() {
        return bachecaRepo.get();
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
        rilevaDuplicato(proposta);

        proposta.pubblica(oggi);
        bacheca().addProposta(proposta);
        bachecaRepo.save();
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

    private void rilevaDuplicato(Proposta proposta) {
        String chiave = proposta.getChiaveIdentita();

        boolean duplicato = queryService.getTutteLeProposte().stream()
                .anyMatch(e -> e.getChiaveIdentita().equals(chiave));

        if (duplicato) {
            throw new DomainException(DomainErrorCode.PROPOSTA_DUPLICATA);
        }
    }
}
