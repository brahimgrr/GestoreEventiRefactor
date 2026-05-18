package it.unibs.ingsoft.domain.shared;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

class AppConstantsTest {
    @Test
    void costruttorePrivato_creaIstanzaQuandoInvocatoViaReflection() throws Exception {
        Constructor<AppConstants> constructor = AppConstants.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        assertNotNull(constructor.newInstance());
    }

    @Test
    void labelEFormatiEspongonoValoriCanonici() {
        assertAll(
                () -> assertEquals("dd/MM/yyyy", AppConstants.DATE_FORMAT_LABEL),
                () -> assertEquals("HH:mm", AppConstants.TIME_FORMAT_LABEL),
                () -> assertEquals("Titolo", AppConstants.CAMPO_TITOLO),
                () -> assertEquals("Termine ultimo di iscrizione", AppConstants.CAMPO_TERMINE_ISCRIZIONE),
                () -> assertEquals("Data", AppConstants.CAMPO_DATA),
                () -> assertEquals("Data conclusiva", AppConstants.CAMPO_DATA_CONCLUSIVA),
                () -> assertEquals("Ora", AppConstants.CAMPO_ORA),
                () -> assertEquals("Luogo", AppConstants.CAMPO_LUOGO),
                () -> assertEquals("Quota individuale", AppConstants.CAMPO_QUOTA),
                () -> assertEquals("Numero di partecipanti", AppConstants.CAMPO_NUM_PARTECIPANTI)
        );
    }

    @Test
    void dateFmt_conDataValida_formattaNelFormatoGiornoMeseAnno() {
        String formattata = LocalDate.of(2026, 12, 25).format(AppConstants.DATE_FMT);

        assertEquals("25/12/2026", formattata);
    }

    @Test
    void timeFmt_conOraValida_formattaNelFormatoOreMinuti() {
        String formattata = LocalTime.of(16, 30).format(AppConstants.TIME_FMT);

        assertEquals("16:30", formattata);
    }

    @Test
    void setClock_conClockValido_sostituisceClockGlobale() {
        Clock originale = AppConstants.clock;
        Clock fisso = Clock.fixed(Instant.parse("2026-05-13T10:15:30Z"), ZoneId.of("Europe/Rome"));

        try {
            AppConstants.setClock(fisso);

            assertEquals(LocalDate.of(2026, 5, 13), LocalDate.now(AppConstants.clock));
        } finally {
            AppConstants.setClock(originale);
        }
    }

    @Test
    void setClock_conClockNull_lanciaNullPointerException() {
        assertThrows(NullPointerException.class, () -> AppConstants.setClock(null));
    }
}
