package fi.vm.sade.organisaatio.service.converter;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;
import fi.vm.sade.organisaatio.model.Organisaatio;
import org.springframework.core.convert.converter.Converter;

import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class OrganisaatioToOrganisaatioPerustietoConverter implements Converter<Organisaatio, OrganisaatioPerustieto> {

    @Override
    public OrganisaatioPerustieto convert(Organisaatio source) {
        OrganisaatioPerustieto destination = new OrganisaatioPerustieto();
        destination.setOid(source.getOid());
        // java.sql.Date -> java.util.Date jotta json-formaatti sama kuin solr-toteutuksessa
        destination.setAlkuPvm(clone(source.getAlkuPvm()));
        destination.setTarkastusPvm(clone(source.getTarkastusPvm()));
        destination.setLakkautusPvm(clone(source.getLakkautusPvm()));
        destination.setMaskingActive(source.isMaskingActive());
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
        destination.setLyhytNimi(source.getActualNimi().getValues());

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
