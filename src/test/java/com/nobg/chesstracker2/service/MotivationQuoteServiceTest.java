package com.nobg.chesstracker2.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.nobg.chesstracker2.model.MotivationQuote;
import com.nobg.chesstracker2.repository.MotivationQuoteRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class MotivationQuoteServiceTest {

    private final MotivationQuoteRepository quoteRepository = org.mockito.Mockito.mock(MotivationQuoteRepository.class);
    private final MotivationQuoteService service = new MotivationQuoteService(quoteRepository);

    @Test
    void returnsActiveWeeklyQuote() {
        when(quoteRepository.findByActiveTrueOrderBySortOrderAscIdAsc())
                .thenReturn(List.of(quote("Erster Spruch"), quote("Zweiter Spruch")));

        var weeklyQuote = service.getWeeklyQuote(LocalDate.of(2026, 6, 8));

        assertThat(weeklyQuote.quoteText()).isIn("Erster Spruch", "Zweiter Spruch");
        assertThat(weeklyQuote.author()).isEqualTo("chesstracker2");
    }

    @Test
    void quoteStaysStableWithinSameIsoWeek() {
        when(quoteRepository.findByActiveTrueOrderBySortOrderAscIdAsc())
                .thenReturn(List.of(quote("Erster Spruch"), quote("Zweiter Spruch"), quote("Dritter Spruch")));

        var monday = service.getWeeklyQuote(LocalDate.of(2026, 6, 8));
        var friday = service.getWeeklyQuote(LocalDate.of(2026, 6, 12));

        assertThat(friday).isEqualTo(monday);
    }

    @Test
    void returnsFallbackWhenNoActiveQuotesExist() {
        when(quoteRepository.findByActiveTrueOrderBySortOrderAscIdAsc()).thenReturn(List.of());

        var weeklyQuote = service.getWeeklyQuote(LocalDate.of(2026, 6, 8));

        assertThat(weeklyQuote.quoteText()).isEqualTo("Jeder Trainingstag ist ein Zug in Richtung besseres Schach.");
        assertThat(weeklyQuote.author()).isEqualTo("chesstracker2");
    }

    private MotivationQuote quote(String text) {
        MotivationQuote quote = new MotivationQuote();
        quote.setQuoteText(text);
        quote.setAuthor("chesstracker2");
        quote.setActive(true);
        return quote;
    }
}
