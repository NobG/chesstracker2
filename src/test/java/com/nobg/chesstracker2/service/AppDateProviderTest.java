package com.nobg.chesstracker2.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class AppDateProviderTest {

    @Test
    void resolvesTodayInConfiguredBerlinTimezone() {
        Clock utcClock = Clock.fixed(Instant.parse("2026-06-08T22:44:00Z"), ZoneOffset.UTC);
        AppDateProvider provider = new AppDateProvider("Europe/Berlin", utcClock);

        assertThat(provider.today()).isEqualTo(LocalDate.of(2026, 6, 9));
        assertThat(provider.currentMonth()).isEqualTo(YearMonth.of(2026, 6));
        assertThat(provider.currentIsoYear()).isEqualTo(2026);
        assertThat(provider.currentIsoWeek()).isEqualTo(24);
        assertThat(provider.zoneId().getId()).isEqualTo("Europe/Berlin");
    }
}
