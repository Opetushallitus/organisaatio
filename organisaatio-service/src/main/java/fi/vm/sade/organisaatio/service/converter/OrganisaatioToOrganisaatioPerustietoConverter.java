package fi.vm.sade.organisaatio.service.converter;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;
import fi.vm.sade.organisaatio.auth.PermissionChecker;
import fi.vm.sade.organisaatio.model.Organisaatio;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import java.util.stream.Stream;

public class OrganisaatioToOrganisaatioPerustietoConverter implements org.springframework.core.convert.converter.Converter<Organisaatio, OrganisaatioPerustieto> {

    @Override
    public OrganisaatioPerustieto convert(Organisaatio source) {
        OrganisaatioPerustieto destination = new OrganisaatioPerustieto();
        destination.setOid(source.getOid());
        // java.sql.Date -> java.util.Date jotta json-formaatti sama kuin solr-toteutuksessa
        destination.setAlkuPvm(clone(source.getAlkuPvm()));
        destination.setLakkautusPvm(clone(source.getLakkautusPvm()));
        List<String> parentOids = Optional.ofNullable(source.getParentOidPath())
                .map(parentOidPath -> Arrays.stream(parentOidPath.split("\\|")))
                .orElseGet(() -> Stream.empty())
                .filter(oid -> !oid.isEmpty())
                .collect(toList());
        Collections.reverse(parentOids);
        destination.setParentOid(!parentOids.isEmpty() ? parentOids.get(0) : null);
        parentOids.add(0, source.getOid());
        destination.setParentOidPath(parentOids.stream().collect(joining("/")));
        destination.setYtunnus(source.getYtunnus());
        destination.setVirastoTunnus(source.getVirastoTunnus());
        //destination.setAliOrganisaatioMaara asetetaan muualla
        destination.setOppilaitosKoodi(source.getOppilaitosKoodi());
        destination.setOppilaitostyyppi(source.getOppilaitosTyyppi());
        destination.setToimipistekoodi(source.getToimipisteKoodi());

        //destination.setMatch asetetaan muualla
        destination.setNimi(source.getNimi().getValues());

        source.getTyypit().stream()
                .map(OrganisaatioTyyppi::fromKoodiValue)
                .forEach(destination.getOrganisaatiotyypit()::add);
        source.getKielet().forEach(destination.getKieletUris()::add);
        destination.setKotipaikkaUri(source.getKotipaikka());
        //destination.setChildren asetetaan muualla
        return destination;
    }

    private static Date clone(Date date) {
        return date != null ? new Date(date.getTime()) : null;
    }

}
