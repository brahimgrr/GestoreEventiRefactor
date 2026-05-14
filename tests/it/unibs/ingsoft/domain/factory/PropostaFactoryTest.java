package it.unibs.ingsoft.domain.factory;

import it.unibs.ingsoft.domain.shared.error.DomainException;
import it.unibs.ingsoft.domain.catalogo.Categoria;
import it.unibs.ingsoft.domain.proposta.Proposta;
import it.unibs.ingsoft.domain.proposta.PropostaFactory;
import it.unibs.ingsoft.domain.proposta.StatoProposta;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PropostaFactoryTest {
    @Test
    void getInstance_quandoInvocatoDueVolte_restituisceStessaIstanza() {
        assertSame(PropostaFactory.getInstance(), PropostaFactory.getInstance());
    }

    @Test
    void creaProposta_conCategoriaValida_creaPropostaInBozza() {
        Proposta proposta = PropostaFactory.getInstance().creaProposta(new Categoria("Sport"), List.of(), List.of());

        assertEquals(StatoProposta.BOZZA, proposta.getStato());
    }

    @Test
    void creaProposta_conCategoriaNull_lanciaIllegalStateException() {
        assertThrows(DomainException.class,
                () -> PropostaFactory.getInstance().creaProposta(null, List.of(), List.of()));
    }
}
