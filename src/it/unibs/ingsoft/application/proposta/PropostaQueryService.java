package it.unibs.ingsoft.application.proposta;

import it.unibs.ingsoft.domain.repository.PropostaRepository;
import it.unibs.ingsoft.domain.model.proposta.Proposta;
import it.unibs.ingsoft.domain.model.proposta.StatoProposta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Gestisce le query sulle proposte persistite in bacheca.
 */
public final class PropostaQueryService {
    private final PropostaRepository propostaRepo;

    public PropostaQueryService(PropostaRepository propostaRepo) {
        this.propostaRepo = Objects.requireNonNull(propostaRepo);
    }

    public List<Proposta> getTutteLeProposte() {
        return propostaRepo.findAll();
    }

    public List<Proposta> getBacheca() {
        return propostaRepo.findOpen();
    }

    public List<Proposta> getProposteAperteIscritteDa(String username) {
        if (username == null) {
            return List.of();
        }
        return propostaRepo.findOpen().stream()
                .filter(Proposta::isAperta)
                .filter(p -> p.isIscritto(username))
                .toList();
    }

    public List<Proposta> getProposteRitirabili() {
        List<Proposta> ritirabili = new ArrayList<>();
        for (Proposta proposta : propostaRepo.findAll()) {
            if (proposta.isAperta() || proposta.isConfermata()) {
                ritirabili.add(proposta);
            }
        }
        return Collections.unmodifiableList(ritirabili);
    }

    public Map<StatoProposta, List<Proposta>> getPropostePerStato() {
        Map<StatoProposta, List<Proposta>> mappa = new LinkedHashMap<>();
        for (Proposta proposta : getTutteLeProposte()) {
            mappa.computeIfAbsent(proposta.getStato(), k -> new ArrayList<>()).add(proposta);
        }
        return mappa;
    }

    public Map<String, List<Proposta>> getBachecaPerCategoria() {
        Map<String, List<Proposta>> mappa = new LinkedHashMap<>();
        for (Proposta proposta : propostaRepo.findAll()) {
            if (proposta.isAperta()) {
                mappa.computeIfAbsent(proposta.getCategoria().getNome(), k -> new ArrayList<>()).add(proposta);
            }
        }
        return mappa;
    }
}
