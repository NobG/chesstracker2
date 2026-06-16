package com.nobg.chesstracker2.repository;

import com.nobg.chesstracker2.model.DailyTrainingEntry;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyTrainingEntryRepository extends JpaRepository<DailyTrainingEntry, Long> {

    List<DailyTrainingEntry> findByTrainingDateOrderByCategorySortOrderAsc(LocalDate trainingDate);

    List<DailyTrainingEntry> findByTrainingDateBetweenOrderByTrainingDateAscCategorySortOrderAsc(LocalDate start, LocalDate end);

    Optional<DailyTrainingEntry> findByTrainingDateAndCategoryId(LocalDate trainingDate, Long categoryId);

    Optional<DailyTrainingEntry> findFirstByCategory_KeyAndScoreIsNotNullOrderByTrainingDateDescUpdatedAtDescIdDesc(String categoryKey);

    List<DailyTrainingEntry> findTop2ByCategoryIdAndScoreIsNotNullOrderByTrainingDateDescUpdatedAtDescIdDesc(Long categoryId);
}
