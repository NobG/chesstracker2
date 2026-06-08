package com.nobg.chesstracker2.controller;

import com.nobg.chesstracker2.dto.RatingSnapshotForm;
import com.nobg.chesstracker2.service.RatingSnapshotService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RatingController {

    private final RatingSnapshotService ratingSnapshotService;

    public RatingController(RatingSnapshotService ratingSnapshotService) {
        this.ratingSnapshotService = ratingSnapshotService;
    }

    @GetMapping("/rating")
    public String rating(Model model) {
        var view = ratingSnapshotService.ratingView();
        model.addAttribute("pageTitle", "Rating");
        model.addAttribute("view", view);
        model.addAttribute("form", view.form());
        return "rating";
    }

    @PostMapping("/rating")
    public String saveRating(@ModelAttribute("form") RatingSnapshotForm form, RedirectAttributes redirectAttributes) {
        try {
            ratingSnapshotService.saveSnapshot(form);
            redirectAttributes.addFlashAttribute("successMessage", "Rating-Snapshot gespeichert.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/rating";
    }
}
