package fi.vm.sade.organisaatio.business.impl;

import fi.vm.sade.organisaatio.business.LisatietoService;
import fi.vm.sade.organisaatio.business.OrganisaatioFindBusinessService;
import fi.vm.sade.organisaatio.dao.LisatietoTyyppiDao;
import fi.vm.sade.organisaatio.dto.LisatietotyyppiCreateDto;
import fi.vm.sade.organisaatio.model.Lisatietotyyppi;
import fi.vm.sade.organisaatio.model.OppilaitostyyppiRajoite;
import fi.vm.sade.organisaatio.model.OrganisaatiotyyppiRajoite;
import fi.vm.sade.organisaatio.model.Rajoite;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.NotFoundException;
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
    @Transactional(readOnly = true)
    public Set<String> getLisatietotyypit() {
        return this.lisatietoTyyppiDao.findAll().stream()
                .map(Lisatietotyyppi::getNimi)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional(readOnly = true)
    public Set<String> getSallitutByOid(String oid) {
        return this.lisatietoTyyppiDao.findValidByOrganisaatiotyyppiAndOppilaitostyyppi(oid);
    }

    @Override
    @Transactional
    public String create(LisatietotyyppiCreateDto lisatietotyyppiCreateDto) {
        this.lisatietoTyyppiDao.findByNimi(lisatietotyyppiCreateDto.getNimi())
                .ifPresent((value) -> {throw new IllegalArgumentException(String.format("Lisatietotyyppi with nimi %s already exists", lisatietotyyppiCreateDto.getNimi()));});
        Set<Rajoite> rajoitteet = lisatietotyyppiCreateDto.getRajoitteet().stream()
                .map(rajoiteDto -> {
                    Rajoite rajoite;
                    switch (rajoiteDto.getRajoitetyyppi()) {
                        case OPPILAITOSTYYPPI:
                            rajoite = new OppilaitostyyppiRajoite();
                            break;
                        case ORGANISAATIOTYYPPI:
                            rajoite = new OrganisaatiotyyppiRajoite();
                            break;
                        default:
                            throw new IllegalArgumentException(String.format("Illegal rajoitetyyppi %s", rajoiteDto.getRajoitetyyppi().getValue()));
                    }
                    rajoite.setArvo(rajoiteDto.getArvo());
                    return rajoite;
                })
                .collect(Collectors.toSet());

        Lisatietotyyppi lisatietotyyppi = new Lisatietotyyppi();
        lisatietotyyppi.setNimi(lisatietotyyppiCreateDto.getNimi());
        lisatietotyyppi.setRajoitteet(rajoitteet);

        Lisatietotyyppi createdLisatietotyyppi = this.lisatietoTyyppiDao.insert(lisatietotyyppi);
        return createdLisatietotyyppi.getNimi();
    }

    @Override
    @Transactional
    public void delete(String nimi) {
        Lisatietotyyppi lisatietotyyppi = this.lisatietoTyyppiDao.findByNimi(nimi)
                .orElseThrow(() -> new NotFoundException(String.format("Can't find lisatietotyyppi with nimi %s", nimi)));
        this.lisatietoTyyppiDao.remove(lisatietotyyppi);
    }
}
