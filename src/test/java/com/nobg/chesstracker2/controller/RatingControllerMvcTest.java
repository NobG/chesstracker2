package com.nobg.chesstracker2.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nobg.chesstracker2.model.RatingSnapshot;
import com.nobg.chesstracker2.repository.RatingSnapshotRepository;
import java.time.LocalDate;
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
class RatingControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RatingSnapshotRepository repository;

    @BeforeEach
    void cleanSnapshots() {
        repository.deleteAll();
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
                "name=\"snapshotDate\"",
                "name=\"lichessBlitz\"",
                "name=\"lichessRapid\"",
                "name=\"lichessClassical\"",
                "name=\"dwz\"",
                "name=\"fideElo\"",
                "name=\"note\""
        );
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
    void ratingShowsNewestSnapshotsFirstAndChanges() throws Exception {
        repository.save(snapshot(LocalDate.of(2026, 6, 1), 1800, 1900, null, 1700));
        repository.save(snapshot(LocalDate.of(2026, 6, 8), 1825, 1890, null, 1718));

        MvcResult result = mockMvc.perform(get("/rating"))
                .andExpect(status().isOk())
                .andReturn();
        String html = result.getResponse().getContentAsString();

        assertThat(html.indexOf("2026-06-08")).isLessThan(html.indexOf("2026-06-01"));
        assertThat(html).contains("Lichess Blitz", "+25", "Lichess Rapid", "-10", "DWZ", "+18");
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

    private RatingSnapshot snapshot(LocalDate date, Integer blitz, Integer rapid, Integer classical, Integer dwz) {
        RatingSnapshot snapshot = new RatingSnapshot();
        snapshot.setSnapshotDate(date);
        snapshot.setLichessBlitz(blitz);
        snapshot.setLichessRapid(rapid);
        snapshot.setLichessClassical(classical);
        snapshot.setDwz(dwz);
        return snapshot;
    }
}
