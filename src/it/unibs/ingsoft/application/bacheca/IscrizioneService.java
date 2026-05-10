package it.unibs.ingsoft.application.bacheca;

import it.unibs.ingsoft.application.proposta.PropostaLifecycleService;
import it.unibs.ingsoft.domain.AppConstants;
import it.unibs.ingsoft.domain.Bacheca;
import it.unibs.ingsoft.domain.Fruitore;
import it.unibs.ingsoft.domain.Proposta;
import it.unibs.ingsoft.domain.error.DomainErrorCode;
import it.unibs.ingsoft.domain.error.DomainException;
import it.unibs.ingsoft.persistence.interfaces.IBachecaRepository;

import java.time.LocalDate;
import java.util.Objects;

public final class IscrizioneService {

    private final IBachecaRepository bachecaRepo;
    private final PropostaLifecycleService propostaLifecycleService;

    public IscrizioneService(IBachecaRepository bachecaRepo, PropostaLifecycleService propostaLifecycleService) {
        this.bachecaRepo = Objects.requireNonNull(bachecaRepo);
        this.propostaLifecycleService = Objects.requireNonNull(propostaLifecycleService);
    }

    public void iscrivi(Proposta p, Fruitore f) {
        Bacheca bacheca = bachecaRepo.load();
        Proposta propostaPersistita = trovaPropostaPersistita(bacheca, p);
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        propostaPersistita.iscrivi(f.getUsername(), oggi);

        if (propostaPersistita.haNumeroPartecipantiCompleto()) {
            propostaLifecycleService.confermaPropostaSenzaSalvataggio(propostaPersistita);
        }

        bachecaRepo.save(bacheca);
    }

    public void disiscrivi(Proposta p, Fruitore f) {
        Bacheca bacheca = bachecaRepo.load();
        Proposta propostaPersistita = trovaPropostaPersistita(bacheca, p);
        propostaPersistita.disiscrivi(f.getUsername(), LocalDate.now(AppConstants.clock));
        bachecaRepo.save(bacheca);
    }

    private Proposta trovaPropostaPersistita(Bacheca bacheca, Proposta proposta) {
        if (proposta == null) {
            throw new DomainException(DomainErrorCode.PROPOSTA_NON_TROVATA);
        }
        String chiave = proposta.getChiaveIdentita();
        return bacheca.findByChiaveIdentita(chiave)
                .orElseThrow(() -> new DomainException(DomainErrorCode.PROPOSTA_NON_TROVATA, chiave));
    }
}
