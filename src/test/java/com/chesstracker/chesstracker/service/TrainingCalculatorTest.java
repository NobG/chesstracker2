package com.chesstracker.chesstracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class TrainingCalculatorTest {

    @Test
    void calculatesSuccessRate() {
        assertThat(TrainingCalculator.successRate(7, 10)).isEqualTo(70);
        assertThat(TrainingCalculator.successRate(0, 0)).isNull();
    }

    @Test
    void parsesResultFormat() {
        TrainingResult result = TrainingCalculator.parseResult(" 12 / 20 ");

        assertThat(result.successCount()).isEqualTo(12);
        assertThat(result.totalCount()).isEqualTo(20);
    }

    @Test
    void rejectsInvalidResult() {
        assertThatThrownBy(() -> TrainingCalculator.parseResult("8 of 10"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> TrainingCalculator.parseResult("11/10"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
