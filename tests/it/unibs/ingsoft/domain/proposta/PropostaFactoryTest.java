package it.unibs.ingsoft.domain.proposta;

import it.unibs.ingsoft.domain.shared.error.DomainException;
import it.unibs.ingsoft.domain.catalogo.Categoria;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PropostaFactoryTest {
    @Test
    void getInstance_quandoInvocatoDueVolte_restituisceStessaIstanza() throws Exception {
        Field instance = PropostaFactory.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);

        assertSame(PropostaFactory.getInstance(), PropostaFactory.getInstance());
    }

    @Test
    void costruttorePrivato_creaIstanzaQuandoInvocatoViaReflection() throws Exception {
        Constructor<PropostaFactory> constructor = PropostaFactory.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        assertNotNull(constructor.newInstance());
    }

    @Test
    void creaProposta_conCategoriaValida_creaPropostaInBozza() {
        Proposta proposta = PropostaFactory.getInstance().creaProposta(new Categoria("Sport"), List.of(), List.of());

        assertAll(
                () -> assertEquals(StatoProposta.BOZZA, proposta.getStato()),
                () -> assertEquals("Sport", proposta.getCategoria().getNome())
        );
    }

    @Test
    void creaProposta_conCategoriaNull_lanciaIllegalStateException() {
        DomainException exception = assertThrows(DomainException.class,
                () -> PropostaFactory.getInstance().creaProposta(null, List.of(), List.of()));

        assertInstanceOf(ProposalFailure.NullCategory.class, exception.failure());
    }

    @Test
    void creaProposta_conListeNull_creaPropostaSenzaCampi() {
        Proposta proposta = PropostaFactory.getInstance().creaProposta(new Categoria("Sport"), null, null);

        assertTrue(proposta.getCampi().isEmpty());
    }
}
