package it.unibs.ingsoft.domain.catalogo;

import it.unibs.ingsoft.domain.shared.error.DomainException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CampoFactoryTest {
    @Test
    void getInstance_quandoInvocatoDueVolte_restituisceStessaIstanza() {
        assertSame(CampoFactory.getInstance(), CampoFactory.getInstance());
    }

    @Test
    void costruttorePrivato_creaIstanzaQuandoInvocatoViaReflection() throws Exception {
        Constructor<CampoFactory> constructor = CampoFactory.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        assertNotNull(constructor.newInstance());
    }

    @Test
    void creaCampiBase_quandoInvocato_creaUnCampoPerOgniCampoBaseDefinito() {
        List<Campo> campi = CampoFactory.getInstance().creaCampiBase();

        assertAll(
                () -> assertEquals(CampoBaseDefinito.values().length, campi.size()),
                () -> assertTrue(campi.stream().allMatch(campo -> campo.getTipo() == TipoCampo.BASE)),
                () -> assertTrue(campi.stream().allMatch(Campo::isObbligatorio))
        );
    }

    @Test
    void creaCampiBaseExtra_conListeAllineate_creaCampiBaseObbligatori() {
        List<Campo> campi = CampoFactory.getInstance()
                .creaCampiBaseExtra(List.of("Extra", "Numero"), List.of(TipoDato.STRINGA, TipoDato.INTERO));

        assertAll(
                () -> assertEquals("Extra", campi.get(0).getNome()),
                () -> assertEquals(TipoCampo.BASE, campi.get(0).getTipo()),
                () -> assertTrue(campi.get(0).isObbligatorio()),
                () -> assertEquals("Numero", campi.get(1).getNome()),
                () -> assertEquals(TipoDato.INTERO, campi.get(1).getTipoDato())
        );
    }

    @Test
    void creaCampiBaseExtra_conListeVuote_restituisceListaVuotaModificabile() {
        List<Campo> campi = CampoFactory.getInstance().creaCampiBaseExtra(List.of(), List.of());

        assertAll(
                () -> assertTrue(campi.isEmpty()),
                () -> assertDoesNotThrow(() -> campi.add(
                        new Campo("Extra", TipoCampo.BASE, TipoDato.STRINGA, true)))
        );
    }

    @Test
    void creaCampiBaseExtra_conListaNomiNull_lanciaIllegalStateException() {
        DomainException exception = assertThrows(DomainException.class,
                () -> CampoFactory.getInstance().creaCampiBaseExtra(null, List.of(TipoDato.STRINGA)));

        assertInstanceOf(CatalogFailure.ExtraFieldDataInvalid.class, exception.failure());
    }

    @Test
    void creaCampiBaseExtra_conTipiDatoNull_lanciaDomainException() {
        DomainException exception = assertThrows(DomainException.class,
                () -> CampoFactory.getInstance().creaCampiBaseExtra(List.of("Nota"), null));

        assertInstanceOf(CatalogFailure.ExtraFieldDataInvalid.class, exception.failure());
    }

    @Test
    void creaCampiBaseExtra_conListeDiDimensioneDiversa_lanciaIllegalStateException() {
        DomainException exception = assertThrows(DomainException.class,
                () -> CampoFactory.getInstance().creaCampiBaseExtra(List.of("Uno"), List.of()));

        assertInstanceOf(CatalogFailure.ExtraFieldDimensionsMismatch.class, exception.failure());
    }

    @Test
    void creaCampo_conParametriValidi_delegaAlCostruttoreCampo() {
        Campo campo = CampoFactory.getInstance().creaCampo("Note", TipoCampo.COMUNE, TipoDato.STRINGA, false);

        assertAll(
                () -> assertEquals("Note", campo.getNome()),
                () -> assertEquals(TipoCampo.COMUNE, campo.getTipo()),
                () -> assertEquals(TipoDato.STRINGA, campo.getTipoDato()),
                () -> assertFalse(campo.isObbligatorio())
        );
    }

    @Test
    void creaCampoBase_conParametriValidi_creaCampoBaseObbligatorio() {
        Campo campo = CampoFactory.getInstance().creaCampoBase("Titolo", TipoDato.STRINGA);

        assertAll(
                () -> assertEquals(TipoCampo.BASE, campo.getTipo()),
                () -> assertTrue(campo.isObbligatorio())
        );
    }

    @Test
    void creaCampoComune_conParametriValidi_creaCampoComuneConObbligatorietaRichiesta() {
        Campo campo = CampoFactory.getInstance().creaCampoComune("Eta", TipoDato.INTERO, false);

        assertAll(
                () -> assertEquals(TipoCampo.COMUNE, campo.getTipo()),
                () -> assertEquals(TipoDato.INTERO, campo.getTipoDato()),
                () -> assertFalse(campo.isObbligatorio())
        );
    }

    @Test
    void creaCampoSpecifico_conNomeNull_lanciaIllegalStateException() {
        DomainException exception = assertThrows(DomainException.class,
                () -> CampoFactory.getInstance().creaCampoSpecifico(null, TipoDato.STRINGA, false));

        assertInstanceOf(CatalogFailure.FieldNameInvalid.class, exception.failure());
    }

    @Test
    void creaCampoSpecifico_conParametriValidi_creaCampoSpecificoConObbligatorietaRichiesta() {
        Campo campo = CampoFactory.getInstance().creaCampoSpecifico("Arbitro", TipoDato.BOOLEANO, true);

        assertAll(
                () -> assertEquals(TipoCampo.SPECIFICO, campo.getTipo()),
                () -> assertEquals(TipoDato.BOOLEANO, campo.getTipoDato()),
                () -> assertTrue(campo.isObbligatorio())
        );
    }
}
