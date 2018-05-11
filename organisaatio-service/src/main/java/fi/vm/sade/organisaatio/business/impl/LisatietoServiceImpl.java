package fi.vm.sade.organisaatio.business.impl;

import fi.vm.sade.organisaatio.business.LisatietoService;
import fi.vm.sade.organisaatio.business.OrganisaatioFindBusinessService;
import fi.vm.sade.organisaatio.dao.LisatietoTyyppiDao;
import fi.vm.sade.organisaatio.model.Lisatietotyyppi;
import org.springframework.stereotype.Service;

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
        return this.lisatietoTyyppiDao.findValidByOrganisaatiotyyppiAndOppilaitostyyppi(oid);
    }
}
