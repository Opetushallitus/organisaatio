package fi.vm.sade.varda.rekisterointi.util;

import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;

public final class AuditUtils {

    private AuditUtils() {
    }

    public static Optional<Oid> createOid(String str) {
        try {
            return Optional.of(new Oid(str));
        } catch (GSSException e) {
            return Optional.empty();
        }
    }

    public static InetAddress createInetAddress(String str) {
        try {
            return InetAddress.getByName(str);
        } catch (UnknownHostException e) {
            return createInetAddress();
        }
    }

    private static InetAddress createInetAddress() {
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            return InetAddress.getLoopbackAddress();
        }
    }

}
