package fi.vm.sade.organisaatio.service.util;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Optional;

public final class DateUtil {

    private DateUtil() {
    }

    public static Timestamp toTimestamp(Date date) {
        return Optional.ofNullable(date).map(Date::getTime).map(Timestamp::new).orElse(null);
    }

}
