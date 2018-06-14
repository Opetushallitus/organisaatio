package fi.vm.sade.organisaatio.business.impl;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.business.LisatietoService;
import fi.vm.sade.organisaatio.business.OrganisaatioKoodisto;
import fi.vm.sade.organisaatio.dao.LisatietoTyyppiDao;
import fi.vm.sade.organisaatio.dto.LisatietotyyppiCreateDto;
import fi.vm.sade.organisaatio.dto.LisatietotyyppiDto;
import fi.vm.sade.organisaatio.dto.RajoiteDto;
import fi.vm.sade.organisaatio.model.Lisatietotyyppi;
import fi.vm.sade.organisaatio.model.OppilaitostyyppiRajoite;
import fi.vm.sade.organisaatio.model.OrganisaatiotyyppiRajoite;
import fi.vm.sade.organisaatio.model.Rajoite;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ValidationException;
import javax.ws.rs.NotFoundException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LisatietoServiceImpl implements LisatietoService {
    private final LisatietoTyyppiDao lisatietoTyyppiDao;

    private final OrganisaatioKoodisto organisaatioKoodisto;

    private final ConversionService conversionService;

    public LisatietoServiceImpl(LisatietoTyyppiDao lisatietoTyyppiDao,
                                OrganisaatioKoodisto organisaatioKoodisto,
                                ConversionService conversionService) {
        this.lisatietoTyyppiDao = lisatietoTyyppiDao;
        this.organisaatioKoodisto = organisaatioKoodisto;
        this.conversionService = conversionService;
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
        Lisatietotyyppi lisatietotyyppi = new Lisatietotyyppi();
        Set<Rajoite> rajoitteet = lisatietotyyppiCreateDto.getRajoitteet().stream()
                .map(this::mapAndValidateRajoite)
                .map(rajoite -> rajoite.setLisatietotyyppi(lisatietotyyppi))
                .collect(Collectors.toSet());

        lisatietotyyppi.setNimi(lisatietotyyppiCreateDto.getNimi());
        lisatietotyyppi.setRajoitteet(rajoitteet);

        Lisatietotyyppi createdLisatietotyyppi = this.lisatietoTyyppiDao.insert(lisatietotyyppi);
        return createdLisatietotyyppi.getNimi();
    }

    private Rajoite mapAndValidateRajoite(RajoiteDto rajoiteDto) {
        Rajoite rajoite;
        switch (rajoiteDto.getRajoitetyyppi()) {
            case OPPILAITOSTYYPPI:
                if (this.organisaatioKoodisto.haeOppilaitoskoodit().stream()
                        .noneMatch(oppilaitoskoodi -> oppilaitoskoodi.equals(rajoiteDto.getArvo()))) {
                    throw new ValidationException("Invalid oppilaitostyyppi " + rajoiteDto.getArvo());
                }
                rajoite = new OppilaitostyyppiRajoite();
                break;
            case ORGANISAATIOTYYPPI:
                if (Arrays.stream(OrganisaatioTyyppi.values()).noneMatch(organisaatioTyyppi ->
                        organisaatioTyyppi.value().equals(rajoiteDto.getArvo()))) {
                    throw new ValidationException("Invalid organisaatiotyyppi " + rajoiteDto.getArvo());
                }
                rajoite = new OrganisaatiotyyppiRajoite();
                break;
            default:
                throw new IllegalArgumentException(String.format("Illegal rajoitetyyppi %s", rajoiteDto.getRajoitetyyppi().getValue()));
        }
        rajoite.setArvo(rajoiteDto.getArvo());
        return rajoite;
    }

    @Override
    @Transactional
    public void delete(String nimi) {
        Lisatietotyyppi lisatietotyyppi = this.lisatietoTyyppiDao.findByNimi(nimi)
                .orElseThrow(() -> new NotFoundException(String.format("Can't find lisatietotyyppi with nimi %s", nimi)));
        this.lisatietoTyyppiDao.remove(lisatietotyyppi);
    }

    @Transactional(readOnly = true)
    @Override
    public LisatietotyyppiDto findByName(String nimi) {
        Lisatietotyyppi lisatietotyyppi = this.lisatietoTyyppiDao.findByNimi(nimi)
                .orElseThrow(() -> new NotFoundException(String.format("Can't find lisatietotyyppi with nimi %s", nimi)));
        return this.conversionService.convert(lisatietotyyppi, LisatietotyyppiDto.class);
    }

}
