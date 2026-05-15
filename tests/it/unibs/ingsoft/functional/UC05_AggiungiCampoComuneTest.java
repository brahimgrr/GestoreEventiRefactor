package it.unibs.ingsoft.functional;

import it.unibs.ingsoft.application.catalogo.dto.CampoDefinitionRequest;
import it.unibs.ingsoft.domain.model.catalogo.CatalogFailure;
import it.unibs.ingsoft.domain.model.catalogo.TipoDato;
import it.unibs.ingsoft.domain.error.DomainException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UC05_AggiungiCampoComuneTest {
    @Test
    void scenarioPrincipale_campoValido_aggiungeCampoComuneAlCatalogo() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();
        graph.configuratoreService().configuraCampiBase(List.of());

        graph.configuratoreService().addCampoComune(new CampoDefinitionRequest("Note", TipoDato.STRINGA, false));

        assertEquals("Note", graph.configuratoreService().getCampiComuni().get(0).getNome());
    }

    @Test
    void scenarioAlternativo3a_nomeGiaUsato_segnalaErrore() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();
        graph.configuratoreService().configuraCampiBase(List.of());

        DomainException exception = assertThrows(DomainException.class,
                () -> graph.configuratoreService().addCampoComune(
                        new CampoDefinitionRequest("Titolo", TipoDato.STRINGA, false)));

        assertInstanceOf(CatalogFailure.FieldDuplicated.class, exception.failure());
    }

    @Test
    void scenarioAlternativo3b_nomeVuoto_segnalaErrore() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();
        graph.configuratoreService().configuraCampiBase(List.of());

        DomainException exception = assertThrows(DomainException.class,
                () -> graph.configuratoreService().addCampoComune(
                        new CampoDefinitionRequest("", TipoDato.STRINGA, false)));

        assertInstanceOf(CatalogFailure.FieldNameInvalid.class, exception.failure());
    }
}
