package fi.vm.sade.varda.rekisterointi.model;

import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.singletonList;

@EqualsAndHashCode
public class OrganisaatioDto extends BaseDto {

    public static final String OPPILAITOS_ORGANISAATIOTYYPPI = "organisaatiotyyppi_02";
    public static final String OPPILAITOSTYYPPI_EI_TIEDOSSA_WITH_VERSION = "oppilaitostyyppi_xx#1";

    public String oid;
    public String parentOid;
    public String oppilaitosTyyppiUri;
    // jalkelaiset endpoint returns this but it equals oppilaitosTyyppiUri
    public String oppilaitostyyppi;
    public String ytunnus;
    public boolean piilotettu;
    public Map<String, String> nimi;
    public List<OrganisaatioNimi> nimet;
    public LocalDate alkuPvm;
    public LocalDate lakkautusPvm;
    public String yritysmuoto;
    public Set<String> tyypit;
    public String kotipaikkaUri;
    public String maaUri;
    public String ytjkieli;
    public Set<String> kieletUris;
    public List<YhteystietoDto> yhteystiedot;


    public static OrganisaatioDto of(String businessId, String organisationName) {
        OrganisaatioDto organisaatio = new OrganisaatioDto();
        organisaatio.ytunnus = businessId;
        organisaatio.nimi = Map.of("fi", organisationName);
        organisaatio.alkuPvm = null;
        organisaatio.lakkautusPvm = null;
        OrganisaatioNimi organisaatioNimi = new OrganisaatioNimi();
        organisaatioNimi.alkuPvm = organisaatio.alkuPvm;
        organisaatioNimi.nimi = organisaatio.nimi;
        organisaatio.nimet = singletonList(organisaatioNimi);
        organisaatio.maaUri = "maatjavaltiot1_fin";
        organisaatio.kieletUris = Set.of();
        return organisaatio;
    }

    public static OrganisaatioDto jotpaChildOppilaitosFrom(OrganisaatioDto parentDto) {
        OrganisaatioDto organisaatio = new OrganisaatioDto();
        organisaatio.nimi = parentDto.nimi;
        organisaatio.alkuPvm = parentDto.alkuPvm;
        organisaatio.lakkautusPvm = null;
        organisaatio.nimet = parentDto.nimet;
        organisaatio.maaUri = parentDto.maaUri;
        organisaatio.kieletUris = parentDto.kieletUris;
        organisaatio.yhteystiedot = parentDto.yhteystiedot;
        organisaatio.ytjkieli = parentDto.ytjkieli;
        organisaatio.kotipaikkaUri = parentDto.kotipaikkaUri;
        organisaatio.parentOid = parentDto.oid;
        organisaatio.tyypit = Set.of(OPPILAITOS_ORGANISAATIOTYYPPI);
        organisaatio.oppilaitosTyyppiUri = OPPILAITOSTYYPPI_EI_TIEDOSSA_WITH_VERSION; // EI tiedossa koodisto
        return organisaatio;
    }

}
