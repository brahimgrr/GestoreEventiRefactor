package it.unibs.ingsoft.application.proposta;

import it.unibs.ingsoft.domain.Bacheca;
import it.unibs.ingsoft.domain.Proposta;
import it.unibs.ingsoft.domain.StatoProposta;
import it.unibs.ingsoft.persistence.api.IBachecaRepository;

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
        return bachecaRepo.get();
    }

    public List<Proposta> getTutteLeProposte() {
        return Collections.unmodifiableList(bacheca().getProposte());
    }

    public List<Proposta> getBacheca() {
        return bacheca().getProposte().stream()
                .filter(p -> p.getStato() == StatoProposta.APERTA)
                .collect(Collectors.toList());
    }

    public List<Proposta> getProposteAperteIscritteDa(String username) {
        if (username == null) {
            return List.of();
        }
        return getBacheca().stream()
                .filter(p -> p.getListaAderenti().contains(username))
                .collect(Collectors.toUnmodifiableList());
    }

    public List<Proposta> getProposteRitirabili() {
        List<Proposta> ritirabili = new ArrayList<>(getBacheca());
        for (Proposta proposta : getTutteLeProposte()) {
            if (proposta.getStato() == StatoProposta.CONFERMATA && !ritirabili.contains(proposta)) {
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
            if (proposta.getStato() == StatoProposta.APERTA) {
                mappa.computeIfAbsent(proposta.getCategoria().getNome(), k -> new ArrayList<>()).add(proposta);
            }
        }
        return mappa;
    }
}
