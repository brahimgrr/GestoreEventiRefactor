package it.unibs.ingsoft.application;

import it.unibs.ingsoft.application.notifica.NotificationService;
import it.unibs.ingsoft.application.catalogo.CatalogoService;
import it.unibs.ingsoft.application.proposta.PropostaLifecycleService;
import it.unibs.ingsoft.application.proposta.PropostaPublicationService;
import it.unibs.ingsoft.application.proposta.PropostaQueryService;
import it.unibs.ingsoft.application.proposta.PropostaService;
import it.unibs.ingsoft.application.proposta.PropostaValidationService;
import it.unibs.ingsoft.persistence.dto.ArchivioNotificheDTO;
import it.unibs.ingsoft.persistence.dto.BachecaDTO;
import it.unibs.ingsoft.persistence.dto.CatalogoDTO;
import it.unibs.ingsoft.persistence.dto.CredenzialiDTO;
import it.unibs.ingsoft.domain.notifica.NotificaFactory;
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
        PropostaPublicationService publicationService = new PropostaPublicationService(bachecaRepository);
        PropostaLifecycleService lifecycleService = new PropostaLifecycleService(
                bachecaRepository,
                notificationService,
                NotificaFactory.getInstance());
        PropostaService propostaService = new PropostaService(
                new PropostaValidationService(),
                publicationService,
                lifecycleService,
                queryService);
        return new ServiceGraph(
                new CatalogoService(new InMemoryCatalogoRepository()),
                propostaService,
                new FruitoreService(
                        propostaService,
                        notificationService),
                bachecaRepository,
                spazioPersonaleRepository);
    }

    public record ServiceGraph(
            CatalogoService catalogoService,
            PropostaService propostaService,
            FruitoreService fruitoreService,
            InMemoryBachecaRepository bachecaRepository,
            InMemorySpazioPersonaleRepository spazioPersonaleRepository) {
    }

    public static final class InMemoryCredenzialiRepository implements ICredenzialiRepository {
        private CredenzialiDTO credenziali = new CredenzialiDTO();
        private int saveCount;

        @Override
        public CredenzialiDTO load() {
            return credenziali;
        }

        @Override
        public void save(CredenzialiDTO credenziali) {
            this.credenziali = credenziali;
            saveCount++;
        }

        public int saveCount() {
            return saveCount;
        }
    }

    public static final class InMemoryCatalogoRepository implements ICatalogoRepository {
        private CatalogoDTO catalogo = new CatalogoDTO();

        @Override
        public CatalogoDTO load() {
            return catalogo;
        }

        @Override
        public void save(CatalogoDTO catalogo) {
            this.catalogo = catalogo;
        }
    }

    public static final class InMemoryBachecaRepository implements IBachecaRepository {
        private BachecaDTO bacheca = new BachecaDTO();
        private int saveCount;

        @Override
        public BachecaDTO load() {
            return bacheca;
        }

        @Override
        public void save(BachecaDTO bacheca) {
            this.bacheca = bacheca;
            saveCount++;
        }

        public int saveCount() {
            return saveCount;
        }
    }

    public static final class InMemorySpazioPersonaleRepository implements ISpazioPersonaleRepository {
        private ArchivioNotificheDTO archivio = new ArchivioNotificheDTO();
        private int saveCount;

        @Override
        public ArchivioNotificheDTO load() {
            return archivio;
        }

        @Override
        public void save(ArchivioNotificheDTO archivio) {
            this.archivio = archivio;
            saveCount++;
        }

        public int saveCount() {
            return saveCount;
        }
    }
}
