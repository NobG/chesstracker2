package com.nobg.chesstracker2.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nobg.chesstracker2.model.DailyTrainingEntry;
import com.nobg.chesstracker2.model.DailyCompletionStatus;
import com.nobg.chesstracker2.model.TrainingCategory;
import com.nobg.chesstracker2.model.RatingSnapshot;
import com.nobg.chesstracker2.repository.DailyNoteRepository;
import com.nobg.chesstracker2.repository.DailyTrainingEntryRepository;
import com.nobg.chesstracker2.repository.MotivationQuoteRepository;
import com.nobg.chesstracker2.repository.RatingSnapshotRepository;
import com.nobg.chesstracker2.repository.TrainingCategoryRepository;
import com.nobg.chesstracker2.service.AppDateProvider;
import com.nobg.chesstracker2.service.MotivationQuoteService;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "norbert")
class DashboardControllerMvcTest {

    private static final LocalDate APP_TODAY = LocalDate.of(2026, 6, 9);

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

    @Autowired
    private MotivationQuoteRepository quoteRepository;

    @Autowired
    private RatingSnapshotRepository ratingSnapshotRepository;

    @Autowired
    private MotivationQuoteService quoteService;

    @Autowired
    private Environment environment;

    @MockBean
    private AppDateProvider appDateProvider;

    @BeforeEach
    void cleanEntries() {
        when(appDateProvider.today()).thenReturn(APP_TODAY);
        when(appDateProvider.currentMonth()).thenReturn(YearMonth.from(APP_TODAY));
        when(appDateProvider.currentIsoYear()).thenReturn(2026);
        when(appDateProvider.currentIsoWeek()).thenReturn(24);
        entryRepository.deleteAll();
        noteRepository.deleteAll();
        ratingSnapshotRepository.deleteAll();
    }

