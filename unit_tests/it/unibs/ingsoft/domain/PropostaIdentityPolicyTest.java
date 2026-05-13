package it.unibs.ingsoft.domain;

import it.unibs.ingsoft.domain.proposta.PropostaIdentityPolicy;
import it.unibs.ingsoft.domain.shared.AppConstants;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PropostaIdentityPolicyTest {
    @Test
    void identityKeyUsesTrimmedLowercaseCoreFields() {
        Map<String, String> valori = new LinkedHashMap<>();
        valori.put(AppConstants.CAMPO_TITOLO, "  Rassegna Cinema  ");
        valori.put(AppConstants.CAMPO_DATA, "20/05/2026");
        valori.put(AppConstants.CAMPO_ORA, " 20:30 ");
        valori.put(AppConstants.CAMPO_LUOGO, " Brescia ");

        String chiave = PropostaIdentityPolicy.DEFAULT.chiaveDuplicato(valori);

        assertEquals("rassegna cinema|20/05/2026|20:30|brescia", chiave);
    }
}
