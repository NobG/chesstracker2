package com.nobg.chesstracker2.controller;

import com.nobg.chesstracker2.service.MotivationQuoteService;
import com.nobg.chesstracker2.viewmodel.MotivationQuoteViewModel;
import java.time.LocalDate;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    private final MotivationQuoteService quoteService;

    public GlobalModelAttributes(MotivationQuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @ModelAttribute("weeklyQuote")
    public MotivationQuoteViewModel weeklyQuote() {
        return quoteService.getWeeklyQuote(LocalDate.now());
    }
}
