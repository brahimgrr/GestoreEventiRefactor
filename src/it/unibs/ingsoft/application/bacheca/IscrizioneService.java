package it.unibs.ingsoft.application.bacheca;

import it.unibs.ingsoft.application.proposta.PropostaPublicationService;
import it.unibs.ingsoft.domain.AppConstants;
import it.unibs.ingsoft.domain.Fruitore;
import it.unibs.ingsoft.domain.Proposta;
import it.unibs.ingsoft.persistence.interfaces.IBachecaRepository;

import java.time.LocalDate;
import java.util.Objects;

public final class IscrizioneService {

    private final IBachecaRepository bachecaRepo;
    private final PropostaPublicationService propostaPublicationService;

    public IscrizioneService(IBachecaRepository bachecaRepo, PropostaPublicationService propostaPublicationService) {
        this.bachecaRepo = Objects.requireNonNull(bachecaRepo);
        this.propostaPublicationService = Objects.requireNonNull(propostaPublicationService);
    }

    public void iscrivi(Proposta p, Fruitore f) {
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        p.iscrivi(f.getUsername(), oggi);

        if (p.haNumeroPartecipantiCompleto()) {
            propostaPublicationService.confermaProposta(p);
        }

        bachecaRepo.save();
    }

    public void disiscrivi(Proposta p, Fruitore f) {
        p.disiscrivi(f.getUsername(), LocalDate.now(AppConstants.clock));
        bachecaRepo.save();
    }
}
