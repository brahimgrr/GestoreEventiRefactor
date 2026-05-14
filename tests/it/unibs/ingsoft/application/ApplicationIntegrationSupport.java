package it.unibs.ingsoft.application;

import it.unibs.ingsoft.application.bacheca.IscrizioneService;
import it.unibs.ingsoft.application.bacheca.NotificationService;
import it.unibs.ingsoft.application.catalogo.Catalogo_Service;
import it.unibs.ingsoft.application.proposta.PropostaCreationService;
import it.unibs.ingsoft.application.proposta.PropostaLifecycleService;
import it.unibs.ingsoft.application.proposta.PropostaPublication_Service;
import it.unibs.ingsoft.application.proposta.PropostaQueryService;
import it.unibs.ingsoft.application.proposta.Proposta_Service;
import it.unibs.ingsoft.domain.ArchivioNotifiche;
import it.unibs.ingsoft.domain.Bacheca;
import it.unibs.ingsoft.domain.Catalogo;
import it.unibs.ingsoft.domain.Credenziali;
import it.unibs.ingsoft.domain.factory.NotificaFactory;
import it.unibs.ingsoft.domain.factory.PropostaFactory;
import it.unibs.ingsoft.persistence.interfaces.IBachecaRepository;
import it.unibs.ingsoft.persistence.interfaces.ICatalogoRepository;
import it.unibs.ingsoft.persistence.interfaces.ICredenzialiRepository;
import it.unibs.ingsoft.persistence.interfaces.ISpazioPersonaleRepository;

public final class ApplicationIntegrationSupport {
    private ApplicationIntegrationSupport() {
    }

    public static ServiceGraph serviceGraph() {
        InMemoryBachecaRepository bachecaRepository = new InMemoryBachecaRepository();
        InMemorySpazioPersonaleRepository spazioPersonaleRepository = new InMemorySpazioPersonaleRepository();
        NotificationService notificationService = new NotificationService(spazioPersonaleRepository);
        PropostaQueryService queryService = new PropostaQueryService(bachecaRepository);
        PropostaPublication_Service publicationService = new PropostaPublication_Service(bachecaRepository, queryService);
        PropostaLifecycleService lifecycleService = new PropostaLifecycleService(
                bachecaRepository,
                notificationService,
                NotificaFactory.getInstance());
        Proposta_Service propostaService = new Proposta_Service(
                new PropostaCreationService(PropostaFactory.getInstance()),
                new it.unibs.ingsoft.application.proposta.PropostaValidationService(),
                publicationService,
                lifecycleService,
                queryService);
        return new ServiceGraph(
                new Catalogo_Service(new InMemoryCatalogoRepository()),
                propostaService,
                new FruitoreService(
                        propostaService,
                        new IscrizioneService(bachecaRepository, lifecycleService),
                        notificationService),
                bachecaRepository,
                spazioPersonaleRepository);
    }

    public record ServiceGraph(
            Catalogo_Service catalogoService,
            Proposta_Service propostaService,
            FruitoreService fruitoreService,
            InMemoryBachecaRepository bachecaRepository,
            InMemorySpazioPersonaleRepository spazioPersonaleRepository) {
    }

    public static final class InMemoryCredenzialiRepository implements ICredenzialiRepository {
        private Credenziali credenziali = new Credenziali();
        private int saveCount;

        @Override
        public Credenziali load() {
            return credenziali;
        }

        @Override
        public void save(Credenziali credenziali) {
            this.credenziali = credenziali;
            saveCount++;
        }

        public int saveCount() {
            return saveCount;
        }
    }

    public static final class InMemoryCatalogoRepository implements ICatalogoRepository {
        private Catalogo catalogo = new Catalogo();

        @Override
        public Catalogo load() {
            return catalogo;
        }

        @Override
        public void save(Catalogo catalogo) {
            this.catalogo = catalogo;
        }
    }

    public static final class InMemoryBachecaRepository implements IBachecaRepository {
        private Bacheca bacheca = new Bacheca();
        private int saveCount;

        @Override
        public Bacheca load() {
            return bacheca;
        }

        @Override
        public void save(Bacheca bacheca) {
            this.bacheca = bacheca;
            saveCount++;
        }

        public int saveCount() {
            return saveCount;
        }
    }

    public static final class InMemorySpazioPersonaleRepository implements ISpazioPersonaleRepository {
        private ArchivioNotifiche archivio = new ArchivioNotifiche();
        private int saveCount;

        @Override
        public ArchivioNotifiche load() {
            return archivio;
        }

        @Override
        public void save(ArchivioNotifiche archivio) {
            this.archivio = archivio;
            saveCount++;
        }

        public int saveCount() {
            return saveCount;
        }
    }
}
