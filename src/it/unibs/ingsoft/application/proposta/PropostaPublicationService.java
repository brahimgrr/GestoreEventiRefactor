package it.unibs.ingsoft.application.proposta;

import it.unibs.ingsoft.domain.AppConstants;
import it.unibs.ingsoft.domain.error.DomainException;
import it.unibs.ingsoft.domain.model.proposta.ProposalFailure;
import it.unibs.ingsoft.domain.model.proposta.Proposta;
import it.unibs.ingsoft.domain.model.proposta.PropostaIdentityPolicy;
import it.unibs.ingsoft.domain.repository.PropostaRepository;

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
    private final PropostaRepository propostaRepo;
    private final PropostaIdentityPolicy duplicatePolicy;
    private final PropostaCommandLock commandLock;
    private final List<Proposta> proposteProntePerPubblicazione = new ArrayList<>();

    public PropostaPublicationService(PropostaRepository propostaRepo) {
        this(propostaRepo, PropostaIdentityPolicy.DEFAULT);
    }

    public PropostaPublicationService(PropostaRepository propostaRepo,
                                      PropostaIdentityPolicy identityPolicy) {
        this(propostaRepo, identityPolicy, new PropostaCommandLock());
    }

    public PropostaPublicationService(PropostaRepository propostaRepo,
                                      PropostaIdentityPolicy duplicatePolicy,
                                      PropostaCommandLock commandLock) {
        this.propostaRepo = Objects.requireNonNull(propostaRepo);
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
            rilevaDuplicato(proposta);

            proposta.pubblica(oggi);
            propostaRepo.save(proposta);
            proposteProntePerPubblicazione.remove(proposta);
        });
    }

    private void rilevaDuplicatoAlSalvataggio(Proposta proposta) {
        String chiave = duplicatePolicy.chiaveDuplicato(proposta);

        boolean inBacheca = propostaRepo.findAll().stream()
                .anyMatch(e -> duplicatePolicy.chiaveDuplicato(e).equals(chiave));
        boolean inValide = proposteProntePerPubblicazione.stream()
                .anyMatch(e -> duplicatePolicy.chiaveDuplicato(e).equals(chiave));

        if (inBacheca || inValide) {
            throw new DomainException(new ProposalFailure.Duplicate());
        }
    }

    private void rilevaDuplicato(Proposta proposta) {
        String chiave = duplicatePolicy.chiaveDuplicato(proposta);

        if (propostaRepo.findAll().stream().anyMatch(e -> duplicatePolicy.chiaveDuplicato(e).equals(chiave))) {
            throw new DomainException(new ProposalFailure.Duplicate());
        }
    }
}
