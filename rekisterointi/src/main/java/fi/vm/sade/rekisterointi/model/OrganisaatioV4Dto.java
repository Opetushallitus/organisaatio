package fi.vm.sade.rekisterointi.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.singletonList;

public class OrganisaatioV4Dto extends BaseDto {

    public String oid;
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

    public static OrganisaatioV4Dto of(String businessId, String organisationName) {
        OrganisaatioV4Dto organisaatio = new OrganisaatioV4Dto();
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

}
