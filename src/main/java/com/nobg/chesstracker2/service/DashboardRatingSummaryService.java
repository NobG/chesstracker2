package com.nobg.chesstracker2.service;

import com.nobg.chesstracker2.model.DailyTrainingEntry;
import com.nobg.chesstracker2.model.RatingSnapshot;
import com.nobg.chesstracker2.model.TrainingCategory;
import com.nobg.chesstracker2.repository.DailyTrainingEntryRepository;
import com.nobg.chesstracker2.repository.RatingSnapshotRepository;
import com.nobg.chesstracker2.repository.TrainingCategoryRepository;
import com.nobg.chesstracker2.viewmodel.RatingSummaryViewModel;
import com.nobg.chesstracker2.viewmodel.RatingValueViewModel;
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
        List<RatingSnapshot> snapshots = ratingSnapshotRepository.findAllByOrderBySnapshotDateDesc();
        return new RatingSummaryViewModel(
                manualRatings(snapshots),
                aimchessRatings()
        );
    }

    private List<RatingValueViewModel> manualRatings(List<RatingSnapshot> snapshots) {
        List<RatingValueViewModel> ratings = new ArrayList<>();
        addManualRating(ratings, snapshots, "Lichess Blitz", RatingSnapshot::getLichessBlitz);
        addManualRating(ratings, snapshots, "Lichess Rapid", RatingSnapshot::getLichessRapid);
        addManualRating(ratings, snapshots, "Lichess Classical", RatingSnapshot::getLichessClassical);
        addManualRating(ratings, snapshots, "DWZ", RatingSnapshot::getDwz);
        addManualRating(ratings, snapshots, "FIDE Elo", RatingSnapshot::getFideElo);
        return ratings;
    }

    private void addManualRating(
            List<RatingValueViewModel> ratings,
            List<RatingSnapshot> snapshots,
            String label,
            Function<RatingSnapshot, Integer> valueExtractor
    ) {
        Integer latest = null;
        Integer previous = null;
        for (RatingSnapshot snapshot : snapshots) {
            Integer value = valueExtractor.apply(snapshot);
            if (value == null) {
                continue;
            }
            if (latest == null) {
                latest = value;
            } else {
                previous = value;
                break;
            }
        }
        if (latest != null) {
            ratings.add(RatingValueViewModel.of(label, latest, previous));
        }
    }

    private List<RatingValueViewModel> aimchessRatings() {
        return categoryRepository.findByActiveTrueOrderBySortOrderAscNameAsc().stream()
                .filter(category -> !TrainingCategoryRules.isPointsOnlyCategory(category))
                .map(this::aimchessRating)
                .filter(rating -> rating.value() != null)
                .toList();
    }

    private RatingValueViewModel aimchessRating(TrainingCategory category) {
        List<DailyTrainingEntry> entries = trainingEntryRepository
                .findTop2ByCategoryIdAndScoreIsNotNullOrderByTrainingDateDescUpdatedAtDescIdDesc(category.getId());
        Integer latest = entries.isEmpty() ? null : entries.get(0).getScore();
        Integer previous = entries.size() < 2 ? null : entries.get(1).getScore();
        return RatingValueViewModel.of(category.getName(), latest, previous);
    }
}
