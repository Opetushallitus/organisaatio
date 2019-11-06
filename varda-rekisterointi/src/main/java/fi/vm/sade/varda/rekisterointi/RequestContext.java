package fi.vm.sade.varda.rekisterointi;

import java.util.Optional;

public interface RequestContext {

    Optional<String> getUsername();

    String getIp();

    Optional<String> getSession();

    Optional<String> getUserAgent();

}
