package com.nobg.chesstracker2.service;

import com.nobg.chesstracker2.dto.RatingSnapshotForm;
import com.nobg.chesstracker2.model.RatingSnapshot;
import com.nobg.chesstracker2.repository.RatingSnapshotRepository;
import com.nobg.chesstracker2.viewmodel.RatingChangeViewModel;
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
    private final AppDateProvider appDateProvider;

    public RatingSnapshotService(RatingSnapshotRepository repository, AppDateProvider appDateProvider) {
        this.repository = repository;
        this.appDateProvider = appDateProvider;
    }

    @Transactional(readOnly = true)
    public RatingSnapshotViewModel ratingView() {
        List<RatingSnapshot> snapshots = repository.findAllByOrderBySnapshotDateDescUpdatedAtDescIdDesc();
        return new RatingSnapshotViewModel(
                prefilledForm(snapshots),
                snapshots.stream().map(this::toRow).toList(),
                changes(snapshots)
        );
    }

    @Transactional(readOnly = true)
    public RatingSnapshotForm prefilledForm() {
        return prefilledForm(repository.findAllByOrderBySnapshotDateDescUpdatedAtDescIdDesc());
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
        snapshot.setNote(blankToNull(form.getNote()));
        repository.save(snapshot);
    }

    private RatingSnapshotForm prefilledForm(List<RatingSnapshot> snapshots) {
        LocalDate today = appDateProvider.today();
        RatingSnapshotForm form = new RatingSnapshotForm();
        form.setSnapshotDate(today);

        RatingSnapshot todaySnapshot = snapshots.stream()
                .filter(snapshot -> today.equals(snapshot.getSnapshotDate()))
                .findFirst()
                .orElse(null);
        if (todaySnapshot != null) {
            copyRatingsToForm(form, todaySnapshot);
            form.setNote(todaySnapshot.getNote());
            return form;
        }

        snapshots.stream()
                .findFirst()
                .ifPresent(snapshot -> copyRatingsToForm(form, snapshot));
        return form;
    }

    private void copyRatingsToForm(RatingSnapshotForm form, RatingSnapshot snapshot) {
        form.setLichessBlitz(snapshot.getLichessBlitz());
        form.setLichessRapid(snapshot.getLichessRapid());
        form.setLichessClassical(snapshot.getLichessClassical());
        form.setDwz(snapshot.getDwz());
        form.setFideElo(snapshot.getFideElo());
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
                snapshot.getNote()
        );
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
