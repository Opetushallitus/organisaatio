package fi.vm.sade.organisaatio.business.impl;

import fi.vm.sade.organisaatio.business.HakutoimistoService;
import fi.vm.sade.organisaatio.business.exception.HakutoimistoNotFoundException;
import fi.vm.sade.organisaatio.resource.dto.HakutoimistoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HakutoimistoServiceImpl implements HakutoimistoService {
    @Override
    public HakutoimistoDTO hakutoimisto(String organisaatioOId) {
        throw new HakutoimistoNotFoundException(organisaatioOId);
    }
}
