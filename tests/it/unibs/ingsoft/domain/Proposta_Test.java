package it.unibs.ingsoft.domain;

import it.unibs.ingsoft.domain.model.catalogo.Campo;
import it.unibs.ingsoft.domain.model.catalogo.Categoria;
import it.unibs.ingsoft.domain.model.catalogo.TipoCampo;
import it.unibs.ingsoft.domain.model.catalogo.TipoDato;
import it.unibs.ingsoft.domain.model.proposta.ProposalFailure;
import it.unibs.ingsoft.domain.model.proposta.ProposalValidationFailure;
import it.unibs.ingsoft.domain.model.proposta.Proposta;
import it.unibs.ingsoft.domain.model.proposta.PropostaStateChange;
import it.unibs.ingsoft.domain.model.proposta.PropostaValidator;
import it.unibs.ingsoft.domain.model.proposta.StatoProposta;
import it.unibs.ingsoft.domain.error.DomainException;
import it.unibs.ingsoft.domain.error.ValidationError;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class Proposta_Test {
    @Test
    void costruttore_conCategoriaValida_creaPropostaInBozzaConId() {
        Proposta proposta = propostaConCampiBaseMinimi();

        assertAll(
                () -> assertNotNull(proposta.getId()),
                () -> assertEquals(StatoProposta.BOZZA, proposta.getStato()),
                () -> assertEquals("Sport", proposta.getCategoria().getNome()),
                () -> assertFalse(proposta.isValida())
        );
    }

    @Test
    void rehydrate_conCampiCompleti_ripristinaStatoEValori() {
        LocalDate oggi = LocalDate.now(AppConstants.clock);

        Proposta proposta = Proposta.rehydrate(
                "id-1",
                campiBaseMinimiCompleti(),
                List.of(),
                new Categoria("Sport"),
                valoriValidiConDate("Torneo", "2", oggi.plusDays(1), oggi.plusDays(4), oggi.plusDays(5)),
                StatoProposta.APERTA,
                oggi,
                oggi.plusDays(1),
                oggi.plusDays(4),
                List.of("mario"),
                List.of(new PropostaStateChange(StatoProposta.BOZZA, oggi),
                        new PropostaStateChange(StatoProposta.APERTA, oggi)));

        assertAll(
                () -> assertEquals("id-1", proposta.getId()),
                () -> assertEquals(StatoProposta.APERTA, proposta.getStato()),
                () -> assertEquals(oggi, proposta.getDataPubblicazione()),
                () -> assertTrue(proposta.isIscritto("mario")),
                () -> assertEquals("Torneo", proposta.valoreCampoOrDefault(AppConstants.CAMPO_TITOLO, ""))
        );
    }

    @Test
    void valida_conCampiObbligatoriMancanti_restituisceValidationError() {
        Proposta proposta = propostaBaseCompleta();

        List<ValidationError> errori = new PropostaValidator().valida(proposta);

        assertAll(
                () -> assertFalse(errori.isEmpty()),
                () -> assertTrue(errori.stream()
                        .anyMatch(e -> e.failure() instanceof ProposalValidationFailure.RequiredFieldMissing))
        );
    }

    @Test
    void valida_conValoriCorretti_portaLaPropostaInStatoValida() {
        Proposta proposta = propostaValidaCompleta("2");

        assertAll(
                () -> assertTrue(proposta.isValida()),
                () -> assertTrue(new PropostaValidator().valida(proposta).isEmpty()),
                () -> assertEquals(StatoProposta.VALIDA, proposta.getStato())
        );
    }

    @Test
    void pubblica_conPropostaValida_aprePropostaERegistraData() {
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        Proposta proposta = propostaValidaCompleta("2");

        proposta.pubblica(oggi);

        assertAll(
                () -> assertTrue(proposta.isAperta()),
                () -> assertEquals(oggi, proposta.getDataPubblicazione())
        );
    }

    @Test
    void pubblica_conPropostaNonValida_lanciaNotValidForPublication() {
        Proposta proposta = propostaConCampiBaseMinimi();

        DomainException exception = assertThrows(DomainException.class,
                () -> proposta.pubblica(LocalDate.now(AppConstants.clock)));

        assertInstanceOf(ProposalFailure.NotValidForPublication.class, exception.failure());
    }

    @Test
    void iscrivi_conPostoDisponibile_aggiungeAderenteENonDuplica() {
        Proposta proposta = propostaApertaConCapienza("2");

        proposta.iscrivi("mario", LocalDate.now(AppConstants.clock));
        DomainException duplicato = assertThrows(DomainException.class,
                () -> proposta.iscrivi("mario", LocalDate.now(AppConstants.clock)));

        assertAll(
                () -> assertEquals(List.of("mario"), proposta.getListaAderenti()),
                () -> assertInstanceOf(ProposalFailure.AlreadySubscribed.class, duplicato.failure())
        );
    }

    @Test
    void iscrivi_conUltimoPosto_confermaProposta() {
        Proposta proposta = propostaApertaConCapienza("1");

        proposta.iscrivi("mario", LocalDate.now(AppConstants.clock));
        proposta.confermaSeAperta();

        assertTrue(proposta.isConfermata());
    }

    @Test
    void disiscrivi_conUtenteIscritto_rimuoveAderente() {
        Proposta proposta = propostaApertaConCapienza("2");
        proposta.iscrivi("mario", LocalDate.now(AppConstants.clock));

        proposta.disiscrivi("mario", LocalDate.now(AppConstants.clock));

        assertFalse(proposta.isIscritto("mario"));
    }

    @Test
    void ritira_conPropostaApertaPrimaEvento_passaaRitirata() {
        Proposta proposta = propostaApertaConCapienza("2");

        proposta.ritira(LocalDate.now(AppConstants.clock));

        assertEquals(StatoProposta.RITIRATA, proposta.getStato());
    }

    @Test
    void chiaveIdentita_normalizzaValoriBase() {
        Map<String, String> valori = Map.of(
                AppConstants.CAMPO_TITOLO, " Torneo ",
                AppConstants.CAMPO_DATA, "25/12/2026",
                AppConstants.CAMPO_ORA, "16:30",
                AppConstants.CAMPO_LUOGO, " Brescia ");

        assertEquals("torneo|25/12/2026|16:30|brescia", Proposta.chiaveIdentita(valori));
    }

    private Proposta propostaApertaConCapienza(String numeroPartecipanti) {
        Proposta proposta = propostaValidaCompleta(numeroPartecipanti);
        proposta.pubblica(LocalDate.now(AppConstants.clock));
        return proposta;
    }

    private Proposta propostaValidaCompleta(String numeroPartecipanti) {
        Proposta proposta = propostaBaseCompleta();
        proposta.aggiornaValoriCampi(valoriValidiCompleti(numeroPartecipanti));
        proposta.valida();
        return proposta;
    }

    private Proposta propostaConCampiBaseMinimi() {
        return new Proposta(new Categoria("Sport"),
                List.of(campo(AppConstants.CAMPO_NUM_PARTECIPANTI, TipoCampo.BASE, TipoDato.INTERO, true)),
                List.of());
    }

    private Proposta propostaBaseCompleta() {
        return new Proposta(new Categoria("Sport"), campiBaseMinimiCompleti(), List.of());
    }

    private List<Campo> campiBaseMinimiCompleti() {
        return List.of(
                campo(AppConstants.CAMPO_TITOLO, TipoCampo.BASE, TipoDato.STRINGA, true),
                campo(AppConstants.CAMPO_NUM_PARTECIPANTI, TipoCampo.BASE, TipoDato.INTERO, true),
                campo(AppConstants.CAMPO_TERMINE_ISCRIZIONE, TipoCampo.BASE, TipoDato.DATA, true),
                campo(AppConstants.CAMPO_LUOGO, TipoCampo.BASE, TipoDato.STRINGA, true),
                campo(AppConstants.CAMPO_DATA, TipoCampo.BASE, TipoDato.DATA, true),
                campo(AppConstants.CAMPO_ORA, TipoCampo.BASE, TipoDato.ORA, true),
                campo(AppConstants.CAMPO_QUOTA, TipoCampo.BASE, TipoDato.DECIMALE, true),
                campo(AppConstants.CAMPO_DATA_CONCLUSIVA, TipoCampo.BASE, TipoDato.DATA, false));
    }

    private Map<String, String> valoriValidiCompleti(String numeroPartecipanti) {
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        return valoriValidiConDate("Torneo", numeroPartecipanti, oggi.plusDays(1), oggi.plusDays(4), oggi.plusDays(5));
    }

    private Map<String, String> valoriValidiConDate(String titolo, String numeroPartecipanti,
                                                    LocalDate termine, LocalDate dataEvento,
                                                    LocalDate dataConclusiva) {
        return Map.of(
                AppConstants.CAMPO_TITOLO, titolo,
                AppConstants.CAMPO_NUM_PARTECIPANTI, numeroPartecipanti,
                AppConstants.CAMPO_TERMINE_ISCRIZIONE, termine.format(AppConstants.DATE_FMT),
                AppConstants.CAMPO_LUOGO, "Brescia",
                AppConstants.CAMPO_DATA, dataEvento.format(AppConstants.DATE_FMT),
                AppConstants.CAMPO_ORA, "16:30",
                AppConstants.CAMPO_QUOTA, "10.50",
                AppConstants.CAMPO_DATA_CONCLUSIVA, dataConclusiva.format(AppConstants.DATE_FMT)
        );
    }

    private Campo campo(String nome, TipoCampo tipo, TipoDato tipoDato, boolean obbligatorio) {
        return new Campo(nome, tipo, tipoDato, obbligatorio);
    }
}
