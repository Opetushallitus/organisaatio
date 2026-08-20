package fi.vm.sade.auditlog;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class HeartbeatThreadFactory implements ThreadFactory {
    private static HeartbeatThreadFactory SINGLETON = new HeartbeatThreadFactory();
    private static final ThreadGroup HB_THREAD_GROUP= new ThreadGroup("AUDIT-THREAD-GROUP");
    private static final String DAEMON_NAME = "HEARTBEAT-%s";
    private AtomicInteger COUNT = new AtomicInteger(0);
    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(HB_THREAD_GROUP, r, String.format(DAEMON_NAME, COUNT.getAndIncrement()));
        t.setDaemon(true);
        t.setPriority(Thread.NORM_PRIORITY);
        return t;
    }
    private HeartbeatThreadFactory() {
    }


    public static HeartbeatThreadFactory getInstance() {
        return SINGLETON;
    }
}