    @Test
    @WithAnonymousUser
    void unauthenticatedTodayRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/today"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void authenticatedTodayReturnsOk() throws Exception {
        mockMvc.perform(get("/today"))
                .andExpect(status().isOk());
    }

    @Test
    void postTodayEntriesWithoutCsrfIsRejected() throws Exception {
        TrainingCategory tactics = tactics();

        MvcResult result = mockMvc.perform(post("/today/entries")
                        .accept(MediaType.TEXT_HTML)
                        .param("entries[0].categoryId", tactics.getId().toString())
                        .param("entries[0].trained", "true"))
                .andExpect(status().isForbidden())
                .andReturn();

        String html = result.getResponse().getContentAsString();
        assertThat(html)
                .contains(
                        "Deine Sitzung ist abgelaufen oder die Aktion wurde blockiert.",
                        "Zur Anmeldung",
                        "Zur heutigen Trainingseite"
                )
                .doesNotContain("Whitelabel Error Page");
    }

    @Test
    void errorPageRendersForbiddenWithoutWhitelabel() throws Exception {
        MvcResult result = mockMvc.perform(get("/error")
                        .accept(MediaType.TEXT_HTML)
                        .requestAttr("jakarta.servlet.error.status_code", 403))
                .andExpect(status().isForbidden())
                .andReturn();

        String html = result.getResponse().getContentAsString();
        assertThat(html)
                .contains(
                        "Sitzung abgelaufen - chesstracker2",
                        "Deine Sitzung ist abgelaufen oder die Aktion wurde blockiert.",
                        "href=\"/login\"",
                        "href=\"/today\""
                )
                .doesNotContain("Whitelabel Error Page");
    }

    @Test
    void sessionTimeoutDefaultsToTwelveHours() {
        assertThat(environment.getProperty("server.servlet.session.timeout")).isEqualTo("12h");
    }

    @Test
    void todayFormRendersEntryFieldNamesWithoutFormPrefix() throws Exception {
        MvcResult result = mockMvc.perform(get("/today"))
                .andExpect(status().isOk())
                .andReturn();
        String html = result.getResponse().getContentAsString();
        String weeklyQuote = quoteService.getWeeklyQuote(APP_TODAY).quoteText();

        assertThat(html)
                .contains(
                        "class=\"app-hero\"",
                        "class=\"app-logo\"",
                        "chesstracker2",
                        "Aimchess Training, Rating und Schachentwicklung",
                        "Wochenspruch",
                        weeklyQuote,
                        "name=\"entries[0].categoryId\"",
                        "name=\"entries[0].trained\"",
                        "name=\"entries[0].result\"",
                        "name=\"entries[0].score\"",
                        "name=\"entries[0].durationMinutes\"",
                        "name=\"entries[0].note\"",
                        "name=\"dayNote\"",
                        "Speichern",
                        "Aimchess Training abschlie",
                        "Nach dem Abschluss ist der Tag eingefroren",
                        "href=\"/favicon.svg\"",
                        "id=\"icon-category-target\"",
                        "href=\"#icon-category-target\"",
                        "class=\"training-form\"",
                        "class=\"entry-card\"",
                        "data-training-plan-grid",
                        "data-training-date=\"2026-06-09\"",
                        "data-category-id=\"",
                        "data-original-index=\"0\"",
                        "class=\"plan-training-button\"",
                        "Heute einplanen",
                        "class=\"plan-training-badge\"",
                        "name=\"_csrf\"",
                        "action=\"/logout\"",
                        ">Logout</button>"
                )
                .doesNotContain(
                        "name=\"form.entries[0].categoryId\"",
                        "name=\"form.entries[0].durationMinutes\"",
                        "name=\"form.dayNote\"",
                        "name=\"form.completionStatus\"",
                        "name=\"completionStatus\""
                );
    }

    @Test
    void todayIncludesDateScopedPlanningScriptWithoutChangingFieldNames() throws Exception {
        MvcResult result = mockMvc.perform(get("/today"))
                .andExpect(status().isOk())
                .andReturn();
        String html = result.getResponse().getContentAsString();

        assertThat(html).contains(
                "chesstracker2.trainingPlan.${trainingPlanGrid.dataset.trainingDate}",
                "chesstracker2.trainingDraft.${trainingPlanGrid.dataset.trainingDate}",
                "localStorage.getItem(key)",
                "localStorage.setItem(key, JSON.stringify(plan))",
                "localStorage.setItem(key, JSON.stringify({ fields }))",
                "localStorage.removeItem(key)",
                "initializeTrainingDraft()",
                "trainingPlanGrid.appendChild(card)",
                "querySelectorAll('.entry-card')",
                "card.classList.toggle('is-planned-today', planned)",
                "Geplant #${index + 1}"
        );
        assertThat(html)
                .contains(
                        "name=\"entries[0].categoryId\"",
                        "name=\"entries[0].trained\"",
                        "name=\"entries[0].result\"",
                        "name=\"entries[0].score\"",
                        "name=\"entries[0].durationMinutes\"",
                        "name=\"entries[0].note\"",
                        "name=\"dayNote\""
                )
                .doesNotContain("name=\"form.entries", "name=\"form.dayNote\"");
    }

    @Test
    void todayRendersTacticsChallengeWithPointsField() throws Exception {
        MvcResult result = mockMvc.perform(get("/today"))
                .andExpect(status().isOk())
                .andReturn();
        String html = result.getResponse().getContentAsString();

        assertThat(html).contains(
                "Tactics Challenge",
                "data-points-only=\"true\"",
                "name=\"entries[14].result\"",
                "name=\"entries[14].score\"",
                "<span>Punkte</span>",
                "placeholder=\"15\""
        );
        assertThat(html).doesNotContain("name=\"form.entries", "name=\"form.dayNote\"");
    }

    @Test
    void migrationSeedsMotivationQuotes() {
        assertThat(quoteRepository.findByActiveTrueOrderBySortOrderAscIdAsc())
                .hasSize(10)
                .extracting("author")
                .containsOnly("chesstracker2");
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
    void todayShowsEmptyRatingSummaryWhenNoSnapshotExists() throws Exception {
        MvcResult result = mockMvc.perform(get("/today"))
                .andExpect(status().isOk())
                .andReturn();
        String html = result.getResponse().getContentAsString();

        assertThat(html).contains(
                "Aktuelle Ratings",
                "Noch keine Ratings erfasst.",
                "Rating eintragen",
                "href=\"/rating\""
        );
        assertThat(html).doesNotContain(">null<");
    }

    @Test
    void todayShowsLatestRatingSummaryWithoutNullValues() throws Exception {
        ratingSnapshotRepository.save(snapshot(LocalDate.of(2026, 6, 1), 1700, 1650, 1600, 1400, 1500));
        ratingSnapshotRepository.save(snapshot(LocalDate.of(2026, 6, 8), 1850, 1780, null, 1450, null));
        saveScore(LocalDate.of(2026, 6, 6), tactics(), 2100);
        saveScore(APP_TODAY, tactics(), 2200);
        saveScore(LocalDate.of(2026, 6, 7), endgame(), 1588);

        MvcResult result = mockMvc.perform(get("/today"))
                .andExpect(status().isOk())
                .andReturn();
        String html = result.getResponse().getContentAsString();

        assertThat(html).contains(
                "Aktuelle Ratings",
                "Schachratings",
                "Lichess Blitz",
                "1850",
                "Lichess Rapid",
                "1780",
                "Lichess Classical",
                "1600",
                "DWZ",
                "1450",
                "FIDE Elo",
                "1500",
                "Aimchess Ratings",
                "Tactics",
                "2200",
                "+100 seit gestern",
                "Endgame",
                "1588"
        );
        assertThat(html)
                .doesNotContain(">null<", "Noch keine Ratings erfasst.", "+150", "+130", "+50");
    }

    @Test
    void todayOmitsAimchessRatingGroupWhenNoTrainingScoresExist() throws Exception {
        ratingSnapshotRepository.save(snapshot(APP_TODAY, 1643, 1624, 1760, 1627, 1670));

        MvcResult result = mockMvc.perform(get("/today"))
                .andExpect(status().isOk())
                .andReturn();
        String html = result.getResponse().getContentAsString();

        assertThat(html).contains(
                "Schachratings",
                "Lichess Blitz",
                "1643",
                "FIDE Elo",
                "1670"
        );
        assertThat(html).doesNotContain("Aimchess Ratings", "2215", "1588", ">null<", "Noch keine Ratings erfasst.");
    }

    @Test
    void todayShowsLatestAimchessScoresForTrainingRatings() throws Exception {
        ratingSnapshotRepository.save(snapshot(APP_TODAY, 1643, 1624, 1760, 1627, 1670));
        saveScore(LocalDate.of(2026, 6, 5), tactics(), 2100);
        saveScore(APP_TODAY, tactics(), 2215);
        saveScore(LocalDate.of(2026, 6, 4), endgame(), 1500);
        saveScore(LocalDate.of(2026, 6, 8), endgame(), 1588);

        MvcResult result = mockMvc.perform(get("/today"))
                .andExpect(status().isOk())
                .andReturn();
        String html = result.getResponse().getContentAsString();

        assertThat(html).contains(
                "Lichess Blitz",
                "1643",
                "DWZ",
                "1627",
                "Aimchess Ratings",
                "Tactics",
                "2215",
                "+115 seit gestern",
                "Endgame",
                "1588"
        );
        assertThat(html).doesNotContain("2100", "1500", ">null<", "+88");
    }

    @Test
    void todayShowsManualRatingChangesWithPositiveAndNegativeValues() throws Exception {
        ratingSnapshotRepository.save(snapshot(LocalDate.of(2026, 6, 1), 1840, 1793, null, 1450, null));
        ratingSnapshotRepository.save(snapshot(APP_TODAY, 1850, 1785, null, 1450, null));

        MvcResult result = mockMvc.perform(get("/today"))
                .andExpect(status().isOk())
                .andReturn();
        String html = result.getResponse().getContentAsString();

        assertThat(html).contains(
                "Schachratings",
                "Lichess Blitz",
                "1850",
                "+10 seit gestern",
                "rating-change--positive",
                "Lichess Rapid",
                "1785",
                "-8 seit gestern",
                "rating-change--negative",
                "DWZ",
                "1450",
                "+/-0 seit gestern",
                "rating-change--neutral"
        );
    }

    @Test
    void todayDoesNotShowHistoricalChangeWhenNoValueWasCapturedToday() throws Exception {
        saveScore(LocalDate.of(2026, 6, 14), tactics(), 1945);
        saveScore(LocalDate.of(2026, 6, 15), tactics(), 1958);

        MvcResult result = mockMvc.perform(get("/today"))
                .andExpect(status().isOk())
                .andReturn();
        String html = result.getResponse().getContentAsString();
        String aimchessRatings = sectionContaining(html, "Aimchess Ratings");

        assertThat(aimchessRatings).contains("Tactics", "1958");
        assertThat(aimchessRatings).doesNotContain("+13", "seit gestern", "rating-change--positive");
    }

    @Test
    void todaySkipsNullAimchessScoresWhenCalculatingPreviousChange() throws Exception {
        TrainingCategory tactics = tactics();
        saveScore(LocalDate.of(2026, 6, 6), tactics, 1994);
        saveScore(LocalDate.of(2026, 6, 8), tactics, null);
        saveScore(APP_TODAY, tactics, 1958);

        MvcResult result = mockMvc.perform(get("/today"))
                .andExpect(status().isOk())
                .andReturn();
        String html = result.getResponse().getContentAsString();

        assertThat(html).contains(
                "Aimchess Ratings",
                "Tactics",
                "1958",
                "-36 seit gestern",
                "rating-change--negative"
        );
        assertThat(html).doesNotContain(">1994<");
    }

    @Test
    void todayShowsNewForSingleAimchessScoreAndExcludesTacticsChallenge() throws Exception {
        saveScore(APP_TODAY, defender(), 1922);
        saveScore(APP_TODAY, tacticsChallenge(), 38);

        MvcResult result = mockMvc.perform(get("/today"))
                .andExpect(status().isOk())
                .andReturn();
        String html = result.getResponse().getContentAsString();
        String aimchessRatings = sectionContaining(html, "Aimchess Ratings");

        assertThat(aimchessRatings).contains(
                "Defender",
                "1922",
                "neu",
                "rating-change--new"
        );
        assertThat(aimchessRatings).doesNotContain("Tactics Challenge", "38");
        assertThat(html).contains("Tactics Challenge", "data-points-only=\"true\"");
    }

    @Test
    void weekShowsRatingChangesWithinIsoWeekOnly() throws Exception {
        ratingSnapshotRepository.save(snapshot(LocalDate.of(2026, 6, 14), 1840, null, null, null, null));
        ratingSnapshotRepository.save(snapshot(LocalDate.of(2026, 6, 16), 1855, null, null, null, null));
        saveScore(LocalDate.of(2026, 6, 14), tactics(), 1945);
        saveScore(LocalDate.of(2026, 6, 16), tactics(), 1958);

        MvcResult result = mockMvc.perform(get("/week/2026/25").with(user("norbert")))
                .andExpect(status().isOk())
                .andReturn();
        String html = result.getResponse().getContentAsString();

        assertThat(html).contains(
                "KW 25",
                "Lichess Blitz",
                "1855",
                "+15 diese Woche",
                "Tactics",
                "1958",
                "+13 diese Woche"
        );
    }

    @Test
    void weekDoesNotShowHistoricalChangeWhenWeekHasNoNewValue() throws Exception {
        saveScore(LocalDate.of(2026, 6, 14), tactics(), 1945);
        saveScore(LocalDate.of(2026, 6, 15), tactics(), 1958);

        MvcResult result = mockMvc.perform(get("/week/2026/26").with(user("norbert")))
                .andExpect(status().isOk())
                .andReturn();
        String html = result.getResponse().getContentAsString();
        String aimchessRatings = sectionContaining(html, "Aimchess Ratings");

        assertThat(aimchessRatings).contains("Tactics", "1958");
        assertThat(aimchessRatings).doesNotContain("+13", "diese Woche", "rating-change--positive");
    }

    @Test
    void monthShowsRatingChangesWithinMonthOnly() throws Exception {
        ratingSnapshotRepository.save(snapshot(LocalDate.of(2026, 5, 31), 1800, null, null, null, null));
        ratingSnapshotRepository.save(snapshot(LocalDate.of(2026, 6, 16), 1858, null, null, null, null));
        saveScore(LocalDate.of(2026, 5, 31), tactics(), 1900);
        saveScore(LocalDate.of(2026, 6, 16), tactics(), 1958);

        MvcResult result = mockMvc.perform(get("/month/2026/6").with(user("norbert")))
                .andExpect(status().isOk())
                .andReturn();
        String html = result.getResponse().getContentAsString();

        assertThat(html).contains(
                "6/2026",
                "Lichess Blitz",
                "1858",
                "+58 diesen Monat",
                "Tactics",
                "1958",
                "+58 diesen Monat"
        );
    }

    @Test
    void monthDoesNotShowHistoricalChangeWhenMonthHasNoNewValue() throws Exception {
        saveScore(LocalDate.of(2026, 5, 30), tactics(), 1900);
        saveScore(LocalDate.of(2026, 5, 31), tactics(), 1958);

        MvcResult result = mockMvc.perform(get("/month/2026/6").with(user("norbert")))
                .andExpect(status().isOk())
                .andReturn();
        String html = result.getResponse().getContentAsString();
        String aimchessRatings = sectionContaining(html, "Aimchess Ratings");

        assertThat(aimchessRatings).contains("Tactics", "1958");
        assertThat(aimchessRatings).doesNotContain("+58", "diesen Monat", "rating-change--positive");
    }

    @Test
    void todayMarksWorkedCategoryAndKeepsSortedFieldBinding() throws Exception {
        TrainingCategory tactics = tactics();
        LocalDate today = APP_TODAY;
        DailyTrainingEntry entry = new DailyTrainingEntry();
        entry.setTrainingDate(today);
        entry.setCategory(tactics);
        entry.setTrained(true);
        entry.setSuccessCount(3);
        entry.setTotalCount(5);
        entry.setDurationMinutes(5);
        entryRepository.saveAndFlush(entry);

        MvcResult result = mockMvc.perform(get("/today"))
                .andExpect(status().isOk())
                .andReturn();
        String html = result.getResponse().getContentAsString();

        assertThat(html).contains(
                "is-worked-today",
                "Heute bearbeitet",
                "name=\"entries[0].categoryId\"",
                "name=\"entries[0].trained\"",
                "name=\"entries[0].result\"",
                "name=\"entries[0].score\"",
                "name=\"entries[0].durationMinutes\"",
                "name=\"entries[0].note\""
        );
        assertThat(html).doesNotContain("name=\"form.entries");
        assertThat(html.indexOf("Tactics")).isLessThan(html.indexOf("Advantage Capitalization"));
        assertThat(html.indexOf("name=\"entries[0].categoryId\""))
                .isLessThan(html.indexOf("name=\"entries[1].categoryId\""));
        assertThat(html).contains("name=\"entries[0].categoryId\" value=\"" + tactics.getId() + "\"");
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
    void categoriesShowLatestAimchessRatingWhenPresent() throws Exception {
        TrainingCategory tactics = tactics();
        saveScore(LocalDate.of(2026, 6, 5), tactics, 2100);
        saveScore(APP_TODAY, tactics, 2215);

        MvcResult result = mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andReturn();
        String html = result.getResponse().getContentAsString();
        String tacticsCard = cardContaining(html, "Tactics");

        assertThat(tacticsCard).contains(
                "Aktuelles Rating",
                "2215",
                "Start-Rating",
                "2100",
                "Entwicklung",
                "+115 seit Start",
                "rating-change--positive"
        );
    }

    @Test
    void categoriesShowNegativeRatingProgressSinceStart() throws Exception {
        TrainingCategory endgame = endgame();
        saveScore(LocalDate.of(2026, 6, 5), endgame, 1950);
        saveScore(APP_TODAY, endgame, 1930);

        MvcResult result = mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andReturn();
        String html = result.getResponse().getContentAsString();
        String endgameCard = cardContaining(html, "Endgame");

        assertThat(endgameCard).contains(
                "Aktuelles Rating",
                "1930",
                "Start-Rating",
                "1950",
                "-20 seit Start",
                "rating-change--negative"
        );
    }

    @Test
    void categoriesShowNewForSingleAimchessRating() throws Exception {
        saveScore(APP_TODAY, defender(), 1922);

        MvcResult result = mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andReturn();
        String html = result.getResponse().getContentAsString();
        String defenderCard = cardContaining(html, "Defender");

        assertThat(defenderCard).contains(
                "Aktuelles Rating",
                "1922",
                "Start-Rating",
                "1922",
                "Entwicklung",
                "neu",
                "rating-change--new"
        );
    }

    @Test
    void categoriesWithoutScoreDoNotShowRatingProgress() throws Exception {
        MvcResult result = mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andReturn();
        String html = result.getResponse().getContentAsString();
        String tacticsCard = cardContaining(html, "Tactics");

        assertThat(tacticsCard)
                .contains("Aktuelles Rating", ">-</strong>")
                .doesNotContain("Start-Rating", "Entwicklung", "rating-change--");
    }

    @Test
    void categoriesRenderTacticsChallengeWithChallengeMetrics() throws Exception {
        TrainingCategory challenge = categoryByName("Tactics Challenge");
        saveEntry(LocalDate.of(2026, 6, 5), challenge, 31, 34, 10);
        saveEntry(APP_TODAY, challenge, 38, 41, 10);

        MvcResult result = mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andReturn();
        String html = result.getResponse().getContentAsString();

        assertThat(html).contains(
                "Tactics Challenge",
                "38 geloest",
                "Modus",
                "10 min / 3 Fehler",
                "Challenge-Bestwert",
                "Bester Tag",
                "(38 geloest)",
                "Trend: steigend"
        );
        String challengeCard = cardContaining(html, "Tactics Challenge");
        assertThat(challengeCard).doesNotContain("Start-Rating", "Entwicklung", "rating-change--");
    }

    @Test
    void ratingPageRendersWeeklyMotivationHeader() throws Exception {
        MvcResult result = mockMvc.perform(get("/rating"))
                .andExpect(status().isOk())
                .andReturn();
        String html = result.getResponse().getContentAsString();

        assertThat(html).contains(
                "class=\"app-hero\"",
                "class=\"app-logo\"",
                "Wochenspruch",
                quoteService.getWeeklyQuote(APP_TODAY).quoteText()
        );
    }

    @Test
    void currentWeekRedirectUsesAppDateProvider() throws Exception {
        when(appDateProvider.currentIsoYear()).thenReturn(2027);
        when(appDateProvider.currentIsoWeek()).thenReturn(1);

        mockMvc.perform(get("/week"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/week/2027/1"));
    }

    @Test
    void weekRendersTacticsChallengeAsPointsCategory() throws Exception {
        TrainingCategory tactics = tactics();
        TrainingCategory challenge = tacticsChallenge();
        saveEntry(LocalDate.of(2026, 6, 8), tactics, 3, 8, 12);
        saveEntry(LocalDate.of(2026, 6, 8), challenge, 15, 18, 3);

        MvcResult result = mockMvc.perform(get("/week/2026/24").with(user("norbert")))
                .andExpect(status().isOk())
                .andReturn();
        String html = result.getResponse().getContentAsString();
        String challengeRow = tableRowContaining(html, "Tactics Challenge");

        assertThat(html).contains("Wochenstatistik");
        assertThat(challengeRow)
                .contains("Tactics Challenge", "Punkte-Kategorie", "<td>-</td>")
                .doesNotContain("15/18", "83%");
    }

    @Test
    void currentMonthRedirectUsesAppDateProvider() throws Exception {
        when(appDateProvider.currentMonth()).thenReturn(YearMonth.of(2027, 1));

        mockMvc.perform(get("/month"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/month/2027/1"));
    }

    @Test
    void monthRendersTacticsChallengeAsPointsCategory() throws Exception {
        TrainingCategory tactics = tactics();
        TrainingCategory challenge = tacticsChallenge();
        saveEntry(LocalDate.of(2026, 6, 8), tactics, 3, 8, 12);
        saveEntry(LocalDate.of(2026, 6, 8), challenge, 15, 18, 3);

        MvcResult result = mockMvc.perform(get("/month/2026/6").with(user("norbert")))
                .andExpect(status().isOk())
                .andReturn();
        String html = result.getResponse().getContentAsString();
        String challengeRow = tableRowContaining(html, "Tactics Challenge");

        assertThat(html).contains("Monatsstatistik");
        assertThat(challengeRow)
                .contains("Tactics Challenge", "Punkte-Kategorie", "<td>-</td>")
                .doesNotContain("15/18", "83%");
    }

    @Test
    void postTodayEntriesBindsIndexedEntryFieldsAndUpdatesExistingEntry() throws Exception {
        TrainingCategory tactics = tactics();
        LocalDate today = APP_TODAY;

        mockMvc.perform(post("/today/entries")
                        .with(csrf())
                        .param("entries[0].categoryId", tactics.getId().toString())
                        .param("entries[0].trained", "true")
                        .param("entries[0].result", "3/5")
                        .param("entries[0].score", "1200")
                        .param("entries[0].durationMinutes", "5")
                        .param("entries[0].note", "Test")
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
        assertThat(noteRepository.findByTrainingDate(today).orElseThrow().getCompletedAt()).isNull();

        mockMvc.perform(post("/today/complete")
                        .with(csrf())
                        .param("entries[0].categoryId", tactics.getId().toString())
                        .param("entries[0].trained", "true")
                        .param("entries[0].result", "4/5")
                        .param("entries[0].score", "1300")
                        .param("entries[0].durationMinutes", "10")
                        .param("entries[0].note", "Aktualisiert"))
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
        assertThat(noteRepository.findByTrainingDate(today).orElseThrow().getCompletedAt()).isNotNull();
    }

    @Test
    void postTodayEntriesStoresTacticsChallengePointsWithoutResult() throws Exception {
        TrainingCategory challenge = tacticsChallenge();
        LocalDate today = APP_TODAY;

        mockMvc.perform(post("/today/entries")
                        .with(csrf())
                        .param("entries[0].categoryId", challenge.getId().toString())
                        .param("entries[0].trained", "true")
                        .param("entries[0].score", "15")
                        .param("entries[0].durationMinutes", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/today"));

        DailyTrainingEntry saved = entryRepository.findByTrainingDateAndCategoryId(today, challenge.getId()).orElseThrow();
        assertThat(saved.isTrained()).isTrue();
        assertThat(saved.getSuccessCount()).isZero();
        assertThat(saved.getTotalCount()).isZero();
        assertThat(saved.getScore()).isEqualTo(15);
        assertThat(saved.getDurationMinutes()).isEqualTo(3);

        MvcResult result = mockMvc.perform(get("/today"))
                .andExpect(status().isOk())
                .andReturn();
        String html = result.getResponse().getContentAsString();
        assertThat(html).contains(
                "Tactics Challenge: Punkte: 15, Zeit: 3 min",
                "- Aufgaben: 0/0",
                "- Zeit: 3 min"
        );
        assertThat(html).doesNotContain("Tactics Challenge: 15/18");
    }

    @Test
    void completedTodayIsLockedAndPostEntriesCannotChangeIt() throws Exception {
        TrainingCategory tactics = tactics();
        LocalDate today = APP_TODAY;

        mockMvc.perform(post("/today/complete")
                        .with(csrf())
                        .param("entries[0].categoryId", tactics.getId().toString())
                        .param("entries[0].trained", "true")
                        .param("entries[0].result", "3/5")
                        .param("entries[0].durationMinutes", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/today"));

        MvcResult lockedPage = mockMvc.perform(get("/today"))
                .andExpect(status().isOk())
                .andReturn();
        String html = lockedPage.getResponse().getContentAsString();
        assertThat(html).contains("Tag abgeschlossen", "readonly", "disabled");

        mockMvc.perform(post("/today/entries")
                        .with(csrf())
                        .param("entries[0].categoryId", tactics.getId().toString())
                        .param("entries[0].trained", "true")
                        .param("entries[0].result", "5/5")
                        .param("entries[0].durationMinutes", "20"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/today"));

        assertThat(entryRepository.findByTrainingDateOrderByCategorySortOrderAsc(today)).hasSize(1);
        DailyTrainingEntry saved = entryRepository.findByTrainingDateAndCategoryId(today, tactics.getId()).orElseThrow();
        assertThat(saved.getSuccessCount()).isEqualTo(3);
        assertThat(saved.getTotalCount()).isEqualTo(5);
        assertThat(saved.getDurationMinutes()).isEqualTo(5);
    }

    private TrainingCategory tactics() {
        return categoryByName("Tactics");
    }

    private TrainingCategory endgame() {
        return categoryByName("Endgame");
    }

    private TrainingCategory tacticsChallenge() {
        return categoryByName("Tactics Challenge");
    }

    private TrainingCategory defender() {
        return categoryByName("Defender");
    }

    private TrainingCategory categoryByName(String name) {
        return categoryRepository.findByActiveTrueOrderBySortOrderAscNameAsc().stream()
                .filter(category -> name.equals(category.getName()))
                .findFirst()
                .orElseThrow();
    }

    private void saveEntry(LocalDate date, TrainingCategory category, int success, int total, int durationMinutes) {
        DailyTrainingEntry entry = new DailyTrainingEntry();
        entry.setTrainingDate(date);
        entry.setCategory(category);
        entry.setTrained(true);
        entry.setSuccessCount(success);
        entry.setTotalCount(total);
        entry.setDurationMinutes(durationMinutes);
        entryRepository.saveAndFlush(entry);
    }

    private void saveScore(LocalDate date, TrainingCategory category, Integer score) {
        DailyTrainingEntry entry = new DailyTrainingEntry();
        entry.setTrainingDate(date);
        entry.setCategory(category);
        entry.setScore(score);
        entryRepository.saveAndFlush(entry);
    }

    private String tableRowContaining(String html, String text) {
        int textIndex = html.indexOf(text);
        assertThat(textIndex).isNotNegative();
        int rowStart = html.lastIndexOf("<tr", textIndex);
        int rowEnd = html.indexOf("</tr>", textIndex);
        assertThat(rowStart).isNotNegative();
        assertThat(rowEnd).isNotNegative();
        return html.substring(rowStart, rowEnd + "</tr>".length());
    }

    private String sectionContaining(String html, String text) {
        int textIndex = html.indexOf(text);
        assertThat(textIndex).isNotNegative();
        int sectionStart = html.lastIndexOf("<div class=\"hero-rating-group", textIndex);
        int sectionEnd = html.indexOf("</div>\n                    </div>", textIndex);
        assertThat(sectionStart).isNotNegative();
        assertThat(sectionEnd).isNotNegative();
        return html.substring(sectionStart, sectionEnd);
    }

    private String cardContaining(String html, String text) {
        int gridStart = html.indexOf("<section class=\"entry-grid\">");
        assertThat(gridStart).isNotNegative();
        int textIndex = html.indexOf(text, gridStart);
        assertThat(textIndex).isNotNegative();
        int cardStart = html.lastIndexOf("<article class=\"entry-card\"", textIndex);
        int cardEnd = html.indexOf("</article>", textIndex);
        assertThat(cardStart).isNotNegative();
        assertThat(cardEnd).isNotNegative();
        return html.substring(cardStart, cardEnd + "</article>".length());
    }

    private RatingSnapshot snapshot(
            LocalDate date,
            Integer blitz,
            Integer rapid,
            Integer classical,
            Integer dwz,
            Integer fide
    ) {
        RatingSnapshot snapshot = new RatingSnapshot();
        snapshot.setSnapshotDate(date);
        snapshot.setLichessBlitz(blitz);
        snapshot.setLichessRapid(rapid);
        snapshot.setLichessClassical(classical);
        snapshot.setDwz(dwz);
        snapshot.setFideElo(fide);
        return snapshot;
    }
}
