package fi.vm.sade.organisaatio.business;

import fi.vm.sade.organisaatio.resource.dto.HakutoimistoDTO;

public interface HakutoimistoService {
    HakutoimistoDTO hakutoimisto(String organisaatioOId);
}
