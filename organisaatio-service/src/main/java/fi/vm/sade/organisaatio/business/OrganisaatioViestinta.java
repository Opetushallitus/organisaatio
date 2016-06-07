package fi.vm.sade.organisaatio.business;

import java.util.List;

public interface OrganisaatioViestinta {
    void sendEmail(String message);

    void sendEmail(String message, List<String> receivers);
}
