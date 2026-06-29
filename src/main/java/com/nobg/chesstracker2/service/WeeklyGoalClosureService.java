package com.nobg.chesstracker2.service;

import com.nobg.chesstracker2.dto.WeeklyGoalClosureForm;
import com.nobg.chesstracker2.model.WeeklyGoalClosure;
import com.nobg.chesstracker2.model.WeeklyGoalClosureStatus;
import com.nobg.chesstracker2.repository.WeeklyGoalClosureRepository;
import com.nobg.chesstracker2.viewmodel.WeeklyGoalClosureViewModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WeeklyGoalClosureService {

    private static final int MAX_NOTE_LENGTH = 1000;

    private final WeeklyGoalClosureRepository repository;

    public WeeklyGoalClosureService(WeeklyGoalClosureRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public WeeklyGoalClosureViewModel view(int isoYear, int isoWeek) {
        validateIsoWeek(isoWeek);
        return repository.findByIsoYearAndIsoWeek(isoYear, isoWeek)
                .map(this::toView)
                .orElseGet(() -> toView(isoYear, isoWeek, WeeklyGoalClosureStatus.NOT_RECORDED, null));
    }

    @Transactional(readOnly = true)
    public WeeklyGoalClosureForm form(int isoYear, int isoWeek) {
        validateIsoWeek(isoWeek);
        WeeklyGoalClosureForm form = new WeeklyGoalClosureForm();
        repository.findByIsoYearAndIsoWeek(isoYear, isoWeek).ifPresent(closure -> {
            form.setStatus(closure.getStatus().name());
            form.setNote(closure.getNote());
        });
        return form;
    }

    @Transactional
    public void save(int isoYear, int isoWeek, WeeklyGoalClosureForm form) {
        validateIsoWeek(isoWeek);
        WeeklyGoalClosureStatus status = parseStatus(form.getStatus());
        String note = blankToNull(form.getNote());
        if (note != null && note.length() > MAX_NOTE_LENGTH) {
            throw new IllegalArgumentException("Notiz darf maximal 1000 Zeichen lang sein.");
        }

        WeeklyGoalClosure closure = repository.findByIsoYearAndIsoWeek(isoYear, isoWeek)
                .orElseGet(WeeklyGoalClosure::new);
        closure.setIsoYear(isoYear);
        closure.setIsoWeek(isoWeek);
        closure.setStatus(status);
        closure.setNote(note);
        repository.save(closure);
    }

    private WeeklyGoalClosureViewModel toView(WeeklyGoalClosure closure) {
        return toView(closure.getIsoYear(), closure.getIsoWeek(), closure.getStatus(), closure.getNote());
    }

    private WeeklyGoalClosureViewModel toView(int isoYear, int isoWeek, WeeklyGoalClosureStatus status, String note) {
        return new WeeklyGoalClosureViewModel(
                isoYear,
                isoWeek,
                status,
                status.displayText(),
                status.tone(),
                note
        );
    }

    private WeeklyGoalClosureStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status ist Pflicht.");
        }
        try {
            return WeeklyGoalClosureStatus.valueOf(status);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Ungueltiger Wochenabschluss-Status.");
        }
    }

    private void validateIsoWeek(int isoWeek) {
        if (isoWeek < 1 || isoWeek > 53) {
            throw new IllegalArgumentException("Kalenderwoche muss zwischen 1 und 53 liegen.");
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
