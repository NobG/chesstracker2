package com.nobg.chesstracker2.service;

import com.nobg.chesstracker2.dto.TrainingDayForm;
import com.nobg.chesstracker2.dto.TrainingEntryForm;
import com.nobg.chesstracker2.model.DailyCompletionStatus;
import com.nobg.chesstracker2.model.DailyNote;
import com.nobg.chesstracker2.model.DailyTrainingEntry;
import com.nobg.chesstracker2.model.TrainingCategory;
import com.nobg.chesstracker2.repository.DailyNoteRepository;
import com.nobg.chesstracker2.repository.DailyTrainingEntryRepository;
import com.nobg.chesstracker2.repository.TrainingCategoryRepository;
import com.nobg.chesstracker2.viewmodel.CategoryEntryViewModel;
import com.nobg.chesstracker2.viewmodel.DaySummaryViewModel;
import com.nobg.chesstracker2.viewmodel.TodayViewModel;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrainingEntryService {

    private final TrainingCategoryRepository categoryRepository;
    private final DailyTrainingEntryRepository entryRepository;
    private final DailyNoteRepository noteRepository;

    public TrainingEntryService(
            TrainingCategoryRepository categoryRepository,
            DailyTrainingEntryRepository entryRepository,
            DailyNoteRepository noteRepository
    ) {
        this.categoryRepository = categoryRepository;
        this.entryRepository = entryRepository;
        this.noteRepository = noteRepository;
    }

    @Transactional(readOnly = true)
    public TodayViewModel todayView(LocalDate date) {
        List<TrainingCategory> categories = categoryRepository.findByActiveTrueOrderBySortOrderAscNameAsc();
        Map<Long, DailyTrainingEntry> existing = entryRepository.findByTrainingDateOrderByCategorySortOrderAsc(date)
                .stream()
                .collect(Collectors.toMap(entry -> entry.getCategory().getId(), Function.identity()));
        String dayNote = noteRepository.findByTrainingDate(date).map(DailyNote::getNote).orElse("");
        DailyCompletionStatus completionStatus = noteRepository.findByTrainingDate(date)
                .map(DailyNote::getCompletionStatus)
                .orElse(DailyCompletionStatus.OPEN);

        TrainingDayForm form = new TrainingDayForm();
        form.setDayNote(dayNote);
        form.setCompletionStatus(completionStatus);
        List<CategoryEntryViewModel> rows = new ArrayList<>();

        for (TrainingCategory category : categories) {
            DailyTrainingEntry entry = existing.get(category.getId());
            TrainingEntryForm entryForm = new TrainingEntryForm();
            entryForm.setCategoryId(category.getId());
            if (entry != null) {
                entryForm.setTrained(entry.isTrained());
                entryForm.setResult(resultText(entry.getSuccessCount(), entry.getTotalCount()));
                entryForm.setScore(entry.getScore());
                entryForm.setDurationMinutes(entry.getDurationMinutes());
                entryForm.setNote(entry.getNote());
            }
            form.getEntries().add(entryForm);
            rows.add(toCategoryView(category, entry));
        }

        return new TodayViewModel(date, form, rows, daySummary(date));
    }

    @Transactional
    public void saveDay(LocalDate date, TrainingDayForm form) {
        Map<Long, TrainingCategory> categories = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(TrainingCategory::getId, Function.identity()));

        for (TrainingEntryForm entryForm : form.getEntries()) {
            TrainingCategory category = categories.get(entryForm.getCategoryId());
            if (category == null) {
                throw new IllegalArgumentException("Unbekannte Trainingskategorie.");
            }

            TrainingResult result = TrainingCalculator.parseResult(entryForm.getResult());
            if (entryForm.isTrained() && result.totalCount() == 0) {
                throw new IllegalArgumentException("Trainierte Kategorien brauchen ein Ergebnis wie 7/10.");
            }
            if (entryForm.getDurationMinutes() != null && entryForm.getDurationMinutes() < 0) {
                throw new IllegalArgumentException("Zeitaufwand darf nicht negativ sein.");
            }

            DailyTrainingEntry entry = entryRepository.findByTrainingDateAndCategoryId(date, category.getId())
                    .orElseGet(DailyTrainingEntry::new);
            entry.setTrainingDate(date);
            entry.setCategory(category);
            entry.setTrained(entryForm.isTrained());
            entry.setSuccessCount(result.successCount());
            entry.setTotalCount(result.totalCount());
            entry.setScore(entryForm.getScore());
            entry.setDurationMinutes(entryForm.getDurationMinutes());
            entry.setNote(blankToNull(entryForm.getNote()));
            entryRepository.save(entry);
        }

        DailyNote dailyNote = noteRepository.findByTrainingDate(date).orElseGet(DailyNote::new);
        dailyNote.setTrainingDate(date);
        dailyNote.setNote(blankToNull(form.getDayNote()));
        dailyNote.setCompletionStatus(form.getCompletionStatus());
        noteRepository.save(dailyNote);
    }

    @Transactional(readOnly = true)
    public DaySummaryViewModel daySummary(LocalDate date) {
        List<DailyTrainingEntry> trainedEntries = entryRepository.findByTrainingDateOrderByCategorySortOrderAsc(date)
                .stream()
                .filter(DailyTrainingEntry::isTrained)
                .toList();
        String dayNote = noteRepository.findByTrainingDate(date).map(DailyNote::getNote).orElse("");
        DailyCompletionStatus completionStatus = noteRepository.findByTrainingDate(date)
                .map(DailyNote::getCompletionStatus)
                .orElse(DailyCompletionStatus.OPEN);
        int success = TrainingCalculator.totalSuccess(trainedEntries);
        int total = TrainingCalculator.totalTasks(trainedEntries);
        int duration = TrainingCalculator.totalDuration(trainedEntries);
        Integer rate = TrainingCalculator.successRate(success, total);
        List<CategoryEntryViewModel> entries = trainedEntries.stream()
                .sorted(Comparator.comparing(entry -> entry.getCategory().getSortOrder()))
                .map(entry -> toCategoryView(entry.getCategory(), entry))
                .toList();
        String summary = automaticSummary(entries.size(), rate, duration);
        return new DaySummaryViewModel(
                date,
                entries,
                entries.size(),
                success,
                total,
                rate,
                duration,
                dayNote,
                completionStatus,
                completionStatus.displayLabel(),
                summary,
                copyBlock(date, entries, success, total, rate, duration, dayNote, completionStatus)
        );
    }

    private CategoryEntryViewModel toCategoryView(TrainingCategory category, DailyTrainingEntry entry) {
        if (entry == null) {
            return new CategoryEntryViewModel(
                    category.getId(),
                    category.getName(),
                    CategoryIconMapper.iconKeyFor(category.getKey()),
                    category.getDescription(),
                    false,
                    "",
                    null,
                    null,
                    "",
                    null
            );
        }
        return new CategoryEntryViewModel(
                category.getId(),
                category.getName(),
                CategoryIconMapper.iconKeyFor(category.getKey()),
                category.getDescription(),
                entry.isTrained(),
                resultText(entry.getSuccessCount(), entry.getTotalCount()),
                entry.getScore(),
                entry.getDurationMinutes(),
                entry.getNote(),
                TrainingCalculator.successRate(entry.getSuccessCount(), entry.getTotalCount())
        );
    }

    private String copyBlock(
            LocalDate date,
            List<CategoryEntryViewModel> entries,
            int success,
            int total,
            Integer rate,
            int duration,
            String dayNote,
            DailyCompletionStatus completionStatus
    ) {
        StringBuilder builder = new StringBuilder();
        builder.append("Aimchess Training - ").append(date).append("\n\n");
        builder.append("Tagesstatus: ").append(completionStatus.displayLabel()).append("\n\n");
        builder.append("Trainierte Kategorien:\n");
        if (entries.isEmpty()) {
            builder.append("- Keine Eintraege\n");
        } else {
            for (CategoryEntryViewModel entry : entries) {
                builder.append("- ").append(entry.categoryName()).append(": ").append(entry.result());
                if (entry.successRate() != null) {
                    builder.append(" = ").append(entry.successRate()).append("%");
                }
                if (entry.score() != null) {
                    builder.append(", Score: ").append(entry.score());
                }
                if (entry.durationMinutes() != null) {
                    builder.append(", Zeit: ").append(entry.durationMinutes()).append(" min");
                }
                if (entry.note() != null && !entry.note().isBlank()) {
                    builder.append(", Notiz: ").append(entry.note());
                }
                builder.append("\n");
            }
        }
        builder.append("\nGesamt:\n");
        builder.append("- Aufgaben: ").append(success).append("/").append(total);
        if (rate != null) {
            builder.append(" = ").append(rate).append("%");
        }
        builder.append("\n");
        builder.append("- Zeit: ").append(duration).append(" min\n");
        builder.append("- Tagesnotiz: ").append(dayNote == null || dayNote.isBlank() ? "-" : dayNote).append("\n\n");
        builder.append("Bitte bewerte mein heutiges Aimchess-Training und gib mir konkrete Hinweise fuer morgen.");
        return builder.toString();
    }

    private String automaticSummary(int trainedCategories, Integer rate, int duration) {
        if (trainedCategories == 0) {
            return "Heute sind noch keine Aimchess-Einheiten erfasst.";
        }
        String quality = rate == null ? "ohne Quote" : rate >= 75 ? "stark" : rate >= 60 ? "solide" : "ausbaufaehig";
        return "Du hast " + trainedCategories + " Kategorien trainiert. Die Tagesleistung wirkt " + quality
                + " bei " + duration + " Minuten Trainingszeit.";
    }

    private String resultText(int success, int total) {
        if (total == 0) {
            return "";
        }
        return success + "/" + total;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
