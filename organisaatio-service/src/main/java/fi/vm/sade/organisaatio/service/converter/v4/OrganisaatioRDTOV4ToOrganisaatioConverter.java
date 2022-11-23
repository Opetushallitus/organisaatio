package fi.vm.sade.organisaatio.service.converter.v4;

import fi.vm.sade.organisaatio.dto.VarhaiskasvatuksenKielipainotusDto;
import fi.vm.sade.organisaatio.dto.VarhaiskasvatuksenToiminnallinepainotusDto;
import fi.vm.sade.organisaatio.dto.VarhaiskasvatuksenToimipaikkaTiedotDto;
import fi.vm.sade.organisaatio.dto.mapping.OrganisaatioNimiModelMapper;
import fi.vm.sade.organisaatio.dto.v4.OrganisaatioRDTOV4;
import fi.vm.sade.organisaatio.model.*;
import fi.vm.sade.organisaatio.service.converter.util.MetadataConverterUtils;
import fi.vm.sade.organisaatio.service.converter.util.MonikielinenTekstiConverterUtils;
import fi.vm.sade.organisaatio.service.converter.util.YhteystietoConverterUtils;
import fi.vm.sade.organisaatio.service.util.OrganisaatioNimiUtil;
import fi.vm.sade.organisaatio.service.util.OrganisaatioUtil;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class OrganisaatioRDTOV4ToOrganisaatioConverter implements Converter<OrganisaatioRDTOV4, Organisaatio> {
    private final OrganisaatioNimiModelMapper organisaatioNimiModelMapper;

    @Autowired
    public OrganisaatioRDTOV4ToOrganisaatioConverter(OrganisaatioNimiModelMapper organisaatioNimiModelMapper) {
        this.organisaatioNimiModelMapper = organisaatioNimiModelMapper;
    }

    @Override
    public Organisaatio convert(OrganisaatioRDTOV4 source) {
        Set<Yhteystieto> yhteystietos = new HashSet<>();
        Organisaatio target = new Organisaatio();

        target.setOid(source.getOid());
        target.setVersion((long) source.getVersion());

        target.setAlkuPvm(source.getAlkuPvm());
        target.setDomainNimi(source.getDomainNimi());

        target.setKielet(convertCollectionToSet(source.getKieletUris()));
        target.setKotipaikka(source.getKotipaikkaUri());
        target.setMuutKotipaikatUris(convertCollectionToSet(source.getMuutKotipaikatUris()));
        target.setKuvaus2(MonikielinenTekstiConverterUtils.convertMapToMonikielinenTeksti(source.getKuvaus2()));
        target.setLakkautusPvm(source.getLakkautusPvm());
        target.setTarkastusPvm(source.getTarkastusPvm());
        target.setMaa(source.getMaaUri());
        target.setMetadata(MetadataConverterUtils.convertMetadata(source.getMetadata()));
        target.setNimi(MonikielinenTekstiConverterUtils.convertMapToMonikielinenTeksti(source.getNimi()));

        // Define the target list type for mapping
        Type organisaatioNimiListType = new TypeToken<List<OrganisaatioNimi>>() {}.getType();

        // Map DTO to domain type
        target.setNimet(organisaatioNimiModelMapper.map(source.getNimet(), organisaatioNimiListType));

        // Asetetaan nimihakuun nimeksi nimihistorian current nimi, tai uusin nimi
        MonikielinenTeksti nimi = OrganisaatioNimiUtil.getNimi(target.getNimet());
        target.setNimihaku(OrganisaatioNimiUtil.createNimihaku(nimi));

        target.setOpetuspisteenJarjNro(source.getOpetuspisteenJarjNro());
        target.setOppilaitosKoodi(source.getOppilaitosKoodi());
        target.setOppilaitosTyyppi(source.getOppilaitosTyyppiUri());
        target.setMuutOppilaitosTyyppiUris(convertCollectionToSet(source.getMuutOppilaitosTyyppiUris()));
        target.setParentOids(OrganisaatioUtil.parentOids(source.getParentOidPath()));

        target.setToimipisteKoodi(source.getToimipistekoodi());
        target.setTyypit(this.convertSetToSet(source.getTyypit()));
        target.setVuosiluokat(convertCollectionToSet(source.getVuosiluokat()));
        target.setOrganisaatioLisatietotyypit(source.getLisatiedot().stream()
                .map(lisatietoNimi -> {
                    OrganisaatioLisatietotyyppi organisaatioLisatietotyyppi = new OrganisaatioLisatietotyyppi();
                    Lisatietotyyppi lisatietotyyppi = new Lisatietotyyppi();
                    lisatietotyyppi.setNimi(lisatietoNimi);
                    organisaatioLisatietotyyppi.setLisatietotyyppi(lisatietotyyppi);
                    organisaatioLisatietotyyppi.setOrganisaatio(target);
                    return organisaatioLisatietotyyppi;
                })
                .collect(Collectors.toSet()));
        target.setRyhmatyypit(convertSetToSet(source.getRyhmatyypit()));
        target.setKayttoryhmat(convertSetToSet(source.getKayttoryhmat()));
        target.setYhteishaunKoulukoodi(source.getYhteishaunKoulukoodi());
        target.setYritysmuoto(source.getYritysmuoto());
        target.setYtjKieli(source.getYTJKieli());
        target.setYtjPaivitysPvm(source.getYTJPaivitysPvm());
        target.setYtunnus(source.getYTunnus());
        target.setVirastoTunnus(source.getVirastoTunnus());

        target.setPiilotettu(Optional.ofNullable(source.getPiilotettu())
                .orElseGet(() -> Optional.ofNullable(target.getVarhaiskasvatuksenToimipaikkaTiedot()).map(tiedot -> tiedot.getToimintamuoto().contains("tm02") || tiedot.getToimintamuoto().contains("tm03")).orElse(false)));

        if (source.getYhteystietoArvos() != null) {
            target.setYhteystietoArvos(YhteystietoConverterUtils.convertYhteystietoArvos(source.getYhteystietoArvos()));
        }

        for (Map<String, String> m : source.getYhteystiedot()) {
            Yhteystieto y = YhteystietoConverterUtils.convertYhteystietoGeneric(m);
            if (y != null) {
                yhteystietos.add(y);
            }
        }
        target.setYhteystiedot(yhteystietos);

        Optional.ofNullable(source.getVarhaiskasvatuksenToimipaikkaTiedot())
                .map(this::varhaiskasvatuksenToimipaikkaTiedotDtoToEntity)
                .ifPresent(target::setVarhaiskasvatuksenToimipaikkaTiedot);

        return target;
    }

    private VarhaiskasvatuksenToimipaikkaTiedot varhaiskasvatuksenToimipaikkaTiedotDtoToEntity(VarhaiskasvatuksenToimipaikkaTiedotDto toimipaikkaTiedotDto) {
        VarhaiskasvatuksenToimipaikkaTiedot varhaiskasvatuksenToimipaikkaTiedot = new VarhaiskasvatuksenToimipaikkaTiedot();
        varhaiskasvatuksenToimipaikkaTiedot.setToimintamuoto(toimipaikkaTiedotDto.getToimintamuoto());
        varhaiskasvatuksenToimipaikkaTiedot.setKasvatusopillinenJarjestelma(toimipaikkaTiedotDto.getKasvatusopillinenJarjestelma());
        varhaiskasvatuksenToimipaikkaTiedot.setPaikkojenLukumaara(toimipaikkaTiedotDto.getPaikkojenLukumaara());
        Optional.ofNullable(toimipaikkaTiedotDto.getVarhaiskasvatuksenToiminnallinenpainotukset())
                .map(this::varhaiskasvatuksenToiminnallinenpainotusDtoToEntity)
                .ifPresent(varhaiskasvatuksenToimipaikkaTiedot::setVarhaiskasvatuksenToiminnallinenpainotukset);
        varhaiskasvatuksenToimipaikkaTiedot.setVarhaiskasvatuksenJarjestamismuodot(this.convertSetToSet(toimipaikkaTiedotDto.getVarhaiskasvatuksenJarjestamismuodot()));
        Optional.ofNullable(toimipaikkaTiedotDto.getVarhaiskasvatuksenKielipainotukset())
                .map(this::varhaiskasvatuksenKielipainotuksetDtoToEntity)
                .ifPresent(varhaiskasvatuksenToimipaikkaTiedot::setVarhaiskasvatuksenKielipainotukset);

        return varhaiskasvatuksenToimipaikkaTiedot;
    }

    private Set<VarhaiskasvatuksenKielipainotus> varhaiskasvatuksenKielipainotuksetDtoToEntity(Set<VarhaiskasvatuksenKielipainotusDto> varhaiskasvatuksenKielipainotusDtoSet) {
        return varhaiskasvatuksenKielipainotusDtoSet.stream()
                .map(kielipainotusDto -> {
                    VarhaiskasvatuksenKielipainotus varhaiskasvatuksenKielipainotus = new VarhaiskasvatuksenKielipainotus();
                    varhaiskasvatuksenKielipainotus.setAlkupvm(this.localDateToDate(kielipainotusDto.getAlkupvm()));
                    varhaiskasvatuksenKielipainotus.setLoppupvm(this.localDateToDate(kielipainotusDto.getLoppupvm()));
                    varhaiskasvatuksenKielipainotus.setKielipainotus(kielipainotusDto.getKielipainotus());
                    return varhaiskasvatuksenKielipainotus;
                })
                .collect(Collectors.toSet());
    }

    private Set<VarhaiskasvatuksenToiminnallinenpainotus> varhaiskasvatuksenToiminnallinenpainotusDtoToEntity(Set<VarhaiskasvatuksenToiminnallinepainotusDto> varhaiskasvatuksenToiminnallinenpainotusDtoSet) {
        return varhaiskasvatuksenToiminnallinenpainotusDtoSet.stream()
                .map(toiminnallinepainotusDto -> {
                    VarhaiskasvatuksenToiminnallinenpainotus varhaiskasvatuksenToiminnallinenpainotus = new VarhaiskasvatuksenToiminnallinenpainotus();
                    varhaiskasvatuksenToiminnallinenpainotus.setAlkupvm(this.localDateToDate(toiminnallinepainotusDto.getAlkupvm()));
                    varhaiskasvatuksenToiminnallinenpainotus.setLoppupvm(this.localDateToDate(toiminnallinepainotusDto.getLoppupvm()));
                    varhaiskasvatuksenToiminnallinenpainotus.setToiminnallinenpainotus(toiminnallinepainotusDto.getToiminnallinenpainotus());
                    return varhaiskasvatuksenToiminnallinenpainotus;
                })
                .collect(Collectors.toSet());
    }

    private Date localDateToDate(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private Set<String> convertCollectionToSet(Collection<String> s) {
        return new HashSet<>(s);
    }

    private Set<String> convertSetToSet(Set<String> s) {
        return new HashSet<>(s);
    }
}
