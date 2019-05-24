package fi.vm.sade.organisaatio.service.converter.v4;

import fi.vm.sade.organisaatio.auth.PermissionChecker;
import fi.vm.sade.organisaatio.dto.VarhaiskasvatuksenKielipainotusDto;
import fi.vm.sade.organisaatio.dto.VarhaiskasvatuksenToiminnallinepainotusDto;
import fi.vm.sade.organisaatio.dto.VarhaiskasvatuksenToimipaikkaTiedotDto;
import fi.vm.sade.organisaatio.dto.mapping.OrganisaatioNimiModelMapper;
import fi.vm.sade.organisaatio.dto.v4.OrganisaatioRDTOV4;
import fi.vm.sade.organisaatio.model.*;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioNimiRDTO;
import fi.vm.sade.organisaatio.service.converter.AbstractFromDomainConverter;
import fi.vm.sade.organisaatio.service.converter.util.MetadataConverterUtils;
import fi.vm.sade.organisaatio.service.converter.util.YhteystietoConverterUtils;
import org.modelmapper.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class OrganisaatioToOrganisaatioRDTOV4Converter extends AbstractFromDomainConverter<Organisaatio, OrganisaatioRDTOV4> {

    private static final Logger LOG = LoggerFactory.getLogger(OrganisaatioToOrganisaatioRDTOV4Converter.class);

    private final OrganisaatioNimiModelMapper organisaatioNimiModelMapper;
    private final Type organisaatioNimiRDTOListType;

    private final PermissionChecker permissionChecker;

    @Autowired
    public OrganisaatioToOrganisaatioRDTOV4Converter(OrganisaatioNimiModelMapper organisaatioNimiModelMapper, PermissionChecker permissionChecker) {
        this.organisaatioNimiRDTOListType = new TypeToken<List<OrganisaatioNimiRDTO>>() {}.getType();
        this.organisaatioNimiModelMapper = organisaatioNimiModelMapper;
        this.permissionChecker = permissionChecker;
    }

    @Override
    public OrganisaatioRDTOV4 convert(Organisaatio s) {
        long qstarted = System.currentTimeMillis();

        OrganisaatioRDTOV4 t = new OrganisaatioRDTOV4();

        t.setOid(s.getOid());
        t.setVersion(s.getVersion() != null ? s.getVersion().intValue() : 0);

        t.setAlkuPvm(s.getAlkuPvm());
        t.setDomainNimi(s.getDomainNimi());

        t.setKieletUris(convertCollectionToSet(s.getKielet()));
        t.setKotipaikkaUri(s.getKotipaikka());
        t.setKuvaus2(YhteystietoConverterUtils.convertMKTToMap(s.getKuvaus2()));
        t.setLakkautusPvm(s.getLakkautusPvm());
        t.setMaaUri(s.getMaa());
        t.setMetadata(MetadataConverterUtils.convertMetadata(s.getMetadata()));

        t.setStatus(s.getStatus().name());

        t.setOppilaitosKoodi(s.getOppilaitosKoodi());
        t.setOppilaitosTyyppiUri(s.getOppilaitosTyyppi());
        t.setParentOid(s.getParentOid().orElse(null));
        t.setParentOidPath(s.getParentOidPath());

        t.setOpetuspisteenJarjNro(s.getOpetuspisteenJarjNro());
        t.setToimipistekoodi(s.getToimipisteKoodi());
        t.setTyypit(this.convertCollectionToSet(s.getTyypit()));
        t.setLisatiedot(convertSetToSet(s.getOrganisaatioLisatietotyypit().stream()
                .map(OrganisaatioLisatietotyyppi::getLisatietotyyppi)
                .map(Lisatietotyyppi::getNimi)
                .collect(Collectors.toSet())));
        t.setVuosiluokat(convertCollectionToSet(s.getVuosiluokat()));
        t.setRyhmatyypit(convertSetToSet(s.getRyhmatyypit()));
        t.setKayttoryhmat(convertSetToSet(s.getKayttoryhmat()));
        t.setYhteishaunKoulukoodi(s.getYhteishaunKoulukoodi());
        t.setYritysmuoto(s.getYritysmuoto());
        t.setYTJKieli(s.getYtjKieli());
        t.setYTJPaivitysPvm(s.getYtjPaivitysPvm());
        t.setYTunnus(s.getYtunnus());
        t.setVirastoTunnus(s.getVirastoTunnus());

        // Get dynamic Yhteysieto / Yhteystietotyppie / Elementti data
        Set<Map<String, String>> yhteystietoArvos = new HashSet<>();
        t.setYhteystietoArvos(yhteystietoArvos);

        if(permissionChecker.canReadOrganisationIfHidden(s)) {
            t.setKayntiosoite(YhteystietoConverterUtils.convertOsoiteToMap(s.getKayntiosoite()));
            t.setNimi(YhteystietoConverterUtils.convertMKTToMap(s.getNimi()));
            t.setNimet(organisaatioNimiModelMapper.map(s.getNimet(), organisaatioNimiRDTOListType));
            t.setPostiosoite(YhteystietoConverterUtils.convertOsoiteToMap(s.getPostiosoite()));

            for (Yhteystieto y : s.getYhteystiedot()) {
                t.addYhteystieto(YhteystietoConverterUtils.mapYhteystietoToGeneric(y));
            }
            YhteystietoConverterUtils.convertYhteystietosToListMap(s, yhteystietoArvos);
        }

        Optional.ofNullable(s.getVarhaiskasvatuksenToimipaikkaTiedot())
                .map(this::varhaiskasvatuksenToimipaikkaTiedotEntityToDto)
                .ifPresent(t::setVarhaiskasvatuksenToimipaikkaTiedot);

        LOG.debug("convert: {} --> " + t.getClass().getSimpleName() + " in {} ms", s, System.currentTimeMillis() - qstarted);

        return t;
    }

    private VarhaiskasvatuksenToimipaikkaTiedotDto varhaiskasvatuksenToimipaikkaTiedotEntityToDto(VarhaiskasvatuksenToimipaikkaTiedot toimipaikkaTiedot) {
        VarhaiskasvatuksenToimipaikkaTiedotDto varhaiskasvatuksenToimipaikkaTiedotDto = new VarhaiskasvatuksenToimipaikkaTiedotDto();
        varhaiskasvatuksenToimipaikkaTiedotDto.setToimintamuoto(toimipaikkaTiedot.getToimintamuoto());
        varhaiskasvatuksenToimipaikkaTiedotDto.setKasvatusopillinenJarjestelma(toimipaikkaTiedot.getKasvatusopillinenJarjestelma());
        varhaiskasvatuksenToimipaikkaTiedotDto.setPaikkojenLukumaara(toimipaikkaTiedot.getPaikkojenLukumaara());
        Optional.ofNullable(toimipaikkaTiedot.getVarhaiskasvatuksenToiminnallinenpainotukset())
                .map(this::varhaiskasvatuksenToiminnallinenpainotusEntityToDto)
                .ifPresent(varhaiskasvatuksenToimipaikkaTiedotDto::setVarhaiskasvatuksenToiminnallinenpainotukset);
        varhaiskasvatuksenToimipaikkaTiedotDto.setVarhaiskasvatuksenJarjestamismuodot(this.convertSetToSet(toimipaikkaTiedot.getVarhaiskasvatuksenJarjestamismuodot()));
        Optional.ofNullable(toimipaikkaTiedot.getVarhaiskasvatuksenKielipainotukset())
                .map(this::varhaiskasvatuksenKielipainotuksetEntityToDto)
                .ifPresent(varhaiskasvatuksenToimipaikkaTiedotDto::setVarhaiskasvatuksenKielipainotukset);
        varhaiskasvatuksenToimipaikkaTiedotDto.setPiilotettu(toimipaikkaTiedot.getPiilotettu());
        return varhaiskasvatuksenToimipaikkaTiedotDto;
    }

    private Set<VarhaiskasvatuksenKielipainotusDto> varhaiskasvatuksenKielipainotuksetEntityToDto(Set<VarhaiskasvatuksenKielipainotus> varhaiskasvatuksenKielipainotusSet) {
        return varhaiskasvatuksenKielipainotusSet.stream()
                .map(kielipainotus -> {
                    VarhaiskasvatuksenKielipainotusDto varhaiskasvatuksenKielipainotus = new VarhaiskasvatuksenKielipainotusDto();
                    varhaiskasvatuksenKielipainotus.setAlkupvm(this.dateToLocaldate(kielipainotus.getAlkupvm()));
                    varhaiskasvatuksenKielipainotus.setLoppupvm(this.dateToLocaldate(kielipainotus.getLoppupvm()));
                    varhaiskasvatuksenKielipainotus.setKielipainotus(kielipainotus.getKielipainotus());
                    return varhaiskasvatuksenKielipainotus;
                })
                .collect(Collectors.toSet());
    }

    private Set<VarhaiskasvatuksenToiminnallinepainotusDto> varhaiskasvatuksenToiminnallinenpainotusEntityToDto(Set<VarhaiskasvatuksenToiminnallinenpainotus> varhaiskasvatuksenToiminnallinepainotus) {
        return varhaiskasvatuksenToiminnallinepainotus.stream()
                .map(toiminnallinenpainotus -> {
                    VarhaiskasvatuksenToiminnallinepainotusDto varhaiskasvatuksenToiminnallinenpainotusDto = new VarhaiskasvatuksenToiminnallinepainotusDto();
                    varhaiskasvatuksenToiminnallinenpainotusDto.setAlkupvm(this.dateToLocaldate(toiminnallinenpainotus.getAlkupvm()));
                    varhaiskasvatuksenToiminnallinenpainotusDto.setLoppupvm(this.dateToLocaldate(toiminnallinenpainotus.getLoppupvm()));
                    varhaiskasvatuksenToiminnallinenpainotusDto.setToiminnallinenpainotus(toiminnallinenpainotus.getToiminnallinenpainotus());
                    return varhaiskasvatuksenToiminnallinenpainotusDto;
                })
                .collect(Collectors.toSet());
    }

    private LocalDate dateToLocaldate(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }


    private Set<String> convertCollectionToSet(Collection<String> s) {
        return new HashSet<>(s);
    }

    private Set<String> convertSetToSet(Collection<String> s) {
        return new HashSet<>(s);
    }

}
