package fi.vm.sade.organisaatio.business.impl;

import fi.vm.sade.organisaatio.business.LisatietoService;
import fi.vm.sade.organisaatio.dao.LisatietoTyyppiDao;
import fi.vm.sade.organisaatio.model.Lisatietotyyppi;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LisatietoServiceImpl implements LisatietoService {
    private LisatietoTyyppiDao lisatietoTyyppiDao;

    public LisatietoServiceImpl(LisatietoTyyppiDao lisatietoTyyppiDao) {
        this.lisatietoTyyppiDao = lisatietoTyyppiDao;
    }

    @Override
    public Set<String> getLisatietotyypit() {
        return this.lisatietoTyyppiDao.findAll().stream()
                .map(Lisatietotyyppi::getNimi)
                .collect(Collectors.toSet());
    }
}
