package com.nobg.chesstracker2.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.IsoFields;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppDateProvider {

    private final ZoneId zoneId;
    private final Clock clock;

    @Autowired
    public AppDateProvider(@Value("${chesstracker2.time-zone:Europe/Berlin}") String timeZone) {
        this(timeZone, Clock.system(ZoneId.of(timeZone)));
    }

    AppDateProvider(String timeZone, Clock clock) {
        this.zoneId = ZoneId.of(timeZone);
        this.clock = clock.withZone(zoneId);
    }

    public LocalDate today() {
        return LocalDate.now(clock);
    }

    public YearMonth currentMonth() {
        return YearMonth.now(clock);
    }

    public int currentIsoWeek() {
        return today().get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
    }

    public int currentIsoYear() {
        return today().get(IsoFields.WEEK_BASED_YEAR);
    }

    public ZoneId zoneId() {
        return zoneId;
    }
}
