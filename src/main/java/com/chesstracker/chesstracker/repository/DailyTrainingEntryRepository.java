package com.chesstracker.chesstracker.repository;

import com.chesstracker.chesstracker.model.DailyTrainingEntry;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyTrainingEntryRepository extends JpaRepository<DailyTrainingEntry, Long> {

    List<DailyTrainingEntry> findByTrainingDateOrderByCategorySortOrderAsc(LocalDate trainingDate);

    List<DailyTrainingEntry> findByTrainingDateBetweenOrderByTrainingDateAscCategorySortOrderAsc(LocalDate start, LocalDate end);

    Optional<DailyTrainingEntry> findByTrainingDateAndCategoryId(LocalDate trainingDate, Long categoryId);
}
