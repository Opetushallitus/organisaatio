package fi.vm.sade.organisaatio.config.scheduling;

import com.github.kagkarlsson.scheduler.task.ExecutionComplete;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WeekdaysTest {

    @Test
    public void timeDoneMonday() {
        Weekdays weekdays = new Weekdays(LocalTime.of(5, 11, 45), ZoneId.of("UTC"));
        ExecutionComplete executionCompleteMock = mock(ExecutionComplete.class);
        when(executionCompleteMock.getTimeDone()).thenReturn(Instant.parse("2018-10-22T10:15:30.000Z"));

        Instant nextExecutionTime = weekdays.getNextExecutionTime(executionCompleteMock);

        assertThat(nextExecutionTime).isEqualTo("2018-10-23T05:11:45Z");
    }

    @Test
    public void timeDoneFriday() {
        Weekdays weekdays = new Weekdays(LocalTime.of(5, 11, 45), ZoneId.of("UTC"));
        ExecutionComplete executionCompleteMock = mock(ExecutionComplete.class);
        when(executionCompleteMock.getTimeDone()).thenReturn(Instant.parse("2018-10-26T10:15:30.000Z"));

        Instant nextExecutionTime = weekdays.getNextExecutionTime(executionCompleteMock);

        assertThat(nextExecutionTime).isEqualTo("2018-10-29T05:11:45Z");
    }

    @Test
    public void timeDoneSaturday() {
        Weekdays weekdays = new Weekdays(LocalTime.of(5, 11, 45), ZoneId.of("UTC"));
        ExecutionComplete executionCompleteMock = mock(ExecutionComplete.class);
        when(executionCompleteMock.getTimeDone()).thenReturn(Instant.parse("2018-10-27T10:15:30.000Z"));

        Instant nextExecutionTime = weekdays.getNextExecutionTime(executionCompleteMock);

        assertThat(nextExecutionTime).isEqualTo("2018-10-29T05:11:45Z");
    }

}
