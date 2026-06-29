package com.nobg.chesstracker2.controller;

import com.nobg.chesstracker2.dto.TrainingDayForm;
import com.nobg.chesstracker2.dto.WeeklyGoalClosureForm;
import com.nobg.chesstracker2.service.AppDateProvider;
import com.nobg.chesstracker2.service.DashboardRatingSummaryService;
import com.nobg.chesstracker2.service.StatsService;
import com.nobg.chesstracker2.service.TrainingEntryService;
import com.nobg.chesstracker2.service.WeeklyGoalClosureService;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
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
    private final AppDateProvider appDateProvider;
    private final DashboardRatingSummaryService ratingSummaryService;
    private final WeeklyGoalClosureService weeklyGoalClosureService;

    public DashboardController(
            TrainingEntryService trainingEntryService,
            StatsService statsService,
            AppDateProvider appDateProvider,
            DashboardRatingSummaryService ratingSummaryService,
            WeeklyGoalClosureService weeklyGoalClosureService
    ) {
        this.trainingEntryService = trainingEntryService;
        this.statsService = statsService;
        this.appDateProvider = appDateProvider;
        this.ratingSummaryService = ratingSummaryService;
        this.weeklyGoalClosureService = weeklyGoalClosureService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/today";
    }

    @GetMapping("/today")
    public String today(Model model) {
        LocalDate today = appDateProvider.today();
        var view = trainingEntryService.todayView(today);
        model.addAttribute("pageTitle", "Heute");
        model.addAttribute("saveAction", "/today/entries");
        model.addAttribute("completeAction", "/today/complete");
        model.addAttribute("view", view);
        model.addAttribute("form", view.form());
        model.addAttribute("ratingSummary", ratingSummaryService.forToday(today));
        return "today";
    }

    @PostMapping("/today/entries")
    public String saveToday(@ModelAttribute("form") TrainingDayForm form, RedirectAttributes redirectAttributes) {
        try {
            boolean saved = trainingEntryService.saveDay(appDateProvider.today(), form);
            if (saved) {
                redirectAttributes.addFlashAttribute("successMessage", "Trainingstag gespeichert.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Der Trainingstag ist abgeschlossen und kann nicht mehr geaendert werden.");
            }
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/today";
    }

    @PostMapping("/today/complete")
    public String completeToday(@ModelAttribute("form") TrainingDayForm form, RedirectAttributes redirectAttributes) {
        try {
            boolean completed = trainingEntryService.completeDay(appDateProvider.today(), form);
            if (completed) {
                redirectAttributes.addFlashAttribute("successMessage", "Aimchess Training abgeschlossen.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Der Trainingstag ist bereits abgeschlossen.");
            }
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
        model.addAttribute("completeAction", "/day/" + date + "/complete");
        model.addAttribute("view", view);
        model.addAttribute("form", view.form());
        model.addAttribute("ratingSummary", ratingSummaryService.latestSummary());
        return "today";
    }

    @PostMapping("/day/{date}/entries")
    public String saveDay(@PathVariable LocalDate date, @ModelAttribute("form") TrainingDayForm form, RedirectAttributes redirectAttributes) {
        try {
            boolean saved = trainingEntryService.saveDay(date, form);
            if (saved) {
                redirectAttributes.addFlashAttribute("successMessage", "Trainingstag gespeichert.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Der Trainingstag ist abgeschlossen und kann nicht mehr geaendert werden.");
            }
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/day/" + date;
    }

    @PostMapping("/day/{date}/complete")
    public String completeDay(@PathVariable LocalDate date, @ModelAttribute("form") TrainingDayForm form, RedirectAttributes redirectAttributes) {
        try {
            boolean completed = trainingEntryService.completeDay(date, form);
            if (completed) {
                redirectAttributes.addFlashAttribute("successMessage", "Aimchess Training abgeschlossen.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Der Trainingstag ist bereits abgeschlossen.");
            }
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/day/" + date;
    }

    @GetMapping("/week")
    public String currentWeek() {
        return "redirect:/week/" + appDateProvider.currentIsoYear() + "/" + appDateProvider.currentIsoWeek();
    }

    @GetMapping("/week/{year}/{week}")
    public String week(@PathVariable int year, @PathVariable int week, Model model) {
        LocalDate start = LocalDate.of(year, 1, 4)
                .with(IsoFields.WEEK_BASED_YEAR, year)
                .with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, week)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        model.addAttribute("pageTitle", "Woche " + week);
        model.addAttribute("stats", statsService.weekStats(year, week));
        model.addAttribute("ratingSummary", ratingSummaryService.forWeek(start, start.plusDays(6)));
        model.addAttribute("goalClosure", weeklyGoalClosureService.view(year, week));
        model.addAttribute("goalClosureForm", weeklyGoalClosureService.form(year, week));
        return "week";
    }

    @PostMapping("/week/{year}/{week}/goal-closure")
    public String saveWeekGoalClosure(
            @PathVariable int year,
            @PathVariable int week,
            @ModelAttribute("goalClosureForm") WeeklyGoalClosureForm form,
            RedirectAttributes redirectAttributes
    ) {
        try {
            weeklyGoalClosureService.save(year, week, form);
            redirectAttributes.addFlashAttribute("successMessage", "Aimchess Wochenabschluss gespeichert.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/week/" + year + "/" + week;
    }

    @GetMapping("/month")
    public String currentMonth() {
        YearMonth month = appDateProvider.currentMonth();
        return "redirect:/month/" + month.getYear() + "/" + month.getMonthValue();
    }

    @GetMapping("/month/{year}/{month}")
    public String month(@PathVariable int year, @PathVariable int month, Model model) {
        YearMonth yearMonth = YearMonth.of(year, month);
        model.addAttribute("pageTitle", "Monat " + month + "/" + year);
        model.addAttribute("stats", statsService.monthStats(year, month));
        model.addAttribute("ratingSummary", ratingSummaryService.forMonth(yearMonth.atDay(1), yearMonth.atEndOfMonth()));
        return "month";
    }

    @GetMapping("/categories")
    public String categories(Model model) {
        model.addAttribute("pageTitle", "Kategorien");
        model.addAttribute("categories", statsService.categoryOverview());
        model.addAttribute("ratingSummary", ratingSummaryService.latestSummary());
        model.addAttribute("compactHeader", true);
        return "categories";
    }
}
