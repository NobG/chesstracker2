package com.nobg.chesstracker2.viewmodel;

import com.nobg.chesstracker2.dto.RatingSnapshotForm;
import java.util.List;

public record RatingSnapshotViewModel(
        RatingSnapshotForm form,
        List<RatingSnapshotRowViewModel> snapshots,
        List<RatingChangeViewModel> changes
) {
}
