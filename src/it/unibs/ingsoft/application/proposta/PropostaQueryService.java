package it.unibs.ingsoft.application.proposta;

import it.unibs.ingsoft.domain.proposta.Bacheca;
import it.unibs.ingsoft.domain.proposta.Proposta;
import it.unibs.ingsoft.domain.proposta.StatoProposta;
import it.unibs.ingsoft.persistence.interfaces.IBachecaRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Gestisce le query sulle proposte persistite in bacheca.
 */
public final class PropostaQueryService {
    private final IBachecaRepository bachecaRepo;

    public PropostaQueryService(IBachecaRepository bachecaRepo) {
        this.bachecaRepo = Objects.requireNonNull(bachecaRepo);
    }

    private Bacheca bacheca() {
        return bachecaRepo.load();
    }

    public List<Proposta> getTutteLeProposte() {
        return bacheca().getProposte();
    }

    public List<Proposta> getBacheca() {
        return bacheca().getProposte().stream()
                .filter(Proposta::isAperta)
                .collect(Collectors.toList());
    }

    public List<Proposta> getProposteAperteIscritteDa(String username) {
        if (username == null) {
            return List.of();
        }
        return bacheca().getProposte().stream()
                .filter(Proposta::isAperta)
                .filter(p -> p.isIscritto(username))
                .toList();
    }

    public List<Proposta> getProposteRitirabili() {
        Bacheca bacheca = bacheca();
        List<Proposta> ritirabili = new ArrayList<>();
        for (Proposta proposta : bacheca.getProposte()) {
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
        for (Proposta proposta : bacheca().getProposte()) {
            if (proposta.isAperta()) {
                mappa.computeIfAbsent(proposta.getCategoria().getNome(), k -> new ArrayList<>()).add(proposta);
            }
        }
        return mappa;
    }
}
