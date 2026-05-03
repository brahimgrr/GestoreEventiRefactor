package it.unibs.ingsoft.application.proposta;

import it.unibs.ingsoft.domain.AppConstants;
import it.unibs.ingsoft.domain.Bacheca;
import it.unibs.ingsoft.domain.Proposta;
import it.unibs.ingsoft.domain.StatoProposta;
import it.unibs.ingsoft.persistence.api.IBachecaRepository;

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
        if (proposta.getStato() != StatoProposta.VALIDA) {
            throw new IllegalStateException("Solo una proposta VALIDA puÃ² essere salvata.");
        }
        rilevaDuplicatoAlSalvataggio(proposta);
        proposteValide.add(proposta);
    }

    public List<Proposta> getProposteValide() {
        return Collections.unmodifiableList(proposteValide);
    }

    public void rimuoviPropostaValida(Proposta proposta) {
        proposteValide.remove(proposta);
    }

    public void clearProposteValide() {
        proposteValide.clear();
    }

    public void pubblicaProposta(Proposta proposta) {
        if (proposta.getStato() != StatoProposta.VALIDA) {
            throw new IllegalStateException("La proposta deve essere in stato VALIDA per essere pubblicata.");
        }

        LocalDate oggi = LocalDate.now(AppConstants.clock);
        if (proposta.getTermineIscrizione() != null && !proposta.getTermineIscrizione().isAfter(oggi)) {
            throw new IllegalStateException(
                    "Non Ã¨ piÃ¹ possibile pubblicare: il termine di iscrizione ("
                            + proposta.getTermineIscrizione() + ") Ã¨ giÃ  scaduto. Rivalidare la proposta.");
        }

        rilevaDuplicato(proposta);

        proposta.setStato(StatoProposta.APERTA);
        proposta.setDataPubblicazione(oggi);
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
            throw new IllegalStateException(
                    "Esiste giÃ  una proposta con lo stesso Titolo, Data, Ora e Luogo.");
        }
    }

    private void rilevaDuplicato(Proposta proposta) {
        String chiave = proposta.getChiaveIdentita();

        boolean duplicato = queryService.getTutteLeProposte().stream()
                .anyMatch(e -> e.getChiaveIdentita().equals(chiave));

        if (duplicato) {
            throw new IllegalStateException("Esiste giÃ  una proposta con lo stesso Titolo, Data, Ora e Luogo.");
        }
    }
}
