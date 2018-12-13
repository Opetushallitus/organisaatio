package fi.vm.sade.organisaatio.business;

import fi.vm.sade.organisaatio.model.Organisaatio;

public interface OrganisaatioValidationService {
    void validateOrganisation(Organisaatio model, String parentOid, Organisaatio parentOrg);
}
