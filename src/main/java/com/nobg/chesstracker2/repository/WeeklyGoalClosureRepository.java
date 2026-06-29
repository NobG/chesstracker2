package com.nobg.chesstracker2.repository;

import com.nobg.chesstracker2.model.WeeklyGoalClosure;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeeklyGoalClosureRepository extends JpaRepository<WeeklyGoalClosure, Long> {

    Optional<WeeklyGoalClosure> findByIsoYearAndIsoWeek(int isoYear, int isoWeek);
}
