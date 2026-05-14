package it.unibs.ingsoft.domain.shared;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AppConstantsTest {
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
