package com.nobg.chesstracker2.controller;

import com.nobg.chesstracker2.dto.TrainingDayForm;
import com.nobg.chesstracker2.service.StatsService;
import com.nobg.chesstracker2.service.TrainingEntryService;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.IsoFields;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class DashboardController {

    private final TrainingEntryService trainingEntryService;
    private final StatsService statsService;

    public DashboardController(TrainingEntryService trainingEntryService, StatsService statsService) {
        this.trainingEntryService = trainingEntryService;
        this.statsService = statsService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/today";
    }

    @GetMapping("/today")
    public String today(Model model) {
        var view = trainingEntryService.todayView(LocalDate.now());
        model.addAttribute("pageTitle", "Heute");
        model.addAttribute("saveAction", "/today/entries");
        model.addAttribute("view", view);
        model.addAttribute("form", view.form());
        return "today";
    }

    @PostMapping("/today/entries")
    public String saveToday(@ModelAttribute("form") TrainingDayForm form, RedirectAttributes redirectAttributes) {
        try {
            trainingEntryService.saveDay(LocalDate.now(), form);
            redirectAttributes.addFlashAttribute("successMessage", "Trainingstag gespeichert.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/today";
    }

    @GetMapping("/day/{date}")
    public String day(@PathVariable LocalDate date, Model model) {
        var view = trainingEntryService.todayView(date);
        model.addAttribute("pageTitle", "Tag " + date);
        model.addAttribute("saveAction", "/day/" + date + "/entries");
        model.addAttribute("view", view);
        model.addAttribute("form", view.form());
        return "today";
    }

    @PostMapping("/day/{date}/entries")
    public String saveDay(@PathVariable LocalDate date, @ModelAttribute("form") TrainingDayForm form, RedirectAttributes redirectAttributes) {
        try {
            trainingEntryService.saveDay(date, form);
            redirectAttributes.addFlashAttribute("successMessage", "Trainingstag gespeichert.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/day/" + date;
    }

    @GetMapping("/week")
    public String currentWeek() {
        LocalDate today = LocalDate.now();
        int year = today.get(IsoFields.WEEK_BASED_YEAR);
        int week = today.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        return "redirect:/week/" + year + "/" + week;
    }

    @GetMapping("/week/{year}/{week}")
    public String week(@PathVariable int year, @PathVariable int week, Model model) {
        model.addAttribute("pageTitle", "Woche " + week);
        model.addAttribute("stats", statsService.weekStats(year, week));
        return "week";
    }

    @GetMapping("/month")
    public String currentMonth() {
        YearMonth month = YearMonth.now();
        return "redirect:/month/" + month.getYear() + "/" + month.getMonthValue();
    }

    @GetMapping("/month/{year}/{month}")
    public String month(@PathVariable int year, @PathVariable int month, Model model) {
        model.addAttribute("pageTitle", "Monat " + month + "/" + year);
        model.addAttribute("stats", statsService.monthStats(year, month));
        return "month";
    }

    @GetMapping("/categories")
    public String categories(Model model) {
        model.addAttribute("pageTitle", "Kategorien");
        model.addAttribute("categories", statsService.categoryOverview());
        return "categories";
    }
}
