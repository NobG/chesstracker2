package com.nobg.chesstracker2.service;

import com.nobg.chesstracker2.model.DailyTrainingEntry;
import com.nobg.chesstracker2.model.RatingSnapshot;
import com.nobg.chesstracker2.model.TrainingCategory;
import com.nobg.chesstracker2.repository.DailyTrainingEntryRepository;
import com.nobg.chesstracker2.repository.RatingSnapshotRepository;
import com.nobg.chesstracker2.repository.TrainingCategoryRepository;
import com.nobg.chesstracker2.viewmodel.RatingSummaryViewModel;
import com.nobg.chesstracker2.viewmodel.RatingValueViewModel;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardRatingSummaryService {

    private final RatingSnapshotRepository ratingSnapshotRepository;
    private final DailyTrainingEntryRepository trainingEntryRepository;
    private final TrainingCategoryRepository categoryRepository;

    public DashboardRatingSummaryService(
            RatingSnapshotRepository ratingSnapshotRepository,
            DailyTrainingEntryRepository trainingEntryRepository,
            TrainingCategoryRepository categoryRepository
    ) {
        this.ratingSnapshotRepository = ratingSnapshotRepository;
        this.trainingEntryRepository = trainingEntryRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public RatingSummaryViewModel latestSummary() {
        List<RatingSnapshot> snapshots = ratingSnapshotRepository.findAllByOrderBySnapshotDateDescUpdatedAtDescIdDesc();
        return new RatingSummaryViewModel(
                latestManualRatings(snapshots),
                latestAimchessRatings()
        );
    }

    @Transactional(readOnly = true)
    public RatingSummaryViewModel forToday(LocalDate today) {
        return forPeriod(today, today, "seit gestern");
    }

    @Transactional(readOnly = true)
    public RatingSummaryViewModel forWeek(LocalDate start, LocalDate end) {
        return forPeriod(start, end, "diese Woche");
    }

    @Transactional(readOnly = true)
    public RatingSummaryViewModel forMonth(LocalDate start, LocalDate end) {
        return forPeriod(start, end, "diesen Monat");
    }

    private RatingSummaryViewModel forPeriod(LocalDate periodStart, LocalDate periodEnd, String changeSuffix) {
        List<RatingSnapshot> snapshots = ratingSnapshotRepository.findAllByOrderBySnapshotDateDescUpdatedAtDescIdDesc();
        return new RatingSummaryViewModel(
                periodManualRatings(snapshots, periodStart, periodEnd, changeSuffix),
                periodAimchessRatings(periodStart, periodEnd, changeSuffix)
        );
    }

    private List<RatingValueViewModel> latestManualRatings(List<RatingSnapshot> snapshots) {
        List<RatingValueViewModel> ratings = new ArrayList<>();
        addLatestManualRating(ratings, snapshots, "Lichess Blitz", RatingSnapshot::getLichessBlitz);
        addLatestManualRating(ratings, snapshots, "Lichess Rapid", RatingSnapshot::getLichessRapid);
        addLatestManualRating(ratings, snapshots, "Lichess Classical", RatingSnapshot::getLichessClassical);
        addLatestManualRating(ratings, snapshots, "DWZ", RatingSnapshot::getDwz);
        addLatestManualRating(ratings, snapshots, "FIDE Elo", RatingSnapshot::getFideElo);
        return ratings;
    }

    private List<RatingValueViewModel> periodManualRatings(
            List<RatingSnapshot> snapshots,
            LocalDate periodStart,
            LocalDate periodEnd,
            String changeSuffix
    ) {
        List<RatingValueViewModel> ratings = new ArrayList<>();
        addPeriodManualRating(ratings, snapshots, "Lichess Blitz", RatingSnapshot::getLichessBlitz, periodStart, periodEnd, changeSuffix);
        addPeriodManualRating(ratings, snapshots, "Lichess Rapid", RatingSnapshot::getLichessRapid, periodStart, periodEnd, changeSuffix);
        addPeriodManualRating(ratings, snapshots, "Lichess Classical", RatingSnapshot::getLichessClassical, periodStart, periodEnd, changeSuffix);
        addPeriodManualRating(ratings, snapshots, "DWZ", RatingSnapshot::getDwz, periodStart, periodEnd, changeSuffix);
        addPeriodManualRating(ratings, snapshots, "FIDE Elo", RatingSnapshot::getFideElo, periodStart, periodEnd, changeSuffix);
        return ratings;
    }

    private void addLatestManualRating(
            List<RatingValueViewModel> ratings,
            List<RatingSnapshot> snapshots,
            String label,
            Function<RatingSnapshot, Integer> valueExtractor
    ) {
        Integer latest = latestManualValue(snapshots, valueExtractor);
        if (latest != null) {
            ratings.add(RatingValueViewModel.latest(label, latest));
        }
    }

    private void addPeriodManualRating(
            List<RatingValueViewModel> ratings,
            List<RatingSnapshot> snapshots,
            String label,
            Function<RatingSnapshot, Integer> valueExtractor,
            LocalDate periodStart,
            LocalDate periodEnd,
            String changeSuffix
    ) {
        Integer latest = latestManualValue(snapshots, valueExtractor);
        if (latest == null) {
            return;
        }
        Integer periodValue = latestManualValueBetween(snapshots, valueExtractor, periodStart, periodEnd);
        Integer previousValue = latestManualValueBefore(snapshots, valueExtractor, periodStart);
        ratings.add(RatingValueViewModel.period(label, latest, periodValue, previousValue, changeSuffix));
    }

    private Integer latestManualValue(List<RatingSnapshot> snapshots, Function<RatingSnapshot, Integer> valueExtractor) {
        for (RatingSnapshot snapshot : snapshots) {
            Integer value = valueExtractor.apply(snapshot);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private Integer latestManualValueBetween(
            List<RatingSnapshot> snapshots,
            Function<RatingSnapshot, Integer> valueExtractor,
            LocalDate periodStart,
            LocalDate periodEnd
    ) {
        for (RatingSnapshot snapshot : snapshots) {
            Integer value = valueExtractor.apply(snapshot);
            if (value != null
                    && !snapshot.getSnapshotDate().isBefore(periodStart)
                    && !snapshot.getSnapshotDate().isAfter(periodEnd)) {
                return value;
            }
        }
        return null;
    }

    private Integer latestManualValueBefore(
            List<RatingSnapshot> snapshots,
            Function<RatingSnapshot, Integer> valueExtractor,
            LocalDate periodStart
    ) {
        for (RatingSnapshot snapshot : snapshots) {
            Integer value = valueExtractor.apply(snapshot);
            if (value != null && snapshot.getSnapshotDate().isBefore(periodStart)) {
                return value;
            }
        }
        return null;
    }

    private List<RatingValueViewModel> latestAimchessRatings() {
        return categoryRepository.findByActiveTrueOrderBySortOrderAscNameAsc().stream()
                .filter(TrainingCategoryRules::isRatingCategory)
                .map(this::latestAimchessRating)
                .filter(rating -> rating.value() != null)
                .toList();
    }

    private List<RatingValueViewModel> periodAimchessRatings(LocalDate periodStart, LocalDate periodEnd, String changeSuffix) {
        return categoryRepository.findByActiveTrueOrderBySortOrderAscNameAsc().stream()
                .filter(TrainingCategoryRules::isRatingCategory)
                .map(category -> periodAimchessRating(category, periodStart, periodEnd, changeSuffix))
                .filter(rating -> rating.value() != null)
                .toList();
    }

    private RatingValueViewModel latestAimchessRating(TrainingCategory category) {
        Integer latest = trainingEntryRepository
                .findFirstByCategoryIdAndScoreIsNotNullOrderByTrainingDateDescUpdatedAtDescIdDesc(category.getId())
                .map(DailyTrainingEntry::getScore)
                .orElse(null);
        return RatingValueViewModel.latest(category.getName(), latest);
    }

    private RatingValueViewModel periodAimchessRating(
            TrainingCategory category,
            LocalDate periodStart,
            LocalDate periodEnd,
            String changeSuffix
    ) {
        Integer latest = trainingEntryRepository
                .findFirstByCategoryIdAndScoreIsNotNullOrderByTrainingDateDescUpdatedAtDescIdDesc(category.getId())
                .map(DailyTrainingEntry::getScore)
                .orElse(null);
        Integer periodValue = trainingEntryRepository
                .findFirstByCategoryIdAndTrainingDateBetweenAndScoreIsNotNullOrderByTrainingDateDescUpdatedAtDescIdDesc(category.getId(), periodStart, periodEnd)
                .map(DailyTrainingEntry::getScore)
                .orElse(null);
        Integer previousValue = trainingEntryRepository
                .findFirstByCategoryIdAndTrainingDateBeforeAndScoreIsNotNullOrderByTrainingDateDescUpdatedAtDescIdDesc(category.getId(), periodStart)
                .map(DailyTrainingEntry::getScore)
                .orElse(null);
        return RatingValueViewModel.period(category.getName(), latest, periodValue, previousValue, changeSuffix);
    }
}
