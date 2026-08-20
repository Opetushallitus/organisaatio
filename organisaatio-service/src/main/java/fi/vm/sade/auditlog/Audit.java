package fi.vm.sade.auditlog;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

public class Audit {
    private static final int VERSION = 1;
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
    public static final String TYPE_ALIVE = "alive";
    private static final String TYPE_LOG = "log";

    static {
        SDF.setTimeZone(TimeZone.getTimeZone("Europe/Helsinki"));
    }

    private final Clock clock;
    private final Date bootTime;
    private final String hostname;
    private final HeartbeatDaemon heartbeat;
    private final AtomicInteger logSeq;
    private final Logger logger;
    private final String serviceName;
    private final String applicationType;
    private final Gson gson = new GsonBuilder().serializeNulls().create();

    /**
     * Create an Audit logger for service.
     *
     * @param serviceName name of the service e.g. omatsivut
     * @param applicationType type of the service application e.g. OPISKELIJA
     */
    public Audit(Logger logger, String serviceName, ApplicationType applicationType) {
        this(
                logger,
                serviceName,
                applicationType,
                System.getProperty("HOSTNAME", ""),
                HeartbeatDaemon.getInstance(),
                new Clock() {
                    @Override
                    public Date wallClockTime() {
                        return new Date();
                    }
                }
        );
    }

    public Audit(Logger logger, String serviceName, ApplicationType applicationType, String hostname, HeartbeatDaemon heartbeat, Clock clock) {
        this.clock = clock;
        this.bootTime = clock.wallClockTime();
        this.hostname = hostname;
        this.logger = logger;
        this.serviceName = serviceName;
        this.applicationType = applicationType.toString().toLowerCase();
        this.heartbeat = heartbeat;
        this.logSeq = new AtomicInteger(0);
        heartbeat.register(this);
    }

    private JsonObject commonFields(String type) {
        JsonObject json = new JsonObject();

        json.addProperty("version", VERSION);
        json.addProperty("logSeq", logSeq.getAndIncrement());
        json.addProperty("type", type);

        synchronized (SDF) {
            json.addProperty("bootTime", SDF.format(this.bootTime));
            json.addProperty("hostname", this.hostname);
            json.addProperty("timestamp", SDF.format(clock.wallClockTime()));
        }

        json.addProperty("serviceName", serviceName);
        json.addProperty("applicationType", applicationType);

        return json;
    }

    public void logStarted() {
        JsonObject json = commonFields(TYPE_ALIVE);
        json.addProperty("message", "started");
        logger.log(gson.toJson(json));
    }

    public void logHeartbeat() {
        JsonObject json = commonFields(TYPE_ALIVE);
        json.addProperty("message", "alive");
        logger.log(gson.toJson(json));
    }

    public void logStopped() {
        JsonObject json = commonFields(TYPE_ALIVE);
        json.addProperty("message", "stopped");
        logger.log(gson.toJson(json));
    }

    public void log(User user, Operation operation, Target target, JsonArray changes) {
        JsonObject json = commonFields(TYPE_LOG);
        json.add("user", user.asJson());
        try {
            json.addProperty("operation", operation.name());
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("Operation is required");
        }
        json.add("target", target.asJson());
        json.add("changes", changes);
        logger.log(gson.toJson(json));
    }

    public void log(User user,
                    Operation operation,
                    Target target,
                    Changes changes) {
        this.log(user, operation, target, changes.asJsonArray());
    }
}
