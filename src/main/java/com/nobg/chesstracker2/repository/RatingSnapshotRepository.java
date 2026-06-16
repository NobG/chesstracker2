package com.nobg.chesstracker2.repository;

import com.nobg.chesstracker2.model.RatingSnapshot;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RatingSnapshotRepository extends JpaRepository<RatingSnapshot, Long> {

    Optional<RatingSnapshot> findBySnapshotDate(LocalDate snapshotDate);

    List<RatingSnapshot> findAllByOrderBySnapshotDateDescUpdatedAtDescIdDesc();
}
