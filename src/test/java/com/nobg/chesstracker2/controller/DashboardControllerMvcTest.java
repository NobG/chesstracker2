package com.nobg.chesstracker2.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nobg.chesstracker2.model.DailyTrainingEntry;
import com.nobg.chesstracker2.model.DailyCompletionStatus;
import com.nobg.chesstracker2.model.TrainingCategory;
import com.nobg.chesstracker2.repository.DailyNoteRepository;
import com.nobg.chesstracker2.repository.DailyTrainingEntryRepository;
import com.nobg.chesstracker2.repository.TrainingCategoryRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DashboardControllerMvcTest {

    private static final List<String> AIMCHESS_CATEGORIES = List.of(
            "Advantage Capitalization",
            "Tactics",
            "Opening Improver",
            "Practice visualization",
            "Blunder Preventer",
            "360 Trainer",
            "Intuition Trainer",
            "Retry Mistakes",
            "Endgame",
            "Defender",
            "Time Trainer",
            "Blindfold Tactics",
            "Checkmate Patterns",
            "Opening Trainer",
            "Tactics Challenge"
    );

    private static final List<String> OLD_CATEGORY_NAMES = List.of(
            "Calculation",
            "Openings",
            "Strategy",
            "Advantage Conversion",
            "Visualization",
            "Endgames",
            "Defense",
            "Blunder Prevention",
            "Time Management"
    );

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TrainingCategoryRepository categoryRepository;

    @Autowired
    private DailyTrainingEntryRepository entryRepository;

    @Autowired
    private DailyNoteRepository noteRepository;

    @BeforeEach
    void cleanEntries() {
        entryRepository.deleteAll();
        noteRepository.deleteAll();
    }

    @Test
    void todayFormRendersEntryFieldNamesWithoutFormPrefix() throws Exception {
        MvcResult result = mockMvc.perform(get("/today"))
                .andExpect(status().isOk())
                .andReturn();
        String html = result.getResponse().getContentAsString();

        assertThat(html)
                .contains(
                        "name=\"entries[0].categoryId\"",
                        "name=\"entries[0].trained\"",
                        "name=\"entries[0].result\"",
                        "name=\"entries[0].score\"",
                        "name=\"entries[0].durationMinutes\"",
                        "name=\"entries[0].note\"",
                        "name=\"dayNote\"",
                        "name=\"completionStatus\"",
                        "value=\"OPEN\"",
                        "value=\"PARTIAL\"",
                        "value=\"COMPLETED\"",
                        "Aimchess Training abgeschlossen",
                        "href=\"/favicon.svg\"",
                        "id=\"icon-category-target\"",
                        "href=\"#icon-category-target\""
                )
                .doesNotContain(
                        "name=\"form.entries[0].categoryId\"",
                        "name=\"form.entries[0].durationMinutes\"",
                        "name=\"form.dayNote\"",
                        "name=\"form.completionStatus\""
                );
    }

    @Test
    void migrationProvidesExactActiveAimchessCategories() {
        List<String> activeCategories = categoryRepository.findByActiveTrueOrderBySortOrderAscNameAsc().stream()
                .map(TrainingCategory::getName)
                .toList();

        assertThat(activeCategories).containsExactlyElementsOf(AIMCHESS_CATEGORIES);
    }

    @Test
    void todayShowsExactActiveAimchessCategoriesWithIconsAndBetaBadge() throws Exception {
        MvcResult result = mockMvc.perform(get("/today"))
                .andExpect(status().isOk())
                .andReturn();
        String html = result.getResponse().getContentAsString();

        assertThat(html).contains(AIMCHESS_CATEGORIES.toArray(String[]::new));
        assertThat(html).doesNotContain(OLD_CATEGORY_NAMES.toArray(String[]::new));
        assertThat(html).contains(
                "href=\"#icon-category-flag\"",
                "href=\"#icon-category-target\"",
                "href=\"#icon-category-book-up\"",
                "href=\"#icon-category-eye\"",
                "href=\"#icon-category-warning\"",
                "href=\"#icon-category-rotate\"",
                "href=\"#icon-category-spark\"",
                "href=\"#icon-category-retry\"",
                "href=\"#icon-category-rook\"",
                "href=\"#icon-category-shield\"",
                "href=\"#icon-category-clock\"",
                "href=\"#icon-category-eye-off\"",
                "href=\"#icon-category-king\"",
                "href=\"#icon-category-book\"",
                "href=\"#icon-category-bolt\"",
                "class=\"beta-badge\"",
                "Beta"
        );
    }

    @Test
    void categoriesRenderFaviconAndCategoryIcons() throws Exception {
        MvcResult result = mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andReturn();
        String html = result.getResponse().getContentAsString();

        assertThat(html)
                .contains(
                        "href=\"/favicon.svg\"",
                        "class=\"category-label\"",
                        "class=\"category-icon\"",
                        "href=\"#icon-category-target\"",
                        "class=\"beta-badge\"",
                        "Tactics Challenge",
                        "Tactics"
                );
    }

    @Test
    void postTodayEntriesBindsIndexedEntryFieldsAndUpdatesExistingEntry() throws Exception {
        TrainingCategory tactics = tactics();
        LocalDate today = LocalDate.now();

        mockMvc.perform(post("/today/entries")
                        .param("entries[0].categoryId", tactics.getId().toString())
                        .param("entries[0].trained", "true")
                        .param("entries[0].result", "3/5")
                        .param("entries[0].score", "1200")
                        .param("entries[0].durationMinutes", "5")
                        .param("entries[0].note", "Test")
                        .param("completionStatus", "PARTIAL")
                        .param("dayNote", "Tagesnotiz"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/today"));

        DailyTrainingEntry saved = entryRepository.findByTrainingDateAndCategoryId(today, tactics.getId()).orElseThrow();
        assertThat(saved.isTrained()).isTrue();
        assertThat(saved.getSuccessCount()).isEqualTo(3);
        assertThat(saved.getTotalCount()).isEqualTo(5);
        assertThat(saved.getDurationMinutes()).isEqualTo(5);
        assertThat(saved.getNote()).isEqualTo("Test");
        assertThat(noteRepository.findByTrainingDate(today).orElseThrow().getCompletionStatus())
                .isEqualTo(DailyCompletionStatus.PARTIAL);

        mockMvc.perform(post("/today/entries")
                        .param("entries[0].categoryId", tactics.getId().toString())
                        .param("entries[0].trained", "true")
                        .param("entries[0].result", "4/5")
                        .param("entries[0].score", "1300")
                        .param("entries[0].durationMinutes", "10")
                        .param("entries[0].note", "Aktualisiert")
                        .param("completionStatus", "COMPLETED"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/today"));

        assertThat(entryRepository.findByTrainingDateOrderByCategorySortOrderAsc(today)).hasSize(1);
        DailyTrainingEntry updated = entryRepository.findByTrainingDateAndCategoryId(today, tactics.getId()).orElseThrow();
        assertThat(updated.getId()).isEqualTo(saved.getId());
        assertThat(updated.getSuccessCount()).isEqualTo(4);
        assertThat(updated.getTotalCount()).isEqualTo(5);
        assertThat(updated.getScore()).isEqualTo(1300);
        assertThat(updated.getDurationMinutes()).isEqualTo(10);
        assertThat(updated.getNote()).isEqualTo("Aktualisiert");
        assertThat(noteRepository.findByTrainingDate(today).orElseThrow().getCompletionStatus())
                .isEqualTo(DailyCompletionStatus.COMPLETED);
    }

    private TrainingCategory tactics() {
        return categoryRepository.findByActiveTrueOrderBySortOrderAscNameAsc().stream()
                .filter(category -> "Tactics".equals(category.getName()))
                .findFirst()
                .orElseThrow();
    }
}
