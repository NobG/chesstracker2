package com.nobg.chesstracker2.service;

import com.nobg.chesstracker2.model.MotivationQuote;
import com.nobg.chesstracker2.repository.MotivationQuoteRepository;
import com.nobg.chesstracker2.viewmodel.MotivationQuoteViewModel;
import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MotivationQuoteService {

    private static final MotivationQuoteViewModel FALLBACK_QUOTE = new MotivationQuoteViewModel(
            "Jeder Trainingstag ist ein Zug in Richtung besseres Schach.",
            "chesstracker2"
    );

    private final MotivationQuoteRepository quoteRepository;

    public MotivationQuoteService(MotivationQuoteRepository quoteRepository) {
        this.quoteRepository = quoteRepository;
    }

    @Transactional(readOnly = true)
    public MotivationQuoteViewModel getWeeklyQuote(LocalDate today) {
        List<MotivationQuote> quotes = quoteRepository.findByActiveTrueOrderBySortOrderAscIdAsc();
        if (quotes.isEmpty()) {
            return FALLBACK_QUOTE;
        }

        int isoYear = today.get(IsoFields.WEEK_BASED_YEAR);
        int isoWeek = today.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        int index = Math.floorMod((isoYear * 53) + isoWeek, quotes.size());
        MotivationQuote quote = quotes.get(index);
        return new MotivationQuoteViewModel(quote.getQuoteText(), quote.getAuthor());
    }
}
