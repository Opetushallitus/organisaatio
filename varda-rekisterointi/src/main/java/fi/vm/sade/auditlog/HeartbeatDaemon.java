package fi.vm.sade.auditlog;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.MINUTES;

public class HeartbeatDaemon implements Runnable {
    private static HeartbeatDaemon instance = null;
    private static final long MINUTES_BETWEEN_HEARTBEATS = 10;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(HeartbeatThreadFactory.getInstance());
    private final CopyOnWriteArrayList<Audit> loggers = new CopyOnWriteArrayList<>();

    private HeartbeatDaemon() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                shutdown();
            }
        }));
        scheduler.scheduleAtFixedRate(this, MINUTES_BETWEEN_HEARTBEATS, MINUTES_BETWEEN_HEARTBEATS, MINUTES);
    }

    public void shutdown() {
        scheduler.shutdown();
        try {
            scheduler.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
        }
        for (Audit logger : loggers) {
            logger.logStopped();
        }
    }

    public static synchronized HeartbeatDaemon getInstance() {
        if (instance == null) {
            instance = new HeartbeatDaemon();
        }
        return instance;
    }

    public void register(Audit audit) {
        this.loggers.add(audit);
        audit.logStarted();
    }

    @Override
    public void run() {
        for (Audit logger : this.loggers) {
            logger.logHeartbeat();
        }
    }
}
