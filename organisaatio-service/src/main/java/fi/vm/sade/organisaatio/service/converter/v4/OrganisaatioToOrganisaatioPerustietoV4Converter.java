package fi.vm.sade.organisaatio.service.converter.v4;

import fi.vm.sade.organisaatio.dto.v4.OrganisaatioPerustietoV4;
import fi.vm.sade.organisaatio.model.Organisaatio;
import org.springframework.core.convert.converter.Converter;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

public class OrganisaatioToOrganisaatioPerustietoV4Converter implements Converter<Organisaatio, OrganisaatioPerustietoV4> {

    @Override
    public OrganisaatioPerustietoV4 convert(Organisaatio source) {
        OrganisaatioPerustietoV4 destination = new OrganisaatioPerustietoV4();
        destination.setMatch(true);
        destination.setOid(source.getOid());
        destination.setAlkuPvm(clone(source.getAlkuPvm()));
        destination.setTarkastusPvm(clone(source.getTarkastusPvm()));
        destination.setLakkautusPvm(clone(source.getLakkautusPvm()));
        List<String> parentOids = Optional.ofNullable(source.getParentOidPath()).stream().flatMap(parentOidPath -> Arrays.stream(parentOidPath.split("\\|")))
                .filter(oid -> !oid.isEmpty())
                .collect(toList());
        destination.setParentOid(source.getParentOid().orElse(null));
        destination.setParentOidPath(String.join("/", parentOids));
        destination.setYtunnus(source.getYtunnus());
        destination.setVirastoTunnus(source.getVirastoTunnus());
        destination.setOppilaitosKoodi(source.getOppilaitosKoodi());
        destination.setOppilaitostyyppi(source.getOppilaitosTyyppi());
        destination.setToimipistekoodi(source.getToimipisteKoodi());
        destination.setNimi(source.getActualNimi().getValues());
//        destination.setNimi(source.getNimi().getValues());
//        destination.setLyhytNimi(source.getActualNimi().getValues());
        source.getTyypit()
                .forEach(destination.getOrganisaatiotyypit()::add);
        source.getKielet().forEach(destination.getKieletUris()::add);
        destination.setKotipaikkaUri(source.getKotipaikka());
        return destination;
    }

    private static Date clone(Date date) {
        return date != null ? new Date(date.getTime()) : null;
    }

}
