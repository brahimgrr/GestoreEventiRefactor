package it.unibs.ingsoft.functional;

import it.unibs.ingsoft.application.catalogo.dto.CampoDefinitionRequest;
import it.unibs.ingsoft.domain.model.catalogo.TipoDato;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UC04_VisualizzareCampiCategorieTest {
    @Test
    void scenarioPrincipale_mostraCampiBaseCampiComuniCategorieECampiSpecifici() {
        FunctionalTestSupport.FunctionalGraph graph = FunctionalTestSupport.graph();
        graph.configuratoreService().configuraCampiBase(List.of());
        graph.configuratoreService().addCampoComune(new CampoDefinitionRequest("Note", TipoDato.STRINGA, false));
        graph.configuratoreService().createCategoria("Sport");
        graph.configuratoreService().addCampoSpecifico("sport",
                new CampoDefinitionRequest("Arbitro", TipoDato.BOOLEANO, false));

        assertAll(
                () -> assertEquals(8, graph.configuratoreService().getCampiBase().size()),
                () -> assertEquals("Note", graph.configuratoreService().getCampiComuni().get(0).getNome()),
                () -> assertEquals("Sport", graph.configuratoreService().getCategorie().get(0).getNome()),
                () -> assertEquals("Arbitro",
                        graph.configuratoreService().getCategorie().get(0).getCampiSpecifici().get(0).getNome())
        );
    }
}
