package it.unibs.ingsoft.application.proposta;

import it.unibs.ingsoft.application.notifica.NotificationService;
import it.unibs.ingsoft.domain.catalogo.Categoria;
import it.unibs.ingsoft.domain.catalogo.CampoFactory;
import it.unibs.ingsoft.domain.notifica.NotificaFactory;
import it.unibs.ingsoft.domain.proposta.Bacheca;
import it.unibs.ingsoft.domain.proposta.Proposta;
import it.unibs.ingsoft.domain.proposta.PropostaIdentityPolicy;
import it.unibs.ingsoft.domain.shared.AppConstants;
import it.unibs.ingsoft.persistence.file.FileBachecaRepository;
import it.unibs.ingsoft.persistence.file.FileSpazioPersonaleRepository;
import it.unibs.ingsoft.persistence.interfaces.IBachecaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ProposalPersistenceServiceTest {
    @TempDir
    Path tempDir;
    private static final PropostaIdentityPolicy IDENTITY = PropostaIdentityPolicy.DEFAULT;

    @Test
    void iscrizionePersistsRehydratedProposalSelectedFromQuery() {
        Fixture fixture = Fixture.create(tempDir, 2);
        Proposta selected = new PropostaQueryService(fixture.bachecaRepo).getBacheca().get(0);

        fixture.lifecycleService.iscrivi(selected, "alice");

        Proposta persisted = fixture.reloadPersistedProposal();
        assertTrue(persisted.isIscritto("alice"));
    }

    @Test
    void iscrizioneThatReachesCapacityPersistsConfirmedState() {
        Fixture fixture = Fixture.create(tempDir, 1);
        Proposta selected = new PropostaQueryService(fixture.bachecaRepo).getBacheca().get(0);

        fixture.lifecycleService.iscrivi(selected, "alice");

        Proposta persisted = fixture.reloadPersistedProposal();
        assertTrue(persisted.isConfermata());
    }

    @Test
    void publicLifecycleConfirmationPersistsStateChange() {
        Fixture fixture = Fixture.create(tempDir, 1);
        Proposta selected = new PropostaQueryService(fixture.bachecaRepo).getBacheca().get(0);
        selected.iscrivi("alice", LocalDate.now(AppConstants.clock));
        Bacheca bacheca = fixture.bachecaRepo.load();
        bacheca.findByChiaveDuplicato(IDENTITY.chiaveDuplicato(selected))
                .orElseThrow()
                .iscrivi("alice", LocalDate.now(AppConstants.clock));
        fixture.bachecaRepo.save(bacheca);

        fixture.lifecycleService.confermaProposta(selected);

        Proposta persisted = fixture.reloadPersistedProposal();
        assertTrue(persisted.isConfermata());
    }

    @Test
    void savedValidProposalsAreSessionScopedAndClearedOnLogout() {
        IBachecaRepository bachecaRepo = new FileBachecaRepository(tempDir.resolve("proposte.json"));
        PropostaPublicationService publicationService = new PropostaPublicationService(bachecaRepo);
        Proposta proposta = draftValidProposal(2);

        publicationService.salvaProposta(proposta);

        assertEquals(List.of(proposta), publicationService.getProposteValide());
        assertTrue(bachecaRepo.load().getProposte().isEmpty());

        PropostaPublicationService reconstructed = new PropostaPublicationService(bachecaRepo);
        assertTrue(reconstructed.getProposteValide().isEmpty());

        publicationService.clearProposteValide();
        assertTrue(publicationService.getProposteValide().isEmpty());
    }

    @Test
    void publicationRemovesProposalFromSessionStagingAndPersistsIt() {
        IBachecaRepository bachecaRepo = new FileBachecaRepository(tempDir.resolve("proposte.json"));
        PropostaPublicationService publicationService = new PropostaPublicationService(bachecaRepo);
        Proposta proposta = draftValidProposal(2);
        publicationService.salvaProposta(proposta);

        publicationService.pubblicaProposta(proposta);

        assertTrue(publicationService.getProposteValide().isEmpty());
        Proposta persisted = bachecaRepo.load().findSameIdentityAs(proposta);
        assertTrue(persisted.isAperta());
        assertEquals(proposta.getId(), persisted.getId());
    }

    private static final class Fixture {
        private final IBachecaRepository bachecaRepo;
        private final PropostaLifecycleService lifecycleService;
        private final String chiave;

        private Fixture(IBachecaRepository bachecaRepo,
                        PropostaLifecycleService lifecycleService,
                        String chiave) {
            this.bachecaRepo = bachecaRepo;
            this.lifecycleService = lifecycleService;
            this.chiave = chiave;
        }

        static Fixture create(Path tempDir, int numeroPartecipanti) {
            IBachecaRepository bachecaRepo = new FileBachecaRepository(tempDir.resolve("proposte.json"));
            NotificationService notificationService = new NotificationService(
                    new FileSpazioPersonaleRepository(tempDir.resolve("notifiche.json"))
            );
            PropostaLifecycleService lifecycleService = new PropostaLifecycleService(
                    bachecaRepo,
                    notificationService,
                    NotificaFactory.getInstance()
            );
            Proposta proposta = publishedProposal(numeroPartecipanti);
            Bacheca bacheca = new Bacheca();
            bacheca.addProposta(proposta);
            bachecaRepo.save(bacheca);

            return new Fixture(bachecaRepo, lifecycleService, IDENTITY.chiaveDuplicato(proposta));
        }

        Proposta reloadPersistedProposal() {
            return bachecaRepo.load()
                    .findByChiaveDuplicato(chiave)
                    .orElseThrow();
        }

        private static Proposta publishedProposal(int numeroPartecipanti) {
            Proposta proposta = draftValidProposal(numeroPartecipanti);
            proposta.pubblica(LocalDate.now(AppConstants.clock));
            return proposta;
        }
    }

    private static Proposta draftValidProposal(int numeroPartecipanti) {
        Proposta proposta = draftProposal();
        assertTrue(new PropostaValidationService()
                .applicaValoriEValida(proposta, valoriValidi(numeroPartecipanti))
                .valida());
        return proposta;
    }

    private static Proposta draftProposal() {
            CampoFactory campoFactory = CampoFactory.getInstance();
            Categoria categoria = new Categoria("Cinema");
        return new Proposta(
                    categoria,
                    campoFactory.creaCampiBase(),
                    List.of()
            );
    }

    private static Map<String, String> valoriValidi(int numeroPartecipanti) {
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        Map<String, String> valori = new LinkedHashMap<>();
        valori.put(AppConstants.CAMPO_TITOLO, "Rassegna");
        valori.put(AppConstants.CAMPO_TERMINE_ISCRIZIONE, oggi.plusDays(7).format(AppConstants.DATE_FMT));
        valori.put(AppConstants.CAMPO_DATA, oggi.plusDays(10).format(AppConstants.DATE_FMT));
        valori.put(AppConstants.CAMPO_DATA_CONCLUSIVA, oggi.plusDays(10).format(AppConstants.DATE_FMT));
        valori.put(AppConstants.CAMPO_ORA, "20:30");
        valori.put(AppConstants.CAMPO_LUOGO, "Brescia");
        valori.put(AppConstants.CAMPO_QUOTA, "12.50");
        valori.put(AppConstants.CAMPO_NUM_PARTECIPANTI, Integer.toString(numeroPartecipanti));
        return valori;
    }
}
