package it.unibs.ingsoft.domain.proposta;

import it.unibs.ingsoft.domain.catalogo.Categoria;
import it.unibs.ingsoft.domain.shared.AppConstants;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PropostaIdentityPolicyTest {
    @Test
    void defaultPolicy_eNuovaIstanzaProduconoStessaChiave() {
        Map<String, String> valori = Map.of(AppConstants.CAMPO_TITOLO, "Torneo");

        assertEquals(
                PropostaIdentityPolicy.DEFAULT.chiaveDuplicato(valori),
                new PropostaIdentityPolicy().chiaveDuplicato(valori));
    }

    @Test
    void chiaveDuplicato_conMappaNormalizzaValoriEMancanti() {
        Map<String, String> valori = Map.of(
                AppConstants.CAMPO_TITOLO, " Torneo ",
                AppConstants.CAMPO_DATA, "25/12/2026",
                AppConstants.CAMPO_ORA, "16:30",
                AppConstants.CAMPO_LUOGO, " Brescia ");

        assertAll(
                () -> assertEquals("torneo|25/12/2026|16:30|brescia",
                        PropostaIdentityPolicy.DEFAULT.chiaveDuplicato(valori)),
                () -> assertEquals("|||", PropostaIdentityPolicy.DEFAULT.chiaveDuplicato(Map.of())),
                () -> assertEquals("torneo|||", PropostaIdentityPolicy.DEFAULT.chiaveDuplicato(
                        Map.of(AppConstants.CAMPO_TITOLO, " Torneo ")))
        );
    }

    @Test
    void chiaveDuplicato_conPropostaDelegaAiValoriCorrenti() {
        Proposta proposta = new Proposta(new Categoria("Sport"), List.of(), List.of());
        proposta.aggiornaValoriCampi(Map.of(
                AppConstants.CAMPO_TITOLO, "Torneo",
                AppConstants.CAMPO_DATA, "25/12/2026",
                AppConstants.CAMPO_ORA, "16:30",
                AppConstants.CAMPO_LUOGO, "Brescia"));

        assertEquals("torneo|25/12/2026|16:30|brescia",
                PropostaIdentityPolicy.DEFAULT.chiaveDuplicato(proposta));
    }

    @Test
    void chiaveDuplicato_conArgomentiNull_lanciaNullPointerException() {
        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> PropostaIdentityPolicy.DEFAULT.chiaveDuplicato((Proposta) null)),
                () -> assertThrows(NullPointerException.class,
                        () -> PropostaIdentityPolicy.DEFAULT.chiaveDuplicato((Map<String, String>) null))
        );
    }
}
