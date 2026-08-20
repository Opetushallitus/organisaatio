package fi.vm.sade.auditlog;

import com.google.gson.JsonObject;
import org.ietf.jgss.Oid;

import java.net.InetAddress;

public class User {
    private final Oid oid;
    private final InetAddress ip;
    private final String session;
    private final String userAgent;

    public User(InetAddress ip, String session, String userAgent) {
        this(null, ip, session, userAgent);
    }
    
    public User(Oid oid, InetAddress ip, String session, String userAgent) {
        this.oid = oid;
        this.ip = ip;
        this.session = session;
        this.userAgent = userAgent;
    }

    public JsonObject asJson() {
        JsonObject o = new JsonObject();
        if (oid != null) {
            o.addProperty("oid", oid.toString());
        }
        o.addProperty("ip", ip.getHostAddress());
        o.addProperty("session", session);
        o.addProperty("userAgent", userAgent);
        return o;
    }
}
