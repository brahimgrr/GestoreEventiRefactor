package it.unibs.ingsoft.domain.factory;

import it.unibs.ingsoft.domain.Campo;
import it.unibs.ingsoft.domain.Categoria;
import it.unibs.ingsoft.domain.Proposta;

import java.util.List;

/**
 * Centralizza la creazione iniziale delle proposte.
 */
public final class PropostaFactory {
    private static PropostaFactory instance;

    private PropostaFactory() {
    }

    public static PropostaFactory getInstance() {
        if (instance == null) {
            instance = new PropostaFactory();
        }
        return instance;
    }

    public Proposta creaProposta(Categoria categoria, List<Campo> campiBase, List<Campo> campiComuni) {
        return new Proposta(categoria, campiBase, campiComuni);
    }
}
