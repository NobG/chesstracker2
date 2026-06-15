package com.nobg.chesstracker2.service;

import com.nobg.chesstracker2.model.DailyTrainingEntry;
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
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StatsService {

    private final DailyTrainingEntryRepository entryRepository;
    private final TrainingCategoryRepository categoryRepository;
    private final DailyNoteRepository noteRepository;
    private final AppDateProvider appDateProvider;

    public StatsService(
            DailyTrainingEntryRepository entryRepository,
            TrainingCategoryRepository categoryRepository,
            DailyNoteRepository noteRepository,
            AppDateProvider appDateProvider
    ) {
        this.entryRepository = entryRepository;
        this.categoryRepository = categoryRepository;
        this.noteRepository = noteRepository;
        this.appDateProvider = appDateProvider;
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
        LocalDate end = appDateProvider.today();
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
        Map<LocalDate, DailyNote> notes = noteRepository.findByTrainingDateBetween(start, end)
                .stream()
                .collect(Collectors.toMap(DailyNote::getTrainingDate, Function.identity()));
        List<LocalDate> daysWithEntries = entries.stream()
                .map(DailyTrainingEntry::getTrainingDate)
                .distinct()
                .toList();

        int completed = 0;
        int partial = 0;
        int open = 0;
        for (LocalDate day : daysWithEntries) {
            DailyNote note = notes.get(day);
            if (note != null && note.isLocked()) {
                completed++;
            } else {
                partial++;
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
        boolean challenge = TrainingCategoryRules.isPointsOnlyCategory(category);
        int success = challenge ? 0 : TrainingCalculator.totalSuccess(entries);
        int total = challenge ? 0 : TrainingCalculator.totalTasks(entries);
        DailyTrainingEntry best = bestEntry(entries, challenge);
        DailyTrainingEntry worst = worstEntry(entries, challenge);
        DailyTrainingEntry last = entries.stream().max(Comparator.comparing(DailyTrainingEntry::getTrainingDate)).orElse(null);
        return new CategoryStatViewModel(
                category.getName(),
                CategoryIconMapper.iconKeyFor(category.getKey()),
                CategoryIconMapper.isBeta(category.getKey()),
                entries.size(),
                success,
                total,
                challenge,
                best == null ? null : TrainingCategoryRules.pointsValue(best),
                latestAimchessRating(category.getKey()),
                TrainingCalculator.successRate(success, total),
                last == null ? null : last.getTrainingDate(),
                best == null ? null : best.getTrainingDate(),
                best == null ? null : displayValue(best, challenge),
                worst == null ? null : worst.getTrainingDate(),
                worst == null ? null : displayValue(worst, challenge),
                trend(entries, challenge)
        );
    }

    private DailyTrainingEntry bestEntry(List<DailyTrainingEntry> entries, boolean challenge) {
        return entries.stream()
                .filter(entry -> challenge ? TrainingCategoryRules.pointsValue(entry) != null : entry.getTotalCount() > 0)
                .max(entryComparator(challenge))
                .orElse(null);
    }

    private DailyTrainingEntry worstEntry(List<DailyTrainingEntry> entries, boolean challenge) {
        return entries.stream()
                .filter(entry -> challenge ? TrainingCategoryRules.pointsValue(entry) != null : entry.getTotalCount() > 0)
                .min(entryComparator(challenge))
                .orElse(null);
    }

    private Comparator<DailyTrainingEntry> entryComparator(boolean challenge) {
        if (challenge) {
            return Comparator
                    .comparingInt((DailyTrainingEntry entry) -> TrainingCategoryRules.pointsValue(entry))
                    .thenComparing(DailyTrainingEntry::getTrainingDate);
        }
        return Comparator.comparing(entry -> TrainingCalculator.successRate(entry.getSuccessCount(), entry.getTotalCount()));
    }

    private Integer displayValue(DailyTrainingEntry entry, boolean challenge) {
        if (challenge) {
            return TrainingCategoryRules.pointsValue(entry);
        }
        return TrainingCalculator.successRate(entry.getSuccessCount(), entry.getTotalCount());
    }

    private Integer latestAimchessRating(String categoryKey) {
        return entryRepository
                .findFirstByCategory_KeyAndScoreIsNotNullOrderByTrainingDateDescUpdatedAtDescIdDesc(categoryKey)
                .map(DailyTrainingEntry::getScore)
                .orElse(null);
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

    private String trend(List<DailyTrainingEntry> entries, boolean challenge) {
        List<DailyTrainingEntry> ordered = entries.stream()
                .filter(entry -> challenge ? TrainingCategoryRules.pointsValue(entry) != null : entry.getTotalCount() > 0)
                .sorted(Comparator.comparing(DailyTrainingEntry::getTrainingDate))
                .toList();
        if (ordered.size() < 2) {
            return "stabil";
        }
        int first = trendValue(ordered.getFirst(), challenge);
        int last = trendValue(ordered.getLast(), challenge);
        int threshold = challenge ? 3 : 5;
        if (last >= first + threshold) {
            return "steigend";
        }
        if (last <= first - threshold) {
            return "fallend";
        }
        return "stabil";
    }

    private int trendValue(DailyTrainingEntry entry, boolean challenge) {
        if (challenge) {
            return TrainingCategoryRules.pointsValue(entry);
        }
        return TrainingCalculator.successRate(entry.getSuccessCount(), entry.getTotalCount());
    }

    private String displayRate(Integer rate) {
        return rate == null ? "-" : rate + "%";
    }

    private record StatusCounts(int completed, int partial, int openWithEntries) {
    }
}
