package com.nobg.chesstracker2.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nobg.chesstracker2.dto.WeeklyGoalClosureForm;
import com.nobg.chesstracker2.model.WeeklyGoalClosure;
import com.nobg.chesstracker2.model.WeeklyGoalClosureStatus;
import com.nobg.chesstracker2.repository.WeeklyGoalClosureRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class WeeklyGoalClosureServiceTest {

    private final WeeklyGoalClosureRepository repository = org.mockito.Mockito.mock(WeeklyGoalClosureRepository.class);
    private final WeeklyGoalClosureService service = new WeeklyGoalClosureService(repository);

    @Test
    void viewReturnsDefaultStatusWhenWeekIsNotRecorded() {
        when(repository.findByIsoYearAndIsoWeek(2026, 27)).thenReturn(Optional.empty());

        var view = service.view(2026, 27);

        assertThat(view.status()).isEqualTo(WeeklyGoalClosureStatus.NOT_RECORDED);
        assertThat(view.displayText()).isEqualTo("Aimchess Wochenabschluss noch nicht erfasst");
        assertThat(view.note()).isNull();
    }

    @Test
    void saveCreatesNewWeeklyGoalClosure() {
        when(repository.findByIsoYearAndIsoWeek(2026, 27)).thenReturn(Optional.empty());

        service.save(2026, 27, form("ACHIEVED", "Play 10 Rapid games: 10/10"));

        ArgumentCaptor<WeeklyGoalClosure> captor = ArgumentCaptor.forClass(WeeklyGoalClosure.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getIsoYear()).isEqualTo(2026);
        assertThat(captor.getValue().getIsoWeek()).isEqualTo(27);
        assertThat(captor.getValue().getStatus()).isEqualTo(WeeklyGoalClosureStatus.ACHIEVED);
        assertThat(captor.getValue().getNote()).isEqualTo("Play 10 Rapid games: 10/10");
    }

    @Test
    void saveUpdatesExistingWeeklyGoalClosureForSameWeek() {
        WeeklyGoalClosure existing = new WeeklyGoalClosure();
        existing.setIsoYear(2026);
        existing.setIsoWeek(27);
        existing.setStatus(WeeklyGoalClosureStatus.MISSED);
        when(repository.findByIsoYearAndIsoWeek(2026, 27)).thenReturn(Optional.of(existing));

        service.save(2026, 27, form("ACHIEVED", "Update"));

        verify(repository).save(existing);
        assertThat(existing.getStatus()).isEqualTo(WeeklyGoalClosureStatus.ACHIEVED);
        assertThat(existing.getNote()).isEqualTo("Update");
    }

    @Test
    void formPrefillsExistingWeeklyGoalClosure() {
        WeeklyGoalClosure existing = new WeeklyGoalClosure();
        existing.setIsoYear(2026);
        existing.setIsoWeek(27);
        existing.setStatus(WeeklyGoalClosureStatus.MISSED);
        existing.setNote("1/5 Tactics Challenge");
        when(repository.findByIsoYearAndIsoWeek(2026, 27)).thenReturn(Optional.of(existing));

        WeeklyGoalClosureForm form = service.form(2026, 27);

        assertThat(form.getStatus()).isEqualTo("MISSED");
        assertThat(form.getNote()).isEqualTo("1/5 Tactics Challenge");
    }

    @Test
    void saveRejectsInvalidStatusAndTooLongNote() {
        assertThatThrownBy(() -> service.save(2026, 27, form("DONE", null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ungueltiger");

        assertThatThrownBy(() -> service.save(2026, 27, form("ACHIEVED", "x".repeat(1001))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("1000 Zeichen");
    }

    private WeeklyGoalClosureForm form(String status, String note) {
        WeeklyGoalClosureForm form = new WeeklyGoalClosureForm();
        form.setStatus(status);
        form.setNote(note);
        return form;
    }
}
