package com.nobg.chesstracker2.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nobg.chesstracker2.model.RatingSnapshot;
import com.nobg.chesstracker2.repository.RatingSnapshotRepository;
import com.nobg.chesstracker2.service.AppDateProvider;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RatingControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RatingSnapshotRepository repository;

    @MockBean
    private AppDateProvider appDateProvider;

    @BeforeEach
    void cleanSnapshots() {
        repository.deleteAll();
        when(appDateProvider.today()).thenReturn(LocalDate.of(2026, 6, 9));
    }

    @Test
    void getRatingRendersFormAndNavigation() throws Exception {
        MvcResult result = mockMvc.perform(get("/rating"))
                .andExpect(status().isOk())
                .andReturn();
        String html = result.getResponse().getContentAsString();

        assertThat(html).contains(
                "href=\"/rating\"",
                "Rating",
                "Die letzten bekannten Ratings sind vorausgefuellt",
                "name=\"snapshotDate\"",
                "name=\"lichessBlitz\"",
                "name=\"lichessRapid\"",
                "name=\"lichessClassical\"",
                "name=\"dwz\"",
                "name=\"fideElo\"",
                "name=\"note\""
        );
        assertInputValue(html, "snapshotDate", "2026-06-09");
        assertThat(html).doesNotContain("name=\"tacticsRating\"", "name=\"endgameRating\"");
    }

    @Test
    void getRatingPrefillsValuesFromLatestSnapshot() throws Exception {
        repository.save(snapshot(LocalDate.of(2026, 6, 1), 1643, 1624, 1760, 1627, 1670));

        MvcResult result = mockMvc.perform(get("/rating"))
                .andExpect(status().isOk())
                .andReturn();
        String html = result.getResponse().getContentAsString();

        assertInputValue(html, "snapshotDate", "2026-06-09");
        assertInputValue(html, "lichessBlitz", "1643");
        assertInputValue(html, "lichessRapid", "1624");
        assertInputValue(html, "lichessClassical", "1760");
        assertInputValue(html, "dwz", "1627");
        assertInputValue(html, "fideElo", "1670");
        assertThat(html).doesNotContain("Old note", "name=\"tacticsRating\"", "name=\"endgameRating\"");
    }

    @Test
    void getRatingPrefillsValuesFromTodaySnapshotWhenPresent() throws Exception {
        repository.save(snapshot(LocalDate.of(2026, 6, 1), 1643, 1624, 1760, 1627, 1670));
        RatingSnapshot today = snapshot(LocalDate.of(2026, 6, 9), 1650, 1625, 1761, 1628, 1671);
        today.setNote("Heute korrigieren");
        repository.save(today);

        MvcResult result = mockMvc.perform(get("/rating"))
                .andExpect(status().isOk())
                .andReturn();
        String html = result.getResponse().getContentAsString();

        assertInputValue(html, "snapshotDate", "2026-06-09");
        assertInputValue(html, "lichessBlitz", "1650");
        assertInputValue(html, "lichessRapid", "1625");
        assertInputValue(html, "lichessClassical", "1761");
        assertInputValue(html, "dwz", "1628");
        assertInputValue(html, "fideElo", "1671");
        assertThat(html).contains(">Heute korrigieren</textarea>");
    }

    @Test
    void postRatingCreatesAndUpdatesSnapshotForSameDate() throws Exception {
        LocalDate date = LocalDate.of(2026, 6, 8);

        mockMvc.perform(post("/rating")
                        .param("snapshotDate", date.toString())
                        .param("lichessBlitz", "1800")
                        .param("lichessRapid", "")
                        .param("lichessClassical", "")
                        .param("dwz", "1700")
                        .param("fideElo", "")
                        .param("note", "Start"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rating"));

        RatingSnapshot saved = repository.findBySnapshotDate(date).orElseThrow();
        assertThat(saved.getLichessBlitz()).isEqualTo(1800);
        assertThat(saved.getLichessRapid()).isNull();
        assertThat(saved.getDwz()).isEqualTo(1700);
        assertThat(saved.getNote()).isEqualTo("Start");

        mockMvc.perform(post("/rating")
                        .param("snapshotDate", date.toString())
                        .param("lichessBlitz", "1825")
                        .param("dwz", "1718")
                        .param("note", "Update"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rating"));

        assertThat(repository.findAll()).hasSize(1);
        RatingSnapshot updated = repository.findBySnapshotDate(date).orElseThrow();
        assertThat(updated.getId()).isEqualTo(saved.getId());
        assertThat(updated.getLichessBlitz()).isEqualTo(1825);
        assertThat(updated.getDwz()).isEqualTo(1718);
        assertThat(updated.getNote()).isEqualTo("Update");
    }

    @Test
    void postRatingStoresSubmittedPrefilledValuesWhenOnlyOneRatingChanged() throws Exception {
        LocalDate date = LocalDate.of(2026, 6, 9);
        repository.save(snapshot(LocalDate.of(2026, 6, 1), 1643, 1624, 1760, 1627, 1670));

        mockMvc.perform(post("/rating")
                        .param("snapshotDate", date.toString())
                        .param("lichessBlitz", "1650")
                        .param("lichessRapid", "1624")
                        .param("lichessClassical", "1760")
                        .param("dwz", "1627")
                        .param("fideElo", "1670")
                        .param("note", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rating"));

        RatingSnapshot saved = repository.findBySnapshotDate(date).orElseThrow();
        assertThat(saved.getLichessBlitz()).isEqualTo(1650);
        assertThat(saved.getLichessRapid()).isEqualTo(1624);
        assertThat(saved.getLichessClassical()).isEqualTo(1760);
        assertThat(saved.getDwz()).isEqualTo(1627);
        assertThat(saved.getFideElo()).isEqualTo(1670);
        assertThat(saved.getNote()).isNull();
    }

    @Test
    void ratingShowsNewestSnapshotsFirstAndChanges() throws Exception {
        repository.save(snapshot(LocalDate.of(2026, 6, 1), 1800, 1900, null, 1700, null));
        repository.save(snapshot(LocalDate.of(2026, 6, 8), 1825, 1890, null, 1718, null));

        MvcResult result = mockMvc.perform(get("/rating"))
                .andExpect(status().isOk())
                .andReturn();
        String html = result.getResponse().getContentAsString();

        assertThat(html.indexOf("2026-06-08")).isLessThan(html.indexOf("2026-06-01"));
        assertThat(html).contains("Lichess Blitz", "+25", "Lichess Rapid", "-10", "DWZ", "+18");
        assertThat(html).doesNotContain("Taktik", "Endspiel");
    }

    @Test
    void postRatingRejectsNegativeRating() throws Exception {
        mockMvc.perform(post("/rating")
                        .param("snapshotDate", "2026-06-08")
                        .param("lichessBlitz", "-1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rating"));

        assertThat(repository.findAll()).isEmpty();
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

    private void assertInputValue(String html, String name, String value) {
        assertThat(html).containsPattern("name=\"" + name + "\"[^>]*value=\"" + value + "\"");
    }
}
