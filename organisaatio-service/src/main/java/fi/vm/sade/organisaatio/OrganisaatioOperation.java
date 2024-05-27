package fi.vm.sade.organisaatio;

import fi.vm.sade.auditlog.Operation;

public enum OrganisaatioOperation implements Operation {

    ORG_DELETE,
    ORG_CREATE,
    ORG_NIMI_CREATE,
    ORG_UPDATE_MANY,
    ORG_SUHDE_UPDATE,
    ORG_NIMI_UPDATE,
    ORG_NIMI_DELETE,
    ORG_UPDATE,
    ORG_TARKASTA,
    ORG_REKISTEROINTI

}
