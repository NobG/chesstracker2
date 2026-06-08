package com.chesstracker.chesstracker.repository;

import com.chesstracker.chesstracker.model.TrainingCategory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainingCategoryRepository extends JpaRepository<TrainingCategory, Long> {

    List<TrainingCategory> findByActiveTrueOrderBySortOrderAscNameAsc();
}
