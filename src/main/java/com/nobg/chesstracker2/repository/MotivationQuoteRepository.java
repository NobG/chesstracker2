package com.nobg.chesstracker2.repository;

import com.nobg.chesstracker2.model.MotivationQuote;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MotivationQuoteRepository extends JpaRepository<MotivationQuote, Long> {

    List<MotivationQuote> findByActiveTrueOrderBySortOrderAscIdAsc();
}
