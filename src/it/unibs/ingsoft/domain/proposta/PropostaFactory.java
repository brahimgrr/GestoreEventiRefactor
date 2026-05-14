package it.unibs.ingsoft.domain.proposta;

import it.unibs.ingsoft.domain.catalogo.Campo;
import it.unibs.ingsoft.domain.catalogo.Categoria;

import java.util.List;

/**
 * Centralizza la creazione delle proposte.
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
