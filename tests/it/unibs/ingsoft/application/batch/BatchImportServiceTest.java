package it.unibs.ingsoft.application.batch;

import it.unibs.ingsoft.application.ApplicationIntegrationSupport;
import it.unibs.ingsoft.application.batch.dto.ImportResult;
import it.unibs.ingsoft.application.catalogo.CatalogoService;
import it.unibs.ingsoft.application.error.ApplicationException;
import it.unibs.ingsoft.application.proposta.PropostaService;
import it.unibs.ingsoft.domain.model.catalogo.TipoDato;
import it.unibs.ingsoft.domain.AppConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BatchImportServiceTest {
    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        Files.createDirectories(Path.of("out"));
        tempDir = Files.createTempDirectory(Path.of("out"), "batch-import-service-test-");
    }

    @AfterEach
    void tearDown() throws IOException {
        if (tempDir != null && Files.exists(tempDir)) {
            try (var paths = Files.walk(tempDir)) {
                paths.sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException ignored) {
                                // Best effort cleanup for test artifacts.
                            }
                        });
            }
        }
    }

    @Test
    void importa_conJsonValido_importaCampiComuniCategorieEProposteValide() throws Exception {
        ApplicationIntegrationSupport.ServiceGraph graph = ApplicationIntegrationSupport.serviceGraph();
        CatalogoService catalogoService = graph.catalogoService();
        PropostaService propostaService = graph.propostaService();
        catalogoService.configuraCampiBase(List.of());
        BatchImportService service = new BatchImportService(catalogoService, propostaService);
        Path file = tempDir.resolve("import.json");
        Files.writeString(file, jsonImportValido());

        ImportResult result = service.importa(file);

        assertAll(
                () -> assertFalse(result.hasErrors()),
                () -> assertEquals(1, result.getCampiComuniImportati()),
                () -> assertEquals(1, result.getCategorieImportate()),
                () -> assertEquals(1, result.getProposteImportate()),
                () -> assertEquals(3, result.totaleImportati()),
                () -> assertEquals("Note", catalogoService.getCampiComuni().get(0).getNome()),
                () -> assertEquals("Sport", catalogoService.getCategorie().get(0).getNome()),
                () -> assertEquals(1, propostaService.getProposteValide().size())
        );
    }

    @Test
    void importa_conCampoComuneDuplicatoNelloStessoFile_registraErroreEImportaSoloIlPrimo() throws Exception {
        ApplicationIntegrationSupport.ServiceGraph graph = ApplicationIntegrationSupport.serviceGraph();
        CatalogoService catalogoService = graph.catalogoService();
        catalogoService.configuraCampiBase(List.of());
        BatchImportService service = new BatchImportService(catalogoService, graph.propostaService());
        Path file = tempDir.resolve("import-duplicato.json");
        Files.writeString(file, """
                {
                  "campiComuni": [
                    { "nome": "Note", "tipoDato": "STRINGA", "obbligatorio": false },
                    { "nome": "note", "tipoDato": "STRINGA", "obbligatorio": true }
                  ],
                  "categorie": [],
                  "proposte": []
                }
                """);

        ImportResult result = service.importa(file);

        assertAll(
                () -> assertTrue(result.hasErrors()),
                () -> assertEquals(1, result.getCampiComuniImportati()),
                () -> assertInstanceOf(ImportFailure.CommonFieldDuplicated.class, result.getErrori().get(0).failure()),
                () -> assertEquals(1, catalogoService.getCampiComuni().size())
        );
    }

    @Test
    void importa_conFileAssente_lanciaApplicationException() {
        BatchImportService service = new BatchImportService(
                ApplicationIntegrationSupport.serviceGraph().catalogoService(),
                ApplicationIntegrationSupport.serviceGraph().propostaService());

        ApplicationException exception = assertThrows(ApplicationException.class,
                () -> service.importa(tempDir.resolve("assente.json")));

        assertInstanceOf(ImportFailure.FileNotFound.class, exception.failure());
    }

    @Test
    void importa_conFileNonLeggibile_lanciaErroreFileNonLeggibile() throws Exception {
        BatchImportService service = serviceConCampiBaseConfigurati();
        Path file = scriviJson("non-leggibile.json", "{}");
        AclFileAttributeView aclView = Files.getFileAttributeView(file, AclFileAttributeView.class);
        List<AclEntry> aclOriginale = aclView == null ? null : aclView.getAcl();

        try {
            file.toFile().setReadable(false, false);
            if (Files.isReadable(file) && aclView != null) {
                AclEntry denyRead = AclEntry.newBuilder()
                        .setType(AclEntryType.DENY)
                        .setPrincipal(file.getFileSystem()
                                .getUserPrincipalLookupService()
                                .lookupPrincipalByName(System.getProperty("user.name")))
                        .setPermissions(AclEntryPermission.READ_DATA)
                        .build();
                List<AclEntry> acl = new ArrayList<>();
                acl.add(denyRead);
                acl.addAll(aclOriginale);
                aclView.setAcl(acl);
            }

            if (!Files.isReadable(file)) {
                ApplicationException exception = assertThrows(ApplicationException.class, () -> service.importa(file));

                assertInstanceOf(ImportFailure.FileNotReadable.class, exception.failure());
            }
        } finally {
            if (aclView != null && aclOriginale != null) {
                aclView.setAcl(aclOriginale);
            }
            file.toFile().setReadable(true, false);
        }
    }

    @Test
    void costruttore_conDipendenzeNull_lanciaNullPointerException() {
        ApplicationIntegrationSupport.ServiceGraph graph = ApplicationIntegrationSupport.serviceGraph();

        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> new BatchImportService(null, graph.propostaService())),
                () -> assertThrows(NullPointerException.class,
                        () -> new BatchImportService(graph.catalogoService(), null))
        );
    }

    @Test
    void importa_conJsonVuoto_nonImportaNullaENonProduceErrori() throws Exception {
        BatchImportService service = serviceConCampiBaseConfigurati();
        Path file = scriviJson("vuoto.json", "{}");

        ImportResult result = service.importa(file);

        assertAll(
                () -> assertFalse(result.hasErrors()),
                () -> assertEquals(0, result.totaleImportati())
        );
    }

    @Test
    void importa_conCampoComuneSenzaNomeOTipoInvalido_registraErrori() throws Exception {
        BatchImportService service = serviceConCampiBaseConfigurati();
        Path file = scriviJson("campi-comuni-invalidi.json", """
                {
                  "campiComuni": [
                    { "tipoDato": "STRINGA", "obbligatorio": false },
                    { "nome": " ", "tipoDato": "STRINGA", "obbligatorio": false },
                    { "nome": "Categoria eta", "obbligatorio": true },
                    { "nome": "Eta", "tipoDato": " ", "obbligatorio": true },
                    { "nome": "Peso", "tipoDato": "inesistente", "obbligatorio": true }
                  ],
                  "categorie": [],
                  "proposte": []
                }
                """);

        ImportResult result = service.importa(file);

        assertEquals(List.of(
                        ImportFailure.CommonFieldNameMissing.class,
                        ImportFailure.CommonFieldNameMissing.class,
                        ImportFailure.CommonFieldTypeInvalid.class,
                        ImportFailure.CommonFieldTypeInvalid.class,
                        ImportFailure.CommonFieldTypeInvalid.class),
                failureTypes(result));
    }

    @Test
    void importa_conCampoComuneGiaPresente_registraErroreDominio() throws Exception {
        ApplicationIntegrationSupport.ServiceGraph graph = ApplicationIntegrationSupport.serviceGraph();
        graph.catalogoService().configuraCampiBase(List.of());
        graph.catalogoService().addCampoComune("Note", TipoDato.STRINGA, false);
        BatchImportService service = new BatchImportService(graph.catalogoService(), graph.propostaService());
        Path file = scriviJson("campo-comune-dominio.json", """
                {
                  "campiComuni": [
                    { "nome": "Note", "tipoDato": "STRINGA", "obbligatorio": false }
                  ],
                  "categorie": [],
                  "proposte": []
                }
                """);

        ImportResult result = service.importa(file);

        assertAll(
                () -> assertInstanceOf(ImportFailure.CommonFieldDomainError.class, result.getErrori().get(0).failure()),
                () -> assertNotNull(((ImportFailure.CommonFieldDomainError) result.getErrori().get(0).failure()).failure())
        );
    }

    @Test
    void importa_conCategorieECampiSpecificiInvalidi_registraErrori() throws Exception {
        BatchImportService service = serviceConCampiBaseConfigurati();
        Path file = scriviJson("categorie-invalide.json", """
                {
                  "campiComuni": [],
                  "categorie": [
                    { "campiSpecifici": [] },
                    { "nome": " ", "campiSpecifici": [] },
                    { "nome": "Sport", "campiSpecifici": [
                      { "tipoDato": "STRINGA", "obbligatorio": false },
                      { "nome": " ", "tipoDato": "STRINGA", "obbligatorio": false },
                      { "nome": "Categoria", "obbligatorio": false },
                      { "nome": "Taglia", "tipoDato": " ", "obbligatorio": false },
                      { "nome": "Arbitro", "tipoDato": "boh", "obbligatorio": false },
                      { "nome": "Livello", "tipoDato": "STRINGA", "obbligatorio": false }
                    ] },
                    { "nome": "sport", "campiSpecifici": [] }
                  ],
                  "proposte": []
                }
                """);

        ImportResult result = service.importa(file);

        assertEquals(List.of(
                        ImportFailure.CategoryNameMissing.class,
                        ImportFailure.CategoryNameMissing.class,
                        ImportFailure.SpecificFieldNameMissing.class,
                        ImportFailure.SpecificFieldNameMissing.class,
                        ImportFailure.SpecificFieldTypeInvalid.class,
                        ImportFailure.SpecificFieldTypeInvalid.class,
                        ImportFailure.SpecificFieldTypeInvalid.class,
                        ImportFailure.CategoryDuplicated.class),
                failureTypes(result));
    }

    @Test
    void importa_conCategoriaGiaPresente_registraErroreDominio() throws Exception {
        ApplicationIntegrationSupport.ServiceGraph graph = ApplicationIntegrationSupport.serviceGraph();
        graph.catalogoService().configuraCampiBase(List.of());
        graph.catalogoService().createCategoria("Sport");
        BatchImportService service = new BatchImportService(graph.catalogoService(), graph.propostaService());
        Path file = scriviJson("categoria-dominio.json", """
                {
                  "campiComuni": [],
                  "categorie": [
                    { "nome": "Sport", "campiSpecifici": [] }
                  ],
                  "proposte": []
                }
                """);

        ImportResult result = service.importa(file);

        assertInstanceOf(ImportFailure.CategoryDomainError.class, result.getErrori().get(0).failure());
    }

    @Test
    void importa_conCampoSpecificoDuplicato_registraErroreDominio() throws Exception {
        BatchImportService service = serviceConCampiBaseConfigurati();
        Path file = scriviJson("campo-specifico-dominio.json", """
                {
                  "campiComuni": [],
                  "categorie": [
                    { "nome": "Sport", "campiSpecifici": [
                      { "nome": "Arbitro", "tipoDato": "BOOLEANO", "obbligatorio": false },
                      { "nome": "arbitro", "tipoDato": "BOOLEANO", "obbligatorio": true }
                    ] }
                  ],
                  "proposte": []
                }
                """);

        ImportResult result = service.importa(file);

        assertInstanceOf(ImportFailure.SpecificFieldDomainError.class, result.getErrori().get(0).failure());
    }

    @Test
    void importa_conProposteInvalidhe_registraErroriAttesi() throws Exception {
        BatchImportService service = serviceConCampiBaseConfigurati();
        Path file = scriviJson("proposte-invalide.json", jsonProposteInvalidhe());

        ImportResult result = service.importa(file);

        List<Class<?>> failures = failureTypes(result);

        assertAll(
                () -> assertTrue(failures.contains(ImportFailure.ProposalCategoryMissing.class)),
                () -> assertTrue(failures.contains(ImportFailure.ProposalCategoryNotFound.class)),
                () -> assertTrue(failures.contains(ImportFailure.ProposalValidation.class)),
                () -> assertTrue(failures.contains(ImportFailure.ProposalDuplicatedInFile.class))
        );
    }

    @Test
    void importa_conPropostaValidaConValoriOpzionaliAssentiOBlank_importaSenzaErroriTipo() throws Exception {
        ApplicationIntegrationSupport.ServiceGraph graph = ApplicationIntegrationSupport.serviceGraph();
        CatalogoService catalogoService = graph.catalogoService();
        catalogoService.configuraCampiBase(List.of());
        catalogoService.addCampoComune("Note", TipoDato.STRINGA, false);
        catalogoService.createCategoria("Sport");
        catalogoService.addCampoSpecifico("Sport", "Arbitro", TipoDato.BOOLEANO, false);
        BatchImportService service = new BatchImportService(catalogoService, graph.propostaService());
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        Path file = scriviJson("proposta-opzionali.json", """
                {
                  "campiComuni": [],
                  "categorie": [],
                  "proposte": [
                    { "categoria": "Sport", "valoriCampi": {
                      "Titolo": "Opzionali assenti",
                      "Numero di partecipanti": "2",
                      "Termine ultimo di iscrizione": "%s",
                      "Luogo": "Brescia",
                      "Data": "%s",
                      "Ora": "16:30",
                      "Quota individuale": "10.50",
                      "Data conclusiva": "%s",
                      "Note": " "
                    } }
                  ]
                }
                """.formatted(
                oggi.plusDays(1).format(AppConstants.DATE_FMT),
                oggi.plusDays(4).format(AppConstants.DATE_FMT),
                oggi.plusDays(5).format(AppConstants.DATE_FMT)));

        ImportResult result = service.importa(file);

        assertAll(
                () -> assertFalse(result.hasErrors()),
                () -> assertEquals(1, result.getProposteImportate())
        );
    }

    @Test
    void importa_conPropostaGiaPresenteNelSistema_registraErroreDominioProposta() throws Exception {
        ApplicationIntegrationSupport.ServiceGraph graph = ApplicationIntegrationSupport.serviceGraph();
        CatalogoService catalogoService = graph.catalogoService();
        PropostaService propostaService = graph.propostaService();
        catalogoService.configuraCampiBase(List.of());
        catalogoService.createCategoria("Sport");
        BatchImportService service = new BatchImportService(catalogoService, propostaService);
        Path primoFile = scriviJson("proposta-esistente-prima.json", jsonPropostaValida("Gia presente"));
        Path secondoFile = scriviJson("proposta-esistente-seconda.json", jsonPropostaValida("Gia presente"));
        service.importa(primoFile);

        ImportResult result = service.importa(secondoFile);

        assertAll(
                () -> assertInstanceOf(ImportFailure.ProposalDomainError.class, result.getErrori().get(0).failure()),
                () -> assertNotNull(((ImportFailure.ProposalDomainError) result.getErrori().get(0).failure()).failure())
        );
    }

    @Test
    void importa_conFileIlleggibileOLetturaJsonFallita_lanciaApplicationException() throws Exception {
        BatchImportService service = serviceConCampiBaseConfigurati();
        Path file = scriviJson("rotto.json", "{ json non valido");

        ApplicationException exception = assertThrows(ApplicationException.class, () -> service.importa(file));

        assertInstanceOf(ImportFailure.InvalidJson.class, exception.failure());
    }

    private String jsonImportValido() {
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        return """
                {
                  "campiComuni": [
                    { "nome": "Note", "tipoDato": "STRINGA", "obbligatorio": false }
                  ],
                  "categorie": [
                    {
                      "nome": "Sport",
                      "campiSpecifici": [
                        { "nome": "Arbitro", "tipoDato": "BOOLEANO", "obbligatorio": false }
                      ]
                    }
                  ],
                  "proposte": [
                    {
                      "categoria": "Sport",
                      "valoriCampi": {
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
                      }
                    }
                  ]
                }
                """.formatted(
                oggi.plusDays(1).format(AppConstants.DATE_FMT),
                oggi.plusDays(4).format(AppConstants.DATE_FMT),
                oggi.plusDays(5).format(AppConstants.DATE_FMT));
    }

    private BatchImportService serviceConCampiBaseConfigurati() {
        ApplicationIntegrationSupport.ServiceGraph graph = ApplicationIntegrationSupport.serviceGraph();
        graph.catalogoService().configuraCampiBase(List.of());
        return new BatchImportService(graph.catalogoService(), graph.propostaService());
    }

    private Path scriviJson(String nomeFile, String json) throws IOException {
        Path file = tempDir.resolve(nomeFile);
        Files.writeString(file, json);
        return file;
    }

    private List<Class<?>> failureTypes(ImportResult result) {
        List<Class<?>> types = new ArrayList<>();
        result.getErrori().forEach(error -> types.add(error.failure().getClass()));
        return types;
    }

    private String jsonProposteInvalidhe() {
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        String termine = oggi.plusDays(1).format(AppConstants.DATE_FMT);
        String data = oggi.plusDays(4).format(AppConstants.DATE_FMT);
        String conclusiva = oggi.plusDays(5).format(AppConstants.DATE_FMT);
        return """
                {
                  "campiComuni": [],
                  "categorie": [
                    { "nome": "Sport", "campiSpecifici": [] }
                  ],
                  "proposte": [
                    { "valoriCampi": { "Titolo": "Categoria null" } },
                    { "categoria": " ", "valoriCampi": { "Titolo": "Senza categoria" } },
                    { "categoria": "Teatro", "valoriCampi": { "Titolo": "Categoria assente" } },
                    { "categoria": "Sport", "valoriCampi": {
                      "Titolo": "Tipo errato",
                      "Numero di partecipanti": "non intero",
                      "Termine ultimo di iscrizione": "%s",
                      "Data": "%s"
                    } },
                    { "categoria": "Sport", "valoriCampi": {
                      "Titolo": "Validazione errata",
                      "Numero di partecipanti": "2",
                      "Termine ultimo di iscrizione": "%s",
                      "Data": "%s"
                    } },
                    { "categoria": "Sport", "valoriCampi": {
                      "Titolo": "Duplicata",
                      "Numero di partecipanti": "2",
                      "Termine ultimo di iscrizione": "%s",
                      "Data": "%s",
                      "Data conclusiva": "%s"
                    } },
                    { "categoria": "Sport", "valoriCampi": {
                      "Titolo": "Duplicata",
                      "Numero di partecipanti": "2",
                      "Termine ultimo di iscrizione": "%s",
                      "Data": "%s",
                      "Data conclusiva": "%s"
                    } }
                  ]
                }
                """.formatted(
                termine, data,
                oggi.format(AppConstants.DATE_FMT), oggi.plusDays(1).format(AppConstants.DATE_FMT),
                termine, data, conclusiva,
                termine, data, conclusiva);
    }

    private String jsonPropostaValida(String titolo) {
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        return """
                {
                  "campiComuni": [],
                  "categorie": [],
                  "proposte": [
                    { "categoria": "Sport", "valoriCampi": {
                      "Titolo": "%s",
                      "Numero di partecipanti": "2",
                      "Termine ultimo di iscrizione": "%s",
                      "Luogo": "Brescia",
                      "Data": "%s",
                      "Ora": "16:30",
                      "Quota individuale": "10.50",
                      "Data conclusiva": "%s"
                    } }
                  ]
                }
                """.formatted(
                titolo,
                oggi.plusDays(1).format(AppConstants.DATE_FMT),
                oggi.plusDays(4).format(AppConstants.DATE_FMT),
                oggi.plusDays(5).format(AppConstants.DATE_FMT));
    }
}
