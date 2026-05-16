package it.unibs.ingsoft.application;

import it.unibs.ingsoft.application.batch.BatchImportService;
import it.unibs.ingsoft.application.batch.dto.ImportResult;
import it.unibs.ingsoft.application.catalogo.dto.CampoBaseExtraRequest;
import it.unibs.ingsoft.application.catalogo.dto.CampoDefinitionRequest;
import it.unibs.ingsoft.application.catalogo.dto.CampoObbligatorietaRequest;
import it.unibs.ingsoft.application.catalogo.dto.CatalogoOperationResult;
import it.unibs.ingsoft.application.proposta.dto.PropostaValidationResult;
import it.unibs.ingsoft.domain.AppConstants;
import it.unibs.ingsoft.domain.model.catalogo.Categoria;
import it.unibs.ingsoft.domain.model.proposta.Proposta;
import it.unibs.ingsoft.domain.model.proposta.StatoProposta;
import it.unibs.ingsoft.domain.model.catalogo.TipoDato;
import it.unibs.ingsoft.domain.error.ValidationError;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConfiguratoreServiceTest {
    @Test
    void costruttore_conDipendenzeNull_lanciaNullPointerException() {
        ApplicationIntegrationSupport.ServiceGraph graph = ApplicationIntegrationSupport.serviceGraph();
        BatchImportService batchImportService = new BatchImportService(graph.catalogoService(), graph.propostaService());

        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> new ConfiguratoreService(null, graph.propostaService(), batchImportService)),
                () -> assertThrows(NullPointerException.class,
                        () -> new ConfiguratoreService(graph.catalogoService(), null, batchImportService)),
                () -> assertThrows(NullPointerException.class,
                        () -> new ConfiguratoreService(graph.catalogoService(), graph.propostaService(), null))
        );
    }

    @Test
    void creaValidaSalvaEPubblicaProposta_attraversoFacadeConfiguratore() {
        ApplicationIntegrationSupport.ServiceGraph graph = ApplicationIntegrationSupport.serviceGraph();
        ConfiguratoreService service = configuratoreService(graph);
        service.configuraCampiBase(List.of());
        Categoria categoria = service.createCategoria("Sport");
        Proposta proposta = service.creaProposta(categoria);

        PropostaValidationResult result = service.applicaValoriEValida(proposta, valoriProposta("Torneo facade"));
        service.salvaProposta(proposta);
        service.pubblicaProposta(proposta);

        assertAll(
                () -> assertTrue(result.valida()),
                () -> assertEquals(StatoProposta.APERTA, proposta.getStato()),
                () -> assertEquals(List.of(proposta), service.getBachecaPerCategoria().get("Sport"))
        );
    }

    @Test
    void facadeCatalogo_delegaCampiCategorieEObbligatorieta() {
        ApplicationIntegrationSupport.ServiceGraph graph = ApplicationIntegrationSupport.serviceGraph();
        ConfiguratoreService service = configuratoreService(graph);

        assertTrue(service.isPrimaConfigurazioneNecessaria());
        assertFalse(service.getCampiBasePredefiniti().isEmpty());
        service.configuraCampiBase(List.of(new CampoBaseExtraRequest("Materiale", TipoDato.STRINGA)));
        Categoria categoria = service.createCategoria("Sport");
        service.addCampoComune(new CampoDefinitionRequest("Costo extra", TipoDato.DECIMALE, false));
        service.addCampoSpecifico("Sport", new CampoDefinitionRequest("Livello", TipoDato.STRINGA, false));

        CatalogoOperationResult obblComune = service.setObbligatorietaCampoComune(
                new CampoObbligatorietaRequest("Costo extra", true));
        CatalogoOperationResult obblSpecifico = service.setObbligatorietaCampoSpecifico(
                "Sport",
                new CampoObbligatorietaRequest("Livello", true));
        List<Categoria> categoriePrimaRimozione = service.getCategorie();
        CatalogoOperationResult rimuoviSpecifico = service.rimuoviCampoSpecifico("Sport", "Livello");
        CatalogoOperationResult rimuoviSpecificoAssente = service.rimuoviCampoSpecifico("Sport", "Livello");
        CatalogoOperationResult rimuoviComune = service.rimuoviCampoComune("Costo extra");
        CatalogoOperationResult rimuoviComuneAssente = service.rimuoviCampoComune("Costo extra");
        CatalogoOperationResult rimuoviCategoriaAssente = service.rimuoviCategoria("Teatro");
        CatalogoOperationResult rimuoviCategoria = service.rimuoviCategoria("Sport");

        assertAll(
                () -> assertFalse(service.isPrimaConfigurazioneNecessaria()),
                () -> assertTrue(service.getCampiBase().stream().anyMatch(c -> c.getNome().equals("Materiale"))),
                () -> assertEquals(List.of(categoria), categoriePrimaRimozione),
                () -> assertEquals(CatalogoOperationResult.SUCCESSO, obblComune),
                () -> assertEquals(CatalogoOperationResult.SUCCESSO, obblSpecifico),
                () -> assertEquals(CatalogoOperationResult.SUCCESSO, rimuoviSpecifico),
                () -> assertEquals(CatalogoOperationResult.NON_TROVATO, rimuoviSpecificoAssente),
                () -> assertEquals(CatalogoOperationResult.SUCCESSO, rimuoviComune),
                () -> assertEquals(CatalogoOperationResult.NON_TROVATO, rimuoviComuneAssente),
                () -> assertEquals(CatalogoOperationResult.NON_TROVATO, rimuoviCategoriaAssente),
                () -> assertEquals(CatalogoOperationResult.SUCCESSO, rimuoviCategoria),
                () -> assertTrue(service.getCampiComuni().isEmpty()),
                () -> assertTrue(service.getCategorie().isEmpty())
        );
    }

    @Test
    void facadeProposta_delegaValidazioneQueryRitiroEClear() {
        ApplicationIntegrationSupport.ServiceGraph graph = ApplicationIntegrationSupport.serviceGraph();
        ConfiguratoreService service = configuratoreService(graph);
        service.configuraCampiBase(List.of());
        Categoria categoria = service.createCategoria("Sport");
        Proposta proposta = service.creaProposta(categoria);
        Map<String, String> valori = valoriProposta("Torneo da gestire");
        Map<String, String> valoriCandidati = new LinkedHashMap<>(valori);
        valoriCandidati.put(
                AppConstants.CAMPO_DATA,
                LocalDate.now(AppConstants.clock).plusDays(2).format(AppConstants.DATE_FMT));

        List<ValidationError> erroriCampo = service.validaCampo(
                proposta.getCampi().stream()
                        .filter(campo -> campo.getNome().equals(AppConstants.CAMPO_DATA))
                        .findFirst()
                        .orElseThrow(),
                valoriCandidati);
        PropostaValidationResult result = service.applicaValoriEValida(proposta, valori);
        service.salvaProposta(proposta);

        assertAll(
                () -> assertFalse(erroriCampo.isEmpty()),
                () -> assertTrue(result.valida()),
                () -> assertEquals(List.of(proposta), service.getProposteValide())
        );

        service.pubblicaProposta(proposta);
        assertEquals(List.of(proposta), service.getProposteRitirabili());
        service.ritiraProposta(proposta);

        assertAll(
                () -> assertTrue(service.getProposteValide().isEmpty()),
                () -> assertTrue(service.getProposteRitirabili().isEmpty()),
                () -> assertEquals(List.of(proposta), service.getPropostePerStato().get(StatoProposta.RITIRATA))
        );

        service.clearProposteValide();
        assertTrue(service.getProposteValide().isEmpty());
    }

    @Test
    void importa_delegaABatchImportService() throws IOException {
        ApplicationIntegrationSupport.ServiceGraph graph = ApplicationIntegrationSupport.serviceGraph();
        ConfiguratoreService service = configuratoreService(graph);
        Path file = Files.createTempFile(Path.of("out"), "codex-configuratore-import", ".json");
        Files.writeString(file, "{\"campiComuni\":[],\"categorie\":[],\"proposte\":[]}");

        try {
            ImportResult result = service.importa(file);

            assertAll(
                    () -> assertFalse(result.hasErrors()),
                    () -> assertEquals(0, result.totaleImportati())
            );
        } finally {
            Files.deleteIfExists(file);
        }
    }

    private ConfiguratoreService configuratoreService(ApplicationIntegrationSupport.ServiceGraph graph) {
        return new ConfiguratoreService(
                graph.catalogoService(),
                graph.propostaService(),
                new BatchImportService(graph.catalogoService(), graph.propostaService()));
    }

    private Map<String, String> valoriProposta(String titolo) {
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        return Map.of(
                AppConstants.CAMPO_TITOLO, titolo,
                AppConstants.CAMPO_NUM_PARTECIPANTI, "2",
                AppConstants.CAMPO_TERMINE_ISCRIZIONE, oggi.plusDays(1).format(AppConstants.DATE_FMT),
                AppConstants.CAMPO_LUOGO, "Brescia",
                AppConstants.CAMPO_DATA, oggi.plusDays(4).format(AppConstants.DATE_FMT),
                AppConstants.CAMPO_ORA, "16:30",
                AppConstants.CAMPO_QUOTA, "10.50",
                AppConstants.CAMPO_DATA_CONCLUSIVA, oggi.plusDays(5).format(AppConstants.DATE_FMT)
        );
    }
}
