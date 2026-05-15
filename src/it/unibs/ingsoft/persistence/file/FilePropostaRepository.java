package it.unibs.ingsoft.persistence.file;

import it.unibs.ingsoft.domain.repository.PropostaRepository;
import it.unibs.ingsoft.domain.model.proposta.Proposta;
import it.unibs.ingsoft.domain.model.proposta.ProposalFailure;
import it.unibs.ingsoft.domain.model.proposta.StatoProposta;
import it.unibs.ingsoft.domain.error.DomainException;
import it.unibs.ingsoft.persistence.file.document.PropostaStoreDocument;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public final class FilePropostaRepository
        extends AbstractFileRepository<PropostaStoreDocument>
        implements PropostaRepository {

    public FilePropostaRepository(Path path) {
        super(path, PropostaStoreDocument.class, PropostaStoreDocument::empty);
    }

    @Override
    public Optional<Proposta> findById(String id) {
        return super.load().findById(id);
    }

    @Override
    public List<Proposta> findAll() {
        return super.load().findAll();
    }

    @Override
    public List<Proposta> findOpen() {
        return super.load().findOpen();
    }

    @Override
    public List<Proposta> findByState(StatoProposta stato) {
        return super.load().findByState(stato);
    }

    @Override
    public void save(Proposta proposta) {
        super.save(super.load().save(proposta));
    }

    @Override
    public <R> R updateById(String id, Function<Proposta, R> operation) {
        Proposta proposta = findById(id)
                .orElseThrow(() -> new DomainException(new ProposalFailure.NotFound()));
        R result = operation.apply(proposta);
        save(proposta);
        return result;
    }
}
