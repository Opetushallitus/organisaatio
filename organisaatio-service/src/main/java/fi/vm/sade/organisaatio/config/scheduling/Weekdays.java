package fi.vm.sade.organisaatio.config.scheduling;

import com.github.kagkarlsson.scheduler.task.ExecutionComplete;
import com.github.kagkarlsson.scheduler.task.schedule.Schedule;

import java.time.*;
import java.util.Set;
import java.util.stream.Stream;

import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

public class Weekdays implements Schedule {

    private static final Set<DayOfWeek> WEEKENDS = Stream.of(SATURDAY, SUNDAY).collect(toSet());

    private final LocalTime time;
    private final ZoneId zone;

    public Weekdays(LocalTime time) {
        this(time, ZoneId.systemDefault());
    }

    public Weekdays(LocalTime time, ZoneId zone) {
        this.time = requireNonNull(time);
        this.zone = requireNonNull(zone);
    }

    @Override
    public Instant getNextExecutionTime(ExecutionComplete executionComplete) {
        LocalDate nextDate = executionComplete.getTimeDone().atZone(zone).toLocalDate();
        do {
            nextDate = nextDate.plusDays(1);
        } while (isWeekend(nextDate.getDayOfWeek()));
        return ZonedDateTime.of(nextDate, time, zone).toInstant();
    }

    @Override
    public boolean isDeterministic() {
        return true;
    }

    private static boolean isWeekend(DayOfWeek dayOfWeek) {
        return WEEKENDS.contains(dayOfWeek);
    }

}
