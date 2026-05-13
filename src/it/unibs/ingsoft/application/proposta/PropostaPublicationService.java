package it.unibs.ingsoft.application.proposta;

import it.unibs.ingsoft.persistence.dto.BachecaDTO;
import it.unibs.ingsoft.domain.proposta.Proposta;
import it.unibs.ingsoft.domain.proposta.PropostaIdentityPolicy;
import it.unibs.ingsoft.domain.proposta.ProposalFailure;
import it.unibs.ingsoft.domain.shared.error.DomainException;
import it.unibs.ingsoft.domain.shared.AppConstants;
import it.unibs.ingsoft.persistence.interfaces.IBachecaRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Gestisce l'area temporanea di sessione delle proposte pronte alla pubblicazione
 * e la pubblicazione duratura in bacheca.
 */
public final class PropostaPublicationService {
    private final IBachecaRepository bachecaRepo;
    private final PropostaIdentityPolicy duplicatePolicy;
    private final PropostaCommandLock commandLock;
    private final List<Proposta> proposteProntePerPubblicazione = new ArrayList<>();

    public PropostaPublicationService(IBachecaRepository bachecaRepo) {
        this(bachecaRepo, PropostaIdentityPolicy.DEFAULT);
    }

    public PropostaPublicationService(IBachecaRepository bachecaRepo,
                                      PropostaIdentityPolicy identityPolicy) {
        this(bachecaRepo, identityPolicy, new PropostaCommandLock());
    }

    public PropostaPublicationService(IBachecaRepository bachecaRepo,
                                      PropostaIdentityPolicy duplicatePolicy,
                                      PropostaCommandLock commandLock) {
        this.bachecaRepo = Objects.requireNonNull(bachecaRepo);
        this.duplicatePolicy = Objects.requireNonNull(duplicatePolicy);
        this.commandLock = Objects.requireNonNull(commandLock);
    }

    public void salvaProposta(Proposta proposta) {
        commandLock.runLocked(() -> {
            proposta.verificaSalvabile();
            rilevaDuplicatoAlSalvataggio(proposta);
            proposteProntePerPubblicazione.add(proposta);
        });
    }

    public List<Proposta> getProposteValide() {
        return commandLock.callLocked(() -> Collections.unmodifiableList(
                new ArrayList<>(proposteProntePerPubblicazione)));
    }

    public void clearProposteValide() {
        commandLock.runLocked(proposteProntePerPubblicazione::clear);
    }

    public void pubblicaProposta(Proposta proposta) {
        commandLock.runLocked(() -> {
            LocalDate oggi = LocalDate.now(AppConstants.clock);
            proposta.verificaPubblicabile(oggi);
            BachecaDTO bacheca = bachecaRepo.load();
            rilevaDuplicato(proposta, bacheca);

            proposta.pubblica(oggi);
            bacheca.addProposta(proposta);
            bachecaRepo.save(bacheca);
            proposteProntePerPubblicazione.remove(proposta);
        });
    }

    private void rilevaDuplicatoAlSalvataggio(Proposta proposta) {
        String chiave = duplicatePolicy.chiaveDuplicato(proposta);

        boolean inBacheca = bachecaRepo.load().getProposte().stream()
                .anyMatch(e -> duplicatePolicy.chiaveDuplicato(e).equals(chiave));
        boolean inValide = proposteProntePerPubblicazione.stream()
                .anyMatch(e -> duplicatePolicy.chiaveDuplicato(e).equals(chiave));

        if (inBacheca || inValide) {
            throw new DomainException(new ProposalFailure.Duplicate());
        }
    }

    private void rilevaDuplicato(Proposta proposta, BachecaDTO bacheca) {
        String chiave = duplicatePolicy.chiaveDuplicato(proposta);

        if (bacheca.containsChiaveDuplicato(chiave)) {
            throw new DomainException(new ProposalFailure.Duplicate());
        }
    }
}
