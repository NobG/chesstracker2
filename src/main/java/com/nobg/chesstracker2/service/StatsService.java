package com.nobg.chesstracker2.service;

import com.nobg.chesstracker2.model.DailyTrainingEntry;
import com.nobg.chesstracker2.model.DailyCompletionStatus;
import com.nobg.chesstracker2.model.DailyNote;
import com.nobg.chesstracker2.model.TrainingCategory;
import com.nobg.chesstracker2.repository.DailyNoteRepository;
import com.nobg.chesstracker2.repository.DailyTrainingEntryRepository;
import com.nobg.chesstracker2.repository.TrainingCategoryRepository;
import com.nobg.chesstracker2.viewmodel.CategoryStatViewModel;
import com.nobg.chesstracker2.viewmodel.MonthlyStatsViewModel;
import com.nobg.chesstracker2.viewmodel.WeekDayViewModel;
import com.nobg.chesstracker2.viewmodel.WeeklyStatsViewModel;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StatsService {

    private final DailyTrainingEntryRepository entryRepository;
    private final TrainingCategoryRepository categoryRepository;
    private final DailyNoteRepository noteRepository;

    public StatsService(
            DailyTrainingEntryRepository entryRepository,
            TrainingCategoryRepository categoryRepository,
            DailyNoteRepository noteRepository
    ) {
        this.entryRepository = entryRepository;
        this.categoryRepository = categoryRepository;
        this.noteRepository = noteRepository;
    }

    @Transactional(readOnly = true)
    public WeeklyStatsViewModel weekStats(int year, int week) {
        LocalDate start = LocalDate.of(year, 1, 4)
                .with(IsoFields.WEEK_BASED_YEAR, year)
                .with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, week)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate end = start.plusDays(6);
        List<DailyTrainingEntry> entries = trainedBetween(start, end);
        StatusCounts statusCounts = statusCounts(start, end, entries);
        List<WeekDayViewModel> days = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = start.plusDays(i);
            List<DailyTrainingEntry> dayEntries = entries.stream().filter(entry -> entry.getTrainingDate().equals(date)).toList();
            days.add(new WeekDayViewModel(
                    date,
                    dayEntries.size(),
                    TrainingCalculator.totalTasks(dayEntries),
                    TrainingCalculator.successRate(TrainingCalculator.totalSuccess(dayEntries), TrainingCalculator.totalTasks(dayEntries)),
                    TrainingCalculator.totalDuration(dayEntries)
            ));
        }
        List<CategoryStatViewModel> categories = categoryStats(entries);
        Integer rate = TrainingCalculator.successRate(TrainingCalculator.totalSuccess(entries), TrainingCalculator.totalTasks(entries));
        return new WeeklyStatsViewModel(
                year,
                week,
                start,
                end,
                days,
                categories,
                (int) days.stream().filter(day -> day.trainedCategories() > 0).count(),
                entries.size(),
                TrainingCalculator.totalTasks(entries),
                TrainingCalculator.totalDuration(entries),
                rate,
                statusCounts.completed(),
                statusCounts.partial(),
                statusCounts.openWithEntries(),
                bestCategory(categories),
                weakestCategory(categories),
                "Woche " + week + " enthaelt " + entries.size() + " Trainingseintraege mit "
                        + displayRate(rate) + " Gesamtquote."
        );
    }

    @Transactional(readOnly = true)
    public MonthlyStatsViewModel monthStats(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        List<DailyTrainingEntry> entries = trainedBetween(start, end);
        StatusCounts statusCounts = statusCounts(start, end, entries);
        Map<LocalDate, List<DailyTrainingEntry>> byDay = entries.stream().collect(Collectors.groupingBy(DailyTrainingEntry::getTrainingDate));
        List<CategoryStatViewModel> categories = categoryStats(entries);
        Integer rate = TrainingCalculator.successRate(TrainingCalculator.totalSuccess(entries), TrainingCalculator.totalTasks(entries));
        int trainingDays = byDay.size();
        Integer trainingPercentage = Math.round((trainingDays * 100f) / yearMonth.lengthOfMonth());
        return new MonthlyStatsViewModel(
                year,
                month,
                yearMonth.lengthOfMonth(),
                trainingDays,
                trainingPercentage,
                TrainingCalculator.totalDuration(entries),
                TrainingCalculator.totalTasks(entries),
                rate,
                statusCounts.completed(),
                statusCounts.partial(),
                TrainingCalculator.successRate(statusCounts.completed(), trainingDays),
                categories,
                categories.stream().filter(category -> "steigend".equals(category.trend())).map(CategoryStatViewModel::categoryName).toList(),
                categories.stream().filter(category -> "fallend".equals(category.trend())).map(CategoryStatViewModel::categoryName).toList(),
                "Im Monat wurden " + trainingDays + " von " + yearMonth.lengthOfMonth()
                        + " Tagen trainiert. Gesamtquote: " + displayRate(rate) + "."
        );
    }

    @Transactional(readOnly = true)
    public List<CategoryStatViewModel> categoryOverview() {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusYears(5);
        return categoryStats(trainedBetween(start, end));
    }

    private List<DailyTrainingEntry> trainedBetween(LocalDate start, LocalDate end) {
        return entryRepository.findByTrainingDateBetweenOrderByTrainingDateAscCategorySortOrderAsc(start, end)
                .stream()
                .filter(DailyTrainingEntry::isTrained)
                .toList();
    }

    private StatusCounts statusCounts(LocalDate start, LocalDate end, List<DailyTrainingEntry> entries) {
        Map<LocalDate, DailyCompletionStatus> statuses = noteRepository.findByTrainingDateBetween(start, end)
                .stream()
                .collect(Collectors.toMap(DailyNote::getTrainingDate, DailyNote::getCompletionStatus));
        List<LocalDate> daysWithEntries = entries.stream()
                .map(DailyTrainingEntry::getTrainingDate)
                .distinct()
                .toList();

        int completed = 0;
        int partial = 0;
        int open = 0;
        for (LocalDate day : daysWithEntries) {
            DailyCompletionStatus status = statuses.getOrDefault(day, DailyCompletionStatus.OPEN);
            if (status == DailyCompletionStatus.COMPLETED) {
                completed++;
            } else if (status == DailyCompletionStatus.PARTIAL) {
                partial++;
            } else {
                open++;
            }
        }
        return new StatusCounts(completed, partial, open);
    }

    private List<CategoryStatViewModel> categoryStats(List<DailyTrainingEntry> entries) {
        Map<String, TrainingCategory> categories = new LinkedHashMap<>();
        Map<String, List<DailyTrainingEntry>> byCategory = new LinkedHashMap<>();
        for (TrainingCategory category : categoryRepository.findByActiveTrueOrderBySortOrderAscNameAsc()) {
            categories.put(category.getKey(), category);
            byCategory.put(category.getKey(), new ArrayList<>());
        }
        for (DailyTrainingEntry entry : entries) {
            List<DailyTrainingEntry> categoryEntries = byCategory.get(entry.getCategory().getKey());
            if (categoryEntries != null) {
                categoryEntries.add(entry);
            }
        }

        return byCategory.entrySet().stream()
                .map(item -> toCategoryStat(categories.get(item.getKey()), item.getValue()))
                .toList();
    }

    private CategoryStatViewModel toCategoryStat(TrainingCategory category, List<DailyTrainingEntry> entries) {
        int success = TrainingCalculator.totalSuccess(entries);
        int total = TrainingCalculator.totalTasks(entries);
        DailyTrainingEntry best = entries.stream()
                .filter(entry -> entry.getTotalCount() > 0)
                .max(Comparator.comparing(entry -> TrainingCalculator.successRate(entry.getSuccessCount(), entry.getTotalCount())))
                .orElse(null);
        DailyTrainingEntry worst = entries.stream()
                .filter(entry -> entry.getTotalCount() > 0)
                .min(Comparator.comparing(entry -> TrainingCalculator.successRate(entry.getSuccessCount(), entry.getTotalCount())))
                .orElse(null);
        DailyTrainingEntry last = entries.stream().max(Comparator.comparing(DailyTrainingEntry::getTrainingDate)).orElse(null);
        return new CategoryStatViewModel(
                category.getName(),
                CategoryIconMapper.iconKeyFor(category.getKey()),
                CategoryIconMapper.isBeta(category.getKey()),
                entries.size(),
                success,
                total,
                TrainingCalculator.successRate(success, total),
                last == null ? null : last.getTrainingDate(),
                best == null ? null : best.getTrainingDate(),
                best == null ? null : TrainingCalculator.successRate(best.getSuccessCount(), best.getTotalCount()),
                worst == null ? null : worst.getTrainingDate(),
                worst == null ? null : TrainingCalculator.successRate(worst.getSuccessCount(), worst.getTotalCount()),
                trend(entries)
        );
    }

    private String bestCategory(List<CategoryStatViewModel> categories) {
        return categories.stream()
                .filter(category -> category.successRate() != null)
                .max(Comparator.comparing(CategoryStatViewModel::successRate))
                .map(CategoryStatViewModel::categoryName)
                .orElse("-");
    }

    private String weakestCategory(List<CategoryStatViewModel> categories) {
        return categories.stream()
                .filter(category -> category.successRate() != null)
                .min(Comparator.comparing(CategoryStatViewModel::successRate))
                .map(CategoryStatViewModel::categoryName)
                .orElse("-");
    }

    private String trend(List<DailyTrainingEntry> entries) {
        List<DailyTrainingEntry> ordered = entries.stream()
                .filter(entry -> entry.getTotalCount() > 0)
                .sorted(Comparator.comparing(DailyTrainingEntry::getTrainingDate))
                .toList();
        if (ordered.size() < 2) {
            return "stabil";
        }
        int first = TrainingCalculator.successRate(ordered.getFirst().getSuccessCount(), ordered.getFirst().getTotalCount());
        int last = TrainingCalculator.successRate(ordered.getLast().getSuccessCount(), ordered.getLast().getTotalCount());
        if (last >= first + 5) {
            return "steigend";
        }
        if (last <= first - 5) {
            return "fallend";
        }
        return "stabil";
    }

    private String displayRate(Integer rate) {
        return rate == null ? "-" : rate + "%";
    }

    private record StatusCounts(int completed, int partial, int openWithEntries) {
    }
}
