package it.unibs.ingsoft.application.proposta;

import it.unibs.ingsoft.domain.Campo;
import it.unibs.ingsoft.domain.Categoria;
import it.unibs.ingsoft.domain.Proposta;
import it.unibs.ingsoft.domain.factory.PropostaFactory;

import java.util.List;
import java.util.Objects;

/**
 * Crea nuove proposte in stato iniziale.
 */
public final class PropostaCreationService {
    private final PropostaFactory propostaFactory;

    public PropostaCreationService(PropostaFactory propostaFactory) {
        this.propostaFactory = Objects.requireNonNull(propostaFactory);
    }

    public Proposta creaProposta(Categoria categoria, List<Campo> campiBase, List<Campo> campiComuni) {
        return propostaFactory.creaProposta(categoria, campiBase, campiComuni);
    }
}
