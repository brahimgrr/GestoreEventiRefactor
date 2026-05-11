package it.unibs.ingsoft.domain;

import it.unibs.ingsoft.domain.shared.AppConstants;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
