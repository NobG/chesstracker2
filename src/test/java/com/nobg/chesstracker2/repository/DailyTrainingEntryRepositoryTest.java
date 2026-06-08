package com.nobg.chesstracker2.repository;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nobg.chesstracker2.model.DailyTrainingEntry;
import com.nobg.chesstracker2.model.TrainingCategory;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class DailyTrainingEntryRepositoryTest {

    @Autowired
    private TrainingCategoryRepository categoryRepository;

    @Autowired
    private DailyTrainingEntryRepository entryRepository;

    @Test
    void rejectsDuplicateDateAndCategory() {
        TrainingCategory category = new TrainingCategory();
        category.setKey("test-category");
        category.setName("Test Category");
        category.setDescription("Test");
        category.setSortOrder(1);
        category.setActive(true);
        TrainingCategory savedCategory = categoryRepository.saveAndFlush(category);

        DailyTrainingEntry first = entry(savedCategory);
        DailyTrainingEntry duplicate = entry(savedCategory);

        entryRepository.saveAndFlush(first);

        assertThatThrownBy(() -> entryRepository.saveAndFlush(duplicate))
                .isInstanceOf(RuntimeException.class);
    }

    private DailyTrainingEntry entry(TrainingCategory category) {
        DailyTrainingEntry entry = new DailyTrainingEntry();
        entry.setTrainingDate(LocalDate.of(2026, 6, 8));
        entry.setCategory(category);
        entry.setTrained(true);
        entry.setSuccessCount(7);
        entry.setTotalCount(10);
        entry.setDurationMinutes(15);
        return entry;
    }
}
