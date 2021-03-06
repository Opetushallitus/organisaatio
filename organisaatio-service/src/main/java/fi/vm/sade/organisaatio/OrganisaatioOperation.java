package fi.vm.sade.organisaatio;

import fi.vm.sade.auditlog.Operation;

public enum OrganisaatioOperation implements Operation {

    ORG_DELETE,
    ORG_CREATE,
    YHTEYSTIETO_UPDATE,
    YHTEYSTIETO_CREATE,
    YHTEYSTIETO_DELETE,
    ORG_NIMI_CREATE,
    ORG_UPDATE_MANY,
    ORG_SUHDE_UPDATE,
    ORG_NIMI_UPDATE,
    ORG_NIMI_DELETE,
    IMG_CREATE,
    IMG_DELETE,
    ORG_UPDATE,

}
