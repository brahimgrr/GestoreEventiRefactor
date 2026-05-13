package it.unibs.ingsoft.domain;

import it.unibs.ingsoft.domain.catalogo.Campo;
import it.unibs.ingsoft.domain.catalogo.CampoFactory;
import it.unibs.ingsoft.domain.catalogo.Categoria;
import it.unibs.ingsoft.domain.proposta.Proposta;
import it.unibs.ingsoft.domain.proposta.PropostaStateChange;
import it.unibs.ingsoft.domain.proposta.StatoProposta;
import it.unibs.ingsoft.domain.shared.AppConstants;
import it.unibs.ingsoft.domain.shared.error.DomainErrorCode;
import it.unibs.ingsoft.domain.shared.error.DomainException;
import it.unibs.ingsoft.persistence.dto.BachecaDTO;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BachecaIdentityLookupTest {
    @Test
    void findsPersistedProposalByIdentityKey() {
        Proposta persisted = propostaConValori("Rassegna");
        Proposta selected = propostaConValori("  rassegna ");
        BachecaDTO bacheca = new BachecaDTO();
        bacheca.addProposta(persisted);

        Proposta found = bacheca.findSameIdentityAs(selected);

        assertSame(persisted, found);
    }

    @Test
    void findsPersistedProposalByStableIdBeforeNaturalKey() {
        Proposta persisted = propostaConValori("Rassegna");
        Proposta selected = propostaFromJsonWithId(persisted.getId(), "Titolo diverso");
        BachecaDTO bacheca = new BachecaDTO();
        bacheca.addProposta(persisted);

        Proposta found = bacheca.findSameIdentityAs(selected);

        assertSame(persisted, found);
    }

    @Test
    void fallsBackToNaturalKeyWhenStableIdDoesNotMatchPersistedData() {
        Proposta persisted = propostaConValori("Rassegna");
        Proposta selected = propostaConValori("  rassegna ");
        BachecaDTO bacheca = new BachecaDTO();
        bacheca.addProposta(persisted);

        Proposta found = bacheca.findSameIdentityAs(selected);

        assertSame(persisted, found);
    }

    @Test
    void throwsDomainErrorWhenProposalIsNull() {
        BachecaDTO bacheca = new BachecaDTO();

        DomainException exception = assertThrows(
                DomainException.class,
                () -> bacheca.findSameIdentityAs(null)
        );

        assertEquals(DomainErrorCode.PROPOSTA_NON_TROVATA, exception.code());
    }

    private static Proposta propostaConValori(String titolo) {
        CampoFactory campoFactory = CampoFactory.getInstance();
        List<Campo> campiBase = campoFactory.creaCampiBase();
        Proposta proposta = new Proposta(new Categoria("Cinema"), campiBase, List.of());

        LocalDate oggi = LocalDate.now(AppConstants.clock);
        Map<String, String> valori = new LinkedHashMap<>();
        valori.put(AppConstants.CAMPO_TITOLO, titolo);
        valori.put(AppConstants.CAMPO_TERMINE_ISCRIZIONE, oggi.plusDays(7).format(AppConstants.DATE_FMT));
        valori.put(AppConstants.CAMPO_DATA, oggi.plusDays(10).format(AppConstants.DATE_FMT));
        valori.put(AppConstants.CAMPO_DATA_CONCLUSIVA, oggi.plusDays(10).format(AppConstants.DATE_FMT));
        valori.put(AppConstants.CAMPO_ORA, "20:30");
        valori.put(AppConstants.CAMPO_LUOGO, "Brescia");
        valori.put(AppConstants.CAMPO_QUOTA, "12.50");
        valori.put(AppConstants.CAMPO_NUM_PARTECIPANTI, "2");
        proposta.aggiornaValoriCampi(valori);
        return proposta;
    }

    private static Proposta propostaFromJsonWithId(String id, String titolo) {
        Proposta proposta = Proposta.fromJson(
                id,
                CampoFactory.getInstance().creaCampiBase(),
                List.of(),
                new Categoria("Cinema"),
                valori(titolo),
                StatoProposta.BOZZA,
                null,
                null,
                null,
                List.of(),
                List.of(new PropostaStateChange(StatoProposta.BOZZA, LocalDate.now(AppConstants.clock)))
        );
        return proposta;
    }

    private static Map<String, String> valori(String titolo) {
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        Map<String, String> valori = new LinkedHashMap<>();
        valori.put(AppConstants.CAMPO_TITOLO, titolo);
        valori.put(AppConstants.CAMPO_TERMINE_ISCRIZIONE, oggi.plusDays(7).format(AppConstants.DATE_FMT));
        valori.put(AppConstants.CAMPO_DATA, oggi.plusDays(10).format(AppConstants.DATE_FMT));
        valori.put(AppConstants.CAMPO_DATA_CONCLUSIVA, oggi.plusDays(10).format(AppConstants.DATE_FMT));
        valori.put(AppConstants.CAMPO_ORA, "20:30");
        valori.put(AppConstants.CAMPO_LUOGO, "Brescia");
        valori.put(AppConstants.CAMPO_QUOTA, "12.50");
        valori.put(AppConstants.CAMPO_NUM_PARTECIPANTI, "2");
        return valori;
    }
}
