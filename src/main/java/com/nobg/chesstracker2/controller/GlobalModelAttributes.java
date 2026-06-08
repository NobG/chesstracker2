package com.nobg.chesstracker2.controller;

import com.nobg.chesstracker2.service.AppDateProvider;
import com.nobg.chesstracker2.service.MotivationQuoteService;
import com.nobg.chesstracker2.viewmodel.MotivationQuoteViewModel;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    private final MotivationQuoteService quoteService;
    private final AppDateProvider appDateProvider;

    public GlobalModelAttributes(MotivationQuoteService quoteService, AppDateProvider appDateProvider) {
        this.quoteService = quoteService;
        this.appDateProvider = appDateProvider;
    }

    @ModelAttribute("weeklyQuote")
    public MotivationQuoteViewModel weeklyQuote() {
        return quoteService.getWeeklyQuote(appDateProvider.today());
    }
}
