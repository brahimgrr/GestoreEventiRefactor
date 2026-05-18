package it.unibs.ingsoft.application.proposta;

import it.unibs.ingsoft.application.ApplicationIntegrationSupport;
import it.unibs.ingsoft.domain.catalogo.Categoria;
import it.unibs.ingsoft.domain.proposta.ProposalFailure;
import it.unibs.ingsoft.domain.proposta.Proposta;
import it.unibs.ingsoft.domain.proposta.PropostaIdentityPolicy;
import it.unibs.ingsoft.domain.proposta.StatoProposta;
import it.unibs.ingsoft.domain.shared.AppConstants;
import it.unibs.ingsoft.domain.shared.error.DomainException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PropostaPublicationServiceTest {
    @Test
    void costruttori_conDipendenzeNull_lancianoNullPointerException() {
        ApplicationIntegrationSupport.InMemoryBachecaRepository repo =
                new ApplicationIntegrationSupport.InMemoryBachecaRepository();
        PropostaCommandLock lock = new PropostaCommandLock();

        assertAll(
                () -> assertThrows(NullPointerException.class, () -> new PropostaPublicationService(null)),
                () -> assertThrows(NullPointerException.class,
                        () -> new PropostaPublicationService(repo, null)),
                () -> assertThrows(NullPointerException.class,
                        () -> new PropostaPublicationService(repo, PropostaIdentityPolicy.DEFAULT, null)),
                () -> assertDoesNotThrow(() -> new PropostaPublicationService(repo, PropostaIdentityPolicy.DEFAULT, lock))
        );
    }

    @Test
    void salvaProposta_conValida_aggiungeAListaImmutabileEClearSvuota() {
        PropostaPublicationService service = new PropostaPublicationService(
                new ApplicationIntegrationSupport.InMemoryBachecaRepository());
        Proposta proposta = propostaValida("Torneo");

        service.salvaProposta(proposta);
        List<Proposta> pronte = service.getProposteValide();

        assertAll(
                () -> assertEquals(List.of(proposta), pronte),
                () -> assertThrows(UnsupportedOperationException.class, () -> pronte.add(proposta))
        );

        service.clearProposteValide();

        assertTrue(service.getProposteValide().isEmpty());
    }

    @Test
    void salvaProposta_conDuplicataInListaOInBacheca_lanciaDuplicate() {
        ApplicationIntegrationSupport.InMemoryBachecaRepository repo =
                new ApplicationIntegrationSupport.InMemoryBachecaRepository();
        PropostaPublicationService service = new PropostaPublicationService(repo);
        Proposta prima = propostaValida("Duplicata");
        Proposta seconda = propostaValida("Duplicata");
        Proposta inBacheca = propostaValida("Gia in bacheca");
        Proposta duplicataBacheca = propostaValida("Gia in bacheca");
        repo.load().addProposta(inBacheca);

        service.salvaProposta(prima);
        DomainException inValide = assertThrows(DomainException.class, () -> service.salvaProposta(seconda));
        DomainException inRepo = assertThrows(DomainException.class, () -> service.salvaProposta(duplicataBacheca));

        assertAll(
                () -> assertInstanceOf(ProposalFailure.Duplicate.class, inValide.failure()),
                () -> assertInstanceOf(ProposalFailure.Duplicate.class, inRepo.failure())
        );
    }

    @Test
    void pubblicaProposta_conValida_persisteInBachecaERimuoveDaPronte() {
        ApplicationIntegrationSupport.InMemoryBachecaRepository repo =
                new ApplicationIntegrationSupport.InMemoryBachecaRepository();
        PropostaPublicationService service = new PropostaPublicationService(repo);
        Proposta proposta = propostaValida("Pubblicata");
        service.salvaProposta(proposta);

        service.pubblicaProposta(proposta);

        assertAll(
                () -> assertEquals(StatoProposta.APERTA, proposta.getStato()),
                () -> assertEquals(List.of(proposta), repo.load().getProposte()),
                () -> assertTrue(service.getProposteValide().isEmpty()),
                () -> assertEquals(1, repo.saveCount())
        );
    }

    @Test
    void pubblicaProposta_conDuplicataInBacheca_lanciaDuplicate() {
        ApplicationIntegrationSupport.InMemoryBachecaRepository repo =
                new ApplicationIntegrationSupport.InMemoryBachecaRepository();
        PropostaPublicationService service = new PropostaPublicationService(repo);
        repo.load().addProposta(propostaValida("Duplicata"));
        Proposta duplicata = propostaValida("Duplicata");

        DomainException exception = assertThrows(DomainException.class,
                () -> service.pubblicaProposta(duplicata));

        assertInstanceOf(ProposalFailure.Duplicate.class, exception.failure());
    }

    private Proposta propostaValida(String titolo) {
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        return Proposta.fromJson(
                titolo.toLowerCase(),
                List.of(),
                List.of(),
                new Categoria("Sport"),
                Map.of(
                        AppConstants.CAMPO_TITOLO, titolo,
                        AppConstants.CAMPO_NUM_PARTECIPANTI, "2",
                        AppConstants.CAMPO_TERMINE_ISCRIZIONE, oggi.plusDays(1).format(AppConstants.DATE_FMT),
                        AppConstants.CAMPO_DATA, oggi.plusDays(4).format(AppConstants.DATE_FMT),
                        AppConstants.CAMPO_ORA, "16:30",
                        AppConstants.CAMPO_LUOGO, "Brescia"
                ),
                StatoProposta.VALIDA,
                null,
                oggi.plusDays(1),
                oggi.plusDays(4),
                List.of(),
                null);
    }
}
