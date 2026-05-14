package it.unibs.ingsoft.functional;

import it.unibs.ingsoft.application.ApplicationIntegrationSupport;
import it.unibs.ingsoft.application.ConfiguratoreService;
import it.unibs.ingsoft.application.FruitoreService;
import it.unibs.ingsoft.application.authentication.AuthenticationService;
import it.unibs.ingsoft.application.batch.BatchImportService;
import it.unibs.ingsoft.application.catalogo.CatalogoService;
import it.unibs.ingsoft.application.catalogo.dto.CampoBaseExtraRequest;
import it.unibs.ingsoft.application.proposta.PropostaService;
import it.unibs.ingsoft.domain.shared.AppConstants;
import it.unibs.ingsoft.domain.catalogo.Campo;
import it.unibs.ingsoft.domain.catalogo.Categoria;
import it.unibs.ingsoft.domain.notifica.Notifica;
import it.unibs.ingsoft.domain.notifica.NotificaType;
import it.unibs.ingsoft.domain.proposta.Proposta;
import it.unibs.ingsoft.domain.proposta.PropostaStateChange;
import it.unibs.ingsoft.domain.proposta.StatoProposta;
import it.unibs.ingsoft.domain.catalogo.TipoDato;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public final class FunctionalTestSupport {
    private FunctionalTestSupport() {
    }

    public static FunctionalGraph graph() {
        ApplicationIntegrationSupport.ServiceGraph app = ApplicationIntegrationSupport.serviceGraph();
        BatchImportService batch = new BatchImportService(app.catalogoService(), app.propostaService());
        return new FunctionalGraph(
                app.catalogoService(),
                app.propostaService(),
                app.fruitoreService(),
                new ConfiguratoreService(app.catalogoService(), app.propostaService(), batch),
                batch,
                app.bachecaRepository(),
                app.spazioPersonaleRepository());
    }

    public static AuthenticationContext authenticationContext() {
        ApplicationIntegrationSupport.InMemoryCredenzialiRepository repository =
                new ApplicationIntegrationSupport.InMemoryCredenzialiRepository();
        return new AuthenticationContext(repository, new AuthenticationService(repository));
    }

    public static Categoria configuraCatalogoBaseConSport(FunctionalGraph graph) {
        graph.configuratoreService().configuraCampiBase(List.of());
        return graph.configuratoreService().createCategoria("Sport");
    }

    public static Proposta propostaValida(FunctionalGraph graph, String titolo, String numeroPartecipanti) {
        Categoria categoria = graph.configuratoreService().getCategorie().isEmpty()
                ? configuraCatalogoBaseConSport(graph)
                : graph.configuratoreService().getCategorie().get(0);
        Proposta proposta = graph.configuratoreService().creaProposta(categoria);
        graph.configuratoreService().applicaValoriEValida(proposta, valoriProposta(titolo, numeroPartecipanti));
        return proposta;
    }

    public static Proposta propostaPubblicata(FunctionalGraph graph, String titolo, String numeroPartecipanti) {
        Proposta proposta = propostaValida(graph, titolo, numeroPartecipanti);
        graph.configuratoreService().salvaProposta(proposta);
        graph.configuratoreService().pubblicaProposta(proposta);
        return proposta;
    }

    public static Proposta propostaPersistita(StatoProposta stato, String titolo, String numeroPartecipanti,
                                              LocalDate termineIscrizione, LocalDate dataEvento,
                                              List<String> aderenti) {
        Categoria categoria = new Categoria("Sport");
        return Proposta.fromJson(
                null,
                campiBase(),
                List.of(),
                categoria,
                valoriProposta(titolo, numeroPartecipanti, termineIscrizione, dataEvento),
                stato,
                stato == StatoProposta.APERTA ? LocalDate.now(AppConstants.clock) : null,
                termineIscrizione,
                dataEvento,
                aderenti,
                List.of(new PropostaStateChange(StatoProposta.BOZZA, LocalDate.now(AppConstants.clock)),
                        new PropostaStateChange(stato, LocalDate.now(AppConstants.clock))));
    }

    public static Map<String, String> valoriProposta(String titolo, String numeroPartecipanti) {
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        return valoriProposta(titolo, numeroPartecipanti, oggi.plusDays(1), oggi.plusDays(4));
    }

    public static Map<String, String> valoriProposta(String titolo, String numeroPartecipanti,
                                                     LocalDate termineIscrizione, LocalDate dataEvento) {
        return Map.of(
                AppConstants.CAMPO_TITOLO, titolo,
                AppConstants.CAMPO_NUM_PARTECIPANTI, numeroPartecipanti,
                AppConstants.CAMPO_TERMINE_ISCRIZIONE, termineIscrizione.format(AppConstants.DATE_FMT),
                AppConstants.CAMPO_LUOGO, "Brescia",
                AppConstants.CAMPO_DATA, dataEvento.format(AppConstants.DATE_FMT),
                AppConstants.CAMPO_ORA, "16:30",
                AppConstants.CAMPO_QUOTA, "10.50",
                AppConstants.CAMPO_DATA_CONCLUSIVA, dataEvento.plusDays(1).format(AppConstants.DATE_FMT)
        );
    }

    public static List<Campo> campiBase() {
        return it.unibs.ingsoft.domain.catalogo.CampoFactory.getInstance().creaCampiBase();
    }

    public static Notifica notifica(String id) {
        return new Notifica(id, NotificaType.LEGACY_MESSAGGIO, Map.of(), "messaggio", LocalDateTime.now());
    }

    public static Path writeBatchJson(Path dir, String json) throws Exception {
        Path file = dir.resolve("batch.json");
        Files.writeString(file, json);
        return file;
    }

    public static String batchJsonValido() {
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        return """
                {
                  "campiComuni": [
                    { "nome": "Note", "tipoDato": "STRINGA", "obbligatorio": false }
                  ],
                  "categorie": [
                    { "nome": "Sport", "campiSpecifici": [
                      { "nome": "Arbitro", "tipoDato": "BOOLEANO", "obbligatorio": false }
                    ] }
                  ],
                  "proposte": [
                    { "categoria": "Sport", "valoriCampi": {
                      "Titolo": "Torneo importato",
                      "Numero di partecipanti": "2",
                      "Termine ultimo di iscrizione": "%s",
                      "Luogo": "Brescia",
                      "Data": "%s",
                      "Ora": "16:30",
                      "Quota individuale": "10.50",
                      "Data conclusiva": "%s",
                      "Note": "Portare documento",
                      "Arbitro": "true"
                    } }
                  ]
                }
                """.formatted(
                oggi.plusDays(1).format(AppConstants.DATE_FMT),
                oggi.plusDays(4).format(AppConstants.DATE_FMT),
                oggi.plusDays(5).format(AppConstants.DATE_FMT));
    }

    public record FunctionalGraph(
            CatalogoService catalogoService,
            PropostaService propostaService,
            FruitoreService fruitoreService,
            ConfiguratoreService configuratoreService,
            BatchImportService batchImportService,
            ApplicationIntegrationSupport.InMemoryBachecaRepository bachecaRepository,
            ApplicationIntegrationSupport.InMemorySpazioPersonaleRepository spazioPersonaleRepository) {
    }

    public record AuthenticationContext(
            ApplicationIntegrationSupport.InMemoryCredenzialiRepository repository,
            AuthenticationService service) {
    }

    public static CampoBaseExtraRequest campoExtra(String nome, TipoDato tipoDato) {
        return new CampoBaseExtraRequest(nome, tipoDato);
    }
}
