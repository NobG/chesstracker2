package com.nobg.chesstracker2.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nobg.chesstracker2.dto.RatingSnapshotForm;
import com.nobg.chesstracker2.model.RatingSnapshot;
import com.nobg.chesstracker2.repository.RatingSnapshotRepository;
import com.nobg.chesstracker2.viewmodel.RatingSnapshotViewModel;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class RatingSnapshotServiceTest {

    private final RatingSnapshotRepository repository = org.mockito.Mockito.mock(RatingSnapshotRepository.class);
    private final AppDateProvider appDateProvider = org.mockito.Mockito.mock(AppDateProvider.class);
    private final RatingSnapshotService service = new RatingSnapshotService(repository, appDateProvider);

    @Test
    void saveSnapshotCreatesNewSnapshot() {
        LocalDate date = LocalDate.of(2026, 6, 8);
        when(repository.findBySnapshotDate(date)).thenReturn(Optional.empty());
        RatingSnapshotForm form = form(date, 1800, 1900, null, null, null, "Start");

        service.saveSnapshot(form);

        ArgumentCaptor<RatingSnapshot> captor = ArgumentCaptor.forClass(RatingSnapshot.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getSnapshotDate()).isEqualTo(date);
        assertThat(captor.getValue().getLichessBlitz()).isEqualTo(1800);
        assertThat(captor.getValue().getLichessRapid()).isEqualTo(1900);
        assertThat(captor.getValue().getNote()).isEqualTo("Start");
    }

    @Test
    void saveSnapshotUpdatesExistingSnapshotForSameDate() {
        LocalDate date = LocalDate.of(2026, 6, 8);
        RatingSnapshot existing = snapshot(date, 1800, null, null, null, null, null);
        when(repository.findBySnapshotDate(date)).thenReturn(Optional.of(existing));

        service.saveSnapshot(form(date, 1850, null, null, 1700, null, "Update"));

        verify(repository).save(existing);
        assertThat(existing.getLichessBlitz()).isEqualTo(1850);
        assertThat(existing.getDwz()).isEqualTo(1700);
        assertThat(existing.getNote()).isEqualTo("Update");
    }

    @Test
    void ratingViewCalculatesChangesAndIgnoresMissingValues() {
        RatingSnapshot current = snapshot(LocalDate.of(2026, 6, 8), 1825, null, null, 1718, null, null);
        RatingSnapshot previous = snapshot(LocalDate.of(2026, 6, 1), 1800, 1900, null, 1700, null, null);
        when(repository.findAllByOrderBySnapshotDateDescUpdatedAtDescIdDesc()).thenReturn(List.of(current, previous));
        when(appDateProvider.today()).thenReturn(LocalDate.of(2026, 6, 9));

        RatingSnapshotViewModel view = service.ratingView();

        assertThat(view.changes()).extracting("label", "displayDifference")
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("Lichess Blitz", "+25"),
                        org.assertj.core.groups.Tuple.tuple("DWZ", "+18")
                );
    }

    @Test
    void prefilledFormUsesTodayAndLatestSnapshotValues() {
        LocalDate today = LocalDate.of(2026, 6, 9);
        RatingSnapshot latest = snapshot(LocalDate.of(2026, 6, 1), 1643, 1624, 1760, 1627, 1670, "Old note");
        when(appDateProvider.today()).thenReturn(today);
        when(repository.findAllByOrderBySnapshotDateDescUpdatedAtDescIdDesc()).thenReturn(List.of(latest));

        RatingSnapshotForm form = service.prefilledForm();

        assertThat(form.getSnapshotDate()).isEqualTo(today);
        assertThat(form.getLichessBlitz()).isEqualTo(1643);
        assertThat(form.getLichessRapid()).isEqualTo(1624);
        assertThat(form.getLichessClassical()).isEqualTo(1760);
        assertThat(form.getDwz()).isEqualTo(1627);
        assertThat(form.getFideElo()).isEqualTo(1670);
        assertThat(form.getNote()).isNull();
    }

    @Test
    void prefilledFormUsesTodaySnapshotIncludingNoteWhenPresent() {
        LocalDate today = LocalDate.of(2026, 6, 9);
        RatingSnapshot todaySnapshot = snapshot(today, 1650, 1625, 1761, 1628, 1671, "Today note");
        RatingSnapshot older = snapshot(LocalDate.of(2026, 6, 1), 1643, 1624, 1760, 1627, 1670, "Old note");
        when(appDateProvider.today()).thenReturn(today);
        when(repository.findAllByOrderBySnapshotDateDescUpdatedAtDescIdDesc()).thenReturn(List.of(todaySnapshot, older));

        RatingSnapshotForm form = service.prefilledForm();

        assertThat(form.getSnapshotDate()).isEqualTo(today);
        assertThat(form.getLichessBlitz()).isEqualTo(1650);
        assertThat(form.getLichessRapid()).isEqualTo(1625);
        assertThat(form.getLichessClassical()).isEqualTo(1761);
        assertThat(form.getDwz()).isEqualTo(1628);
        assertThat(form.getFideElo()).isEqualTo(1671);
        assertThat(form.getNote()).isEqualTo("Today note");
    }

    @Test
    void prefilledFormUsesTodayWithEmptyRatingsWhenNoSnapshotsExist() {
        LocalDate today = LocalDate.of(2026, 6, 9);
        when(appDateProvider.today()).thenReturn(today);
        when(repository.findAllByOrderBySnapshotDateDescUpdatedAtDescIdDesc()).thenReturn(List.of());

        RatingSnapshotForm form = service.prefilledForm();

        assertThat(form.getSnapshotDate()).isEqualTo(today);
        assertThat(form.getLichessBlitz()).isNull();
        assertThat(form.getLichessRapid()).isNull();
        assertThat(form.getLichessClassical()).isNull();
        assertThat(form.getDwz()).isNull();
        assertThat(form.getFideElo()).isNull();
        assertThat(form.getNote()).isNull();
    }

    @Test
    void saveSnapshotRejectsNegativeRatingsAndAllowsEmptyOptionalFields() {
        LocalDate date = LocalDate.of(2026, 6, 8);
        RatingSnapshotForm empty = form(date, null, null, null, null, null, "");
        when(repository.findBySnapshotDate(date)).thenReturn(Optional.empty());

        service.saveSnapshot(empty);

        verify(repository).save(any(RatingSnapshot.class));

        RatingSnapshotForm negative = form(date, -1, null, null, null, null, null);
        assertThatThrownBy(() -> service.saveSnapshot(negative))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Lichess Blitz");
    }

    private RatingSnapshotForm form(
            LocalDate date,
            Integer blitz,
            Integer rapid,
            Integer classical,
            Integer dwz,
            Integer fide,
            String note
    ) {
        RatingSnapshotForm form = new RatingSnapshotForm();
        form.setSnapshotDate(date);
        form.setLichessBlitz(blitz);
        form.setLichessRapid(rapid);
        form.setLichessClassical(classical);
        form.setDwz(dwz);
        form.setFideElo(fide);
        form.setNote(note);
        return form;
    }

    private RatingSnapshot snapshot(
            LocalDate date,
            Integer blitz,
            Integer rapid,
            Integer classical,
            Integer dwz,
            Integer fide,
            String note
    ) {
        RatingSnapshot snapshot = new RatingSnapshot();
        snapshot.setSnapshotDate(date);
        snapshot.setLichessBlitz(blitz);
        snapshot.setLichessRapid(rapid);
        snapshot.setLichessClassical(classical);
        snapshot.setDwz(dwz);
        snapshot.setFideElo(fide);
        snapshot.setNote(note);
        return snapshot;
    }
}
