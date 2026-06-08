package com.nobg.chesstracker2.repository;

import com.nobg.chesstracker2.model.DailyNote;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyNoteRepository extends JpaRepository<DailyNote, Long> {

    Optional<DailyNote> findByTrainingDate(LocalDate trainingDate);
}
