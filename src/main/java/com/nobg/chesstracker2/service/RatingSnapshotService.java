package com.nobg.chesstracker2.service;

import com.nobg.chesstracker2.dto.RatingSnapshotForm;
import com.nobg.chesstracker2.model.RatingSnapshot;
import com.nobg.chesstracker2.repository.RatingSnapshotRepository;
import com.nobg.chesstracker2.viewmodel.RatingChangeViewModel;
import com.nobg.chesstracker2.viewmodel.RatingSummaryViewModel;
import com.nobg.chesstracker2.viewmodel.RatingSnapshotRowViewModel;
import com.nobg.chesstracker2.viewmodel.RatingSnapshotViewModel;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RatingSnapshotService {

    private final RatingSnapshotRepository repository;

    public RatingSnapshotService(RatingSnapshotRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public RatingSnapshotViewModel ratingView() {
        List<RatingSnapshot> snapshots = repository.findAllByOrderBySnapshotDateDesc();
        return new RatingSnapshotViewModel(
                new RatingSnapshotForm(),
                snapshots.stream().map(this::toRow).toList(),
                changes(snapshots)
        );
    }

    @Transactional(readOnly = true)
    public RatingSummaryViewModel latestRatingSummary() {
        return repository.findAllByOrderBySnapshotDateDesc().stream()
                .findFirst()
                .map(this::toSummary)
                .orElseGet(() -> new RatingSummaryViewModel(null, null, null, null, null, null, null, null, false));
    }

    @Transactional
    public void saveSnapshot(RatingSnapshotForm form) {
        validate(form);
        RatingSnapshot snapshot = repository.findBySnapshotDate(form.getSnapshotDate())
                .orElseGet(RatingSnapshot::new);
        snapshot.setSnapshotDate(form.getSnapshotDate());
        snapshot.setLichessBlitz(form.getLichessBlitz());
        snapshot.setLichessRapid(form.getLichessRapid());
        snapshot.setLichessClassical(form.getLichessClassical());
        snapshot.setDwz(form.getDwz());
        snapshot.setFideElo(form.getFideElo());
        snapshot.setTacticsRating(form.getTacticsRating());
        snapshot.setEndgameRating(form.getEndgameRating());
        snapshot.setNote(blankToNull(form.getNote()));
        repository.save(snapshot);
    }

    private void validate(RatingSnapshotForm form) {
        if (form.getSnapshotDate() == null) {
            throw new IllegalArgumentException("Datum ist Pflicht.");
        }
        requireNonNegative("Lichess Blitz", form.getLichessBlitz());
        requireNonNegative("Lichess Rapid", form.getLichessRapid());
        requireNonNegative("Lichess Classical", form.getLichessClassical());
        requireNonNegative("DWZ", form.getDwz());
        requireNonNegative("FIDE Elo", form.getFideElo());
        requireNonNegative("Taktik", form.getTacticsRating());
        requireNonNegative("Endspiel", form.getEndgameRating());
    }

    private void requireNonNegative(String label, Integer value) {
        if (value != null && value < 0) {
            throw new IllegalArgumentException(label + " darf nicht negativ sein.");
        }
    }

    private List<RatingChangeViewModel> changes(List<RatingSnapshot> snapshots) {
        if (snapshots.size() < 2) {
            return List.of();
        }
        RatingSnapshot current = snapshots.get(0);
        RatingSnapshot previous = snapshots.get(1);
        List<RatingChangeViewModel> changes = new ArrayList<>();
        addChange(changes, "Lichess Blitz", current.getLichessBlitz(), previous.getLichessBlitz());
        addChange(changes, "Lichess Rapid", current.getLichessRapid(), previous.getLichessRapid());
        addChange(changes, "Lichess Classical", current.getLichessClassical(), previous.getLichessClassical());
        addChange(changes, "DWZ", current.getDwz(), previous.getDwz());
        addChange(changes, "FIDE Elo", current.getFideElo(), previous.getFideElo());
        addChange(changes, "Taktik", current.getTacticsRating(), previous.getTacticsRating());
        addChange(changes, "Endspiel", current.getEndgameRating(), previous.getEndgameRating());
        return changes;
    }

    private void addChange(List<RatingChangeViewModel> changes, String label, Integer current, Integer previous) {
        if (current == null || previous == null) {
            return;
        }
        int difference = current - previous;
        changes.add(new RatingChangeViewModel(label, difference, difference > 0 ? "+" + difference : Integer.toString(difference)));
    }

    private RatingSnapshotRowViewModel toRow(RatingSnapshot snapshot) {
        return new RatingSnapshotRowViewModel(
                snapshot.getSnapshotDate(),
                snapshot.getLichessBlitz(),
                snapshot.getLichessRapid(),
                snapshot.getLichessClassical(),
                snapshot.getDwz(),
                snapshot.getFideElo(),
                snapshot.getTacticsRating(),
                snapshot.getEndgameRating(),
                snapshot.getNote()
        );
    }

    private RatingSummaryViewModel toSummary(RatingSnapshot snapshot) {
        boolean hasAnyRating = snapshot.getLichessBlitz() != null
                || snapshot.getLichessRapid() != null
                || snapshot.getLichessClassical() != null
                || snapshot.getDwz() != null
                || snapshot.getFideElo() != null
                || snapshot.getTacticsRating() != null
                || snapshot.getEndgameRating() != null;
        return new RatingSummaryViewModel(
                snapshot.getSnapshotDate(),
                snapshot.getLichessBlitz(),
                snapshot.getLichessRapid(),
                snapshot.getLichessClassical(),
                snapshot.getDwz(),
                snapshot.getFideElo(),
                snapshot.getTacticsRating(),
                snapshot.getEndgameRating(),
                hasAnyRating
        );
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
