package fi.vm.sade.organisaatio.client;

import fi.vm.sade.organisaatio.business.exception.OrganisaatioKoodistoException;

public interface OrganisaatioKoodistoClient {
    String get(String uri) throws OrganisaatioKoodistoException;
    void put(String uri) throws OrganisaatioKoodistoException;
    void post(String json, String uri) throws OrganisaatioKoodistoException;
}
