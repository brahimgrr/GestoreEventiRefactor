package it.unibs.ingsoft.application;

import it.unibs.ingsoft.application.notifica.NotificationService;
import it.unibs.ingsoft.application.catalogo.CatalogoService;
import it.unibs.ingsoft.domain.repository.CatalogoRepository;
import it.unibs.ingsoft.domain.repository.NotificationRepository;
import it.unibs.ingsoft.domain.repository.PropostaRepository;
import it.unibs.ingsoft.domain.repository.UserRepository;
import it.unibs.ingsoft.application.proposta.PropostaLifecycleService;
import it.unibs.ingsoft.application.proposta.PropostaPublicationService;
import it.unibs.ingsoft.application.proposta.PropostaQueryService;
import it.unibs.ingsoft.application.proposta.PropostaService;
import it.unibs.ingsoft.application.proposta.PropostaValidationService;
import it.unibs.ingsoft.domain.model.catalogo.Catalogo;
import it.unibs.ingsoft.domain.model.notifica.Notifica;
import it.unibs.ingsoft.domain.model.notifica.NotificaFactory;
import it.unibs.ingsoft.domain.model.proposta.Proposta;
import it.unibs.ingsoft.domain.model.proposta.ProposalFailure;
import it.unibs.ingsoft.domain.model.proposta.StatoProposta;
import it.unibs.ingsoft.domain.error.DomainException;
import it.unibs.ingsoft.domain.model.utente.UserAccount;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

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

    public static final class InMemoryCredenzialiRepository implements UserRepository {
        private final Map<String, UserAccount> users = new LinkedHashMap<>();
        private int saveCount;

        @Override
        public Optional<UserAccount> findByUsername(String username) {
            return Optional.ofNullable(users.get(UserAccount.normalize(username)));
        }

        @Override
        public boolean existsByUsername(String username) {
            return findByUsername(username).isPresent();
        }

        @Override
        public void save(UserAccount account) {
            users.put(account.normalizedUsername(), account);
            saveCount++;
        }

        public int saveCount() {
            return saveCount;
        }
    }

    public static final class InMemoryCatalogoRepository implements CatalogoRepository {
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

    public static final class InMemoryBachecaRepository implements PropostaRepository {
        private final List<Proposta> proposte = new ArrayList<>();
        private int saveCount;

        @Override
        public Optional<Proposta> findById(String id) {
            return proposte.stream()
                    .filter(proposta -> proposta.getId().equals(id))
                    .findFirst();
        }

        @Override
        public List<Proposta> findAll() {
            return List.copyOf(proposte);
        }

        @Override
        public List<Proposta> findByState(StatoProposta stato) {
            return proposte.stream().filter(proposta -> proposta.getStato() == stato).toList();
        }

        @Override
        public void save(Proposta proposta) {
            for (int i = 0; i < proposte.size(); i++) {
                if (proposta.getId().equals(proposte.get(i).getId())) {
                    proposte.set(i, proposta);
                    saveCount++;
                    return;
                }
            }
            proposte.add(proposta);
            saveCount++;
        }

        @Override
        public <R> R updateById(String id, Function<Proposta, R> operation) {
            Proposta proposta = findById(id)
                    .orElseThrow(() -> new DomainException(new ProposalFailure.NotFound()));
            R result = operation.apply(proposta);
            save(proposta);
            return result;
        }

        public int saveCount() {
            return saveCount;
        }
    }

    public static final class InMemorySpazioPersonaleRepository implements NotificationRepository {
        private final Map<String, List<Notifica>> notifiche = new LinkedHashMap<>();
        private int saveCount;

        @Override
        public List<Notifica> findByUsername(String username) {
            if (username == null) {
                return List.of();
            }
            return List.copyOf(notifiche.getOrDefault(UserAccount.normalize(username), List.of()));
        }

        @Override
        public void add(String username, Notifica notifica) {
            notifiche.computeIfAbsent(UserAccount.normalize(username), ignored -> new ArrayList<>()).add(notifica);
            saveCount++;
        }

        @Override
        public boolean delete(String username, String notificationId) {
            List<Notifica> userNotifications = notifiche.get(UserAccount.normalize(username));
            if (userNotifications == null) {
                return false;
            }
            boolean removed = userNotifications.removeIf(notifica -> notificationId.equals(notifica.id()));
            if (removed) {
                saveCount++;
            }
            return removed;
        }

        public int saveCount() {
            return saveCount;
        }
    }
}
