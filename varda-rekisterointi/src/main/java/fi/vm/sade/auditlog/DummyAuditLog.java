package fi.vm.sade.auditlog;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Dummy logger for tests. Do not use in production.
 */
public class DummyAuditLog extends Audit {
    private final String serviceName;
    private final ApplicationType applicationType;

    public DummyAuditLog(Logger logger, String serviceName, ApplicationType applicationType) {
        super(logger, serviceName, applicationType);

        this.serviceName = serviceName;
        this.applicationType = applicationType;
        System.out.printf("%s: created. serviceName = %s, applicationType = %s, ignoring logger = %s%n",
            getClass().getName(), serviceName, applicationType, logger);
    }

    public DummyAuditLog() {
        this(null, "testing", ApplicationType.BACKEND);
    }

    @Override
    public void logStarted() {
        System.out.printf("%s: started. serviceName = %s, applicationType = %s%n",
            getClass().getName(), serviceName, applicationType);
    }

    @Override
    public void logHeartbeat() {
    }

    @Override
    public void logStopped() {
        System.out.printf("%s: stopped. serviceName = %s, applicationType = %s%n",
            getClass().getName(), serviceName, applicationType);
    }

    @Override
    public void log(User user, Operation operation, Target target, Changes changes) {
        System.out.printf("%s: got message with user = '%s', operation = '%s', target = '%s', changes = '%s'%n",
            getClass().getName(), toString(user), toString(operation), toString(target), toString(changes));
    }

    private Object toString(Object o) {
        return ToStringBuilder.reflectionToString(o);
    }
}
