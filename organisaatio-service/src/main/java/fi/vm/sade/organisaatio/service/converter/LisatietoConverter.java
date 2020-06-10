package fi.vm.sade.organisaatio.service.converter;

import fi.vm.sade.organisaatio.dto.LisatietotyyppiDto;
import fi.vm.sade.organisaatio.dto.RajoiteDto;
import fi.vm.sade.organisaatio.dto.Rajoitetyyppi;
import fi.vm.sade.organisaatio.model.Lisatietotyyppi;
import fi.vm.sade.organisaatio.model.OppilaitostyyppiRajoite;
import fi.vm.sade.organisaatio.model.OrganisaatiotyyppiRajoite;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class LisatietoConverter implements Converter<Lisatietotyyppi, LisatietotyyppiDto> {

    @Override
    public LisatietotyyppiDto convert(Lisatietotyyppi lisatietotyyppi) {
        LisatietotyyppiDto lisatietotyyppiDto = new LisatietotyyppiDto();
        lisatietotyyppiDto.setNimi(lisatietotyyppi.getNimi());
        lisatietotyyppiDto.setRajoitteet(lisatietotyyppi.getRajoitteet().stream()
                .map(rajoite -> {
                    RajoiteDto rajoiteDto = new RajoiteDto();
                    if (rajoite instanceof OppilaitostyyppiRajoite) {
                        rajoiteDto.setRajoitetyyppi(Rajoitetyyppi.OPPILAITOSTYYPPI);
                    }
                    else if (rajoite instanceof OrganisaatiotyyppiRajoite) {
                        rajoiteDto.setRajoitetyyppi(Rajoitetyyppi.ORGANISAATIOTYYPPI);
                    }
                    else {
                        throw new RuntimeException("Data inconsistency illegal rajoitetyyppi " + rajoite.getClass());
                    }
                    rajoiteDto.setArvo(rajoite.getArvo());
                    return rajoiteDto;
                }).collect(Collectors.toSet()));

        return lisatietotyyppiDto;
    }
}
