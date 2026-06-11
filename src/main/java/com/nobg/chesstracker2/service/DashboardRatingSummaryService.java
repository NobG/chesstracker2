package com.nobg.chesstracker2.service;

import com.nobg.chesstracker2.model.RatingSnapshot;
import com.nobg.chesstracker2.repository.DailyTrainingEntryRepository;
import com.nobg.chesstracker2.repository.RatingSnapshotRepository;
import com.nobg.chesstracker2.viewmodel.RatingSummaryViewModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardRatingSummaryService {

    private static final String TACTICS_CATEGORY_KEY = "tactics";
    private static final String ENDGAME_CATEGORY_KEY = "endgame";

    private final RatingSnapshotRepository ratingSnapshotRepository;
    private final DailyTrainingEntryRepository trainingEntryRepository;

    public DashboardRatingSummaryService(
            RatingSnapshotRepository ratingSnapshotRepository,
            DailyTrainingEntryRepository trainingEntryRepository
    ) {
        this.ratingSnapshotRepository = ratingSnapshotRepository;
        this.trainingEntryRepository = trainingEntryRepository;
    }

    @Transactional(readOnly = true)
    public RatingSummaryViewModel latestSummary() {
        RatingSnapshot latestRating = ratingSnapshotRepository.findAllByOrderBySnapshotDateDesc().stream()
                .findFirst()
                .orElse(null);
        Integer tacticsScore = latestAimchessScoreForCategory(TACTICS_CATEGORY_KEY);
        Integer endgameScore = latestAimchessScoreForCategory(ENDGAME_CATEGORY_KEY);

        if (latestRating == null) {
            return new RatingSummaryViewModel(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    tacticsScore,
                    endgameScore,
                    tacticsScore != null || endgameScore != null
            );
        }

        boolean hasAnyRating = latestRating.getLichessBlitz() != null
                || latestRating.getLichessRapid() != null
                || latestRating.getLichessClassical() != null
                || latestRating.getDwz() != null
                || latestRating.getFideElo() != null
                || tacticsScore != null
                || endgameScore != null;
        return new RatingSummaryViewModel(
                latestRating.getSnapshotDate(),
                latestRating.getLichessBlitz(),
                latestRating.getLichessRapid(),
                latestRating.getLichessClassical(),
                latestRating.getDwz(),
                latestRating.getFideElo(),
                tacticsScore,
                endgameScore,
                hasAnyRating
        );
    }

    private Integer latestAimchessScoreForCategory(String categoryKey) {
        return trainingEntryRepository
                .findFirstByCategory_KeyAndScoreIsNotNullOrderByTrainingDateDescUpdatedAtDescIdDesc(categoryKey)
                .map(entry -> entry.getScore())
                .orElse(null);
    }
}
