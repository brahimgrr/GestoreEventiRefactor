package it.unibs.ingsoft.domain.repository;

import it.unibs.ingsoft.domain.model.proposta.Proposta;
import it.unibs.ingsoft.domain.model.proposta.StatoProposta;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public interface PropostaRepository {
    Optional<Proposta> findById(String id);

    List<Proposta> findAll();

    List<Proposta> findByState(StatoProposta stato);

    void save(Proposta proposta);

    <R> R updateById(String id, Function<Proposta, R> operation);
}
