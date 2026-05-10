package it.unibs.ingsoft.application.bacheca;

import it.unibs.ingsoft.application.proposta.PropostaLifecycleService;
import it.unibs.ingsoft.domain.AppConstants;
import it.unibs.ingsoft.domain.Fruitore;
import it.unibs.ingsoft.domain.Proposta;
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
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        p.iscrivi(f.getUsername(), oggi);

        if (p.haNumeroPartecipantiCompleto()) {
            propostaLifecycleService.confermaProposta(p);
        }

        bachecaRepo.save();
    }

    public void disiscrivi(Proposta p, Fruitore f) {
        p.disiscrivi(f.getUsername(), LocalDate.now(AppConstants.clock));
        bachecaRepo.save();
    }
}
