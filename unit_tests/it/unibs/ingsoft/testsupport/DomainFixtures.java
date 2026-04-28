package it.unibs.ingsoft.testsupport;

import it.unibs.ingsoft.application.PropostaService;
import it.unibs.ingsoft.domain.*;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

public final class DomainFixtures {
    public static final ZoneId ZONE = ZoneId.of("Europe/Rome");
    public static final LocalDate TODAY = LocalDate.of(2026, 1, 10);
    public static final Clock FIXED_CLOCK = Clock.fixed(TODAY.atStartOfDay(ZONE).toInstant(), ZONE);

    private DomainFixtures() {
    }

    public static void useFixedClock() {
        AppConstants.clock = FIXED_CLOCK;
    }

    public static void resetClock() {
        AppConstants.clock = Clock.systemDefaultZone();
    }

    public static List<Campo> baseFields() {
        return Arrays.stream(CampoBaseDefinito.values())
                .map(CampoBaseDefinito::toCampo)
                .toList();
    }

    public static List<Campo> commonFields() {
        return List.of(new Campo("Difficolta", TipoCampo.COMUNE, TipoDato.STRINGA, false));
    }

    public static Categoria category(String nome, Campo... campiSpecifici) {
        Categoria categoria = new Categoria(nome);
        for (Campo campo : campiSpecifici) {
            categoria.addCampoSpecifico(campo);
        }
        return categoria;
    }

    public static Campo specificField(String nome, TipoDato tipoDato, boolean obbligatorio) {
        return new Campo(nome, TipoCampo.SPECIFICO, tipoDato, obbligatorio);
    }

    public static Map<String, String> validProposalValues(String titolo) {
        return validProposalValues(titolo, 3, TODAY.plusDays(5), TODAY.plusDays(8), TODAY.plusDays(8));
    }

    public static Map<String, String> validProposalValues(String titolo, int posti,
                                                          LocalDate termineIscrizione,
                                                          LocalDate dataEvento,
                                                          LocalDate dataConclusiva) {
        Map<String, String> values = new LinkedHashMap<>();
        values.put(AppConstants.CAMPO_TITOLO, titolo);
        values.put(AppConstants.CAMPO_NUM_PARTECIPANTI, String.valueOf(posti));
        values.put(AppConstants.CAMPO_TERMINE_ISCRIZIONE, termineIscrizione.format(AppConstants.DATE_FMT));
        values.put(AppConstants.CAMPO_LUOGO, "Brescia");
        values.put(AppConstants.CAMPO_DATA, dataEvento.format(AppConstants.DATE_FMT));
        values.put(AppConstants.CAMPO_ORA, "16:30");
        values.put(AppConstants.CAMPO_QUOTA, "12.50");
        values.put(AppConstants.CAMPO_DATA_CONCLUSIVA, dataConclusiva.format(AppConstants.DATE_FMT));
        values.put("Difficolta", "media");
        return values;
    }

    public static Proposta draftProposal(String titolo) {
        Categoria categoria = category("Escursione");
        Proposta proposta = new Proposta(categoria, baseFields(), commonFields());
        proposta.putAllValoriCampi(validProposalValues(titolo));
        return proposta;
    }

    public static Proposta validatedProposal(String titolo) {
        InMemoryBachecaRepository repo = new InMemoryBachecaRepository();
        PropostaService service = new PropostaService(repo);
        Proposta proposta = draftProposal(titolo);
        assertTrue(service.validaProposta(proposta).isEmpty());
        return proposta;
    }

    public static Proposta openProposal(String titolo, int posti) {
        Proposta proposta = new Proposta(category("Escursione"), baseFields(), commonFields());
        proposta.putAllValoriCampi(validProposalValues(titolo, posti, TODAY.plusDays(5), TODAY.plusDays(8), TODAY.plusDays(8)));
        proposta.setTermineIscrizione(TODAY.plusDays(5));
        proposta.setDataEvento(TODAY.plusDays(8));
        proposta.setStato(StatoProposta.VALIDA);
        proposta.setStato(StatoProposta.APERTA);
        proposta.setDataPubblicazione(TODAY);
        return proposta;
    }

    public static String validImportJson() {
        return """
                {
                  "campiComuni": [
                    {"nome":"Difficolta","tipoDato":"STRINGA","obbligatorio":false}
                  ],
                  "categorie": [
                    {"nome":"Escursione","campiSpecifici":[
                      {"nome":"Guida","tipoDato":"STRINGA","obbligatorio":false}
                    ]}
                  ],
                  "proposte": [
                    {"categoria":"Escursione","valoriCampi":{
                      "Titolo":"Giro sul lago",
                      "Numero di partecipanti":"3",
                      "Termine ultimo di iscrizione":"15/01/2026",
                      "Luogo":"Brescia",
                      "Data":"18/01/2026",
                      "Ora":"16:30",
                      "Quota individuale":"12.50",
                      "Data conclusiva":"18/01/2026",
                      "Difficolta":"media",
                      "Guida":"Luca"
                    }}
                  ]
                }
                """;
    }
}
