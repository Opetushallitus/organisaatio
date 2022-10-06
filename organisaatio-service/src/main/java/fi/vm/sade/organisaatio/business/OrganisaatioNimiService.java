package fi.vm.sade.organisaatio.business;

import fi.vm.sade.organisaatio.dto.OrganisaatioNimiDTO;

import java.util.List;

public interface OrganisaatioNimiService {
    List<OrganisaatioNimiDTO> getNimet(String oid);
}
