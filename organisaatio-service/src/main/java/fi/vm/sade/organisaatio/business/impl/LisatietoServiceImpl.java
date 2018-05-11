package fi.vm.sade.organisaatio.business.impl;

import fi.vm.sade.organisaatio.business.LisatietoService;
import fi.vm.sade.organisaatio.business.OrganisaatioFindBusinessService;
import fi.vm.sade.organisaatio.dao.LisatietoTyyppiDao;
import fi.vm.sade.organisaatio.dto.v3.OrganisaatioRDTOV3;
import fi.vm.sade.organisaatio.model.Lisatietotyyppi;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LisatietoServiceImpl implements LisatietoService {
    private LisatietoTyyppiDao lisatietoTyyppiDao;

    private OrganisaatioFindBusinessService organisaatioFindBusinessService;

    public LisatietoServiceImpl(LisatietoTyyppiDao lisatietoTyyppiDao,
                                OrganisaatioFindBusinessService organisaatioFindBusinessService) {
        this.lisatietoTyyppiDao = lisatietoTyyppiDao;
        this.organisaatioFindBusinessService = organisaatioFindBusinessService;
    }

    @Override
    public Set<String> getLisatietotyypit() {
        return this.lisatietoTyyppiDao.findAll().stream()
                .map(Lisatietotyyppi::getNimi)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getSallitutByOid(String oid) {
        List<OrganisaatioRDTOV3> organisaatioRDTOV3s = organisaatioFindBusinessService.findByOids(Collections.singletonList(oid));
        if (organisaatioRDTOV3s.isEmpty()) {
            return new HashSet<>();
        }
        OrganisaatioRDTOV3 organisaatio = organisaatioRDTOV3s.get(0);
        return this.lisatietoTyyppiDao.findValidByOrganisaatiotyyppiAndOppilaitostyyppi(organisaatio.getTyypit(), organisaatio.getOppilaitosTyyppiUri());
    }
}
