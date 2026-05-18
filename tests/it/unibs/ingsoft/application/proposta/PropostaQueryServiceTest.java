package it.unibs.ingsoft.application.proposta;

import it.unibs.ingsoft.application.ApplicationIntegrationSupport;
import it.unibs.ingsoft.domain.catalogo.Categoria;
import it.unibs.ingsoft.domain.proposta.Proposta;
import it.unibs.ingsoft.domain.proposta.StatoProposta;
import it.unibs.ingsoft.domain.shared.AppConstants;
import it.unibs.ingsoft.persistence.dto.BachecaDTO;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PropostaQueryServiceTest {
    @Test
    void costruttore_conRepositoryNull_lanciaNullPointerException() {
        assertThrows(NullPointerException.class, () -> new PropostaQueryService(null));
    }

    @Test
    void query_filtranoPerStatoCategoriaEIscrizione() {
        ApplicationIntegrationSupport.InMemoryBachecaRepository repo =
                new ApplicationIntegrationSupport.InMemoryBachecaRepository();
        Proposta aperta = proposta("Aperta", "Sport", StatoProposta.APERTA, List.of("mario"));
        Proposta confermata = proposta("Confermata", "Sport", StatoProposta.CONFERMATA, List.of("mario"));
        Proposta annullata = proposta("Annullata", "Musica", StatoProposta.ANNULLATA, List.of());
        repo.load().addProposta(aperta);
        repo.load().addProposta(confermata);
        repo.load().addProposta(annullata);
        PropostaQueryService service = new PropostaQueryService(repo);

        assertAll(
                () -> assertEquals(List.of(aperta, confermata, annullata), service.getTutteLeProposte()),
                () -> assertEquals(List.of(aperta), service.getBacheca()),
                () -> assertEquals(List.of(aperta), service.getProposteAperteIscritteDa("mario")),
                () -> assertTrue(service.getProposteAperteIscritteDa(null).isEmpty()),
                () -> assertEquals(List.of(aperta, confermata), service.getProposteRitirabili()),
                () -> assertEquals(List.of(aperta), service.getPropostePerStato().get(StatoProposta.APERTA)),
                () -> assertEquals(List.of(aperta), service.getBachecaPerCategoria().get("Sport")),
                () -> assertNull(service.getBachecaPerCategoria().get("Musica"))
        );
    }

    @Test
    void getProposteRitirabili_restituisceListaImmutabile() {
        ApplicationIntegrationSupport.InMemoryBachecaRepository repo =
                new ApplicationIntegrationSupport.InMemoryBachecaRepository();
        Proposta aperta = proposta("Aperta", "Sport", StatoProposta.APERTA, List.of());
        repo.load().addProposta(aperta);

        List<Proposta> ritirabili = new PropostaQueryService(repo).getProposteRitirabili();

        assertThrows(UnsupportedOperationException.class, () -> ritirabili.add(aperta));
    }

    private Proposta proposta(String titolo, String categoria, StatoProposta stato, List<String> aderenti) {
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        return Proposta.fromJson(
                titolo.toLowerCase(),
                List.of(),
                List.of(),
                new Categoria(categoria),
                Map.of(
                        AppConstants.CAMPO_TITOLO, titolo,
                        AppConstants.CAMPO_NUM_PARTECIPANTI, "2",
                        AppConstants.CAMPO_TERMINE_ISCRIZIONE, oggi.plusDays(1).format(AppConstants.DATE_FMT),
                        AppConstants.CAMPO_DATA, oggi.plusDays(4).format(AppConstants.DATE_FMT),
                        AppConstants.CAMPO_ORA, "16:30",
                        AppConstants.CAMPO_LUOGO, "Brescia"
                ),
                stato,
                oggi,
                oggi.plusDays(1),
                oggi.plusDays(4),
                aderenti,
                null);
    }
}
