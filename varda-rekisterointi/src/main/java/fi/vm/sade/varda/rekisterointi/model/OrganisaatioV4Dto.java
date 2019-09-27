package fi.vm.sade.varda.rekisterointi.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;

public class OrganisaatioV4Dto extends BaseDto {

    public String oid;

    public String ytunnus;
    public Map<String, String> nimi;
    public List<OrganisaatioNimi> nimet;

    public LocalDate alkuPvm;

    public static OrganisaatioV4Dto of(String businessId, String organisationName) {
        OrganisaatioV4Dto organisaatio = new OrganisaatioV4Dto();
        organisaatio.ytunnus = businessId;
        organisaatio.nimi = Map.of("fi", organisationName);
        organisaatio.alkuPvm = null;
        OrganisaatioNimi organisaatioNimi = new OrganisaatioNimi();
        organisaatioNimi.alkuPvm = organisaatio.alkuPvm;
        organisaatioNimi.nimi = organisaatio.nimi;
        organisaatio.nimet = singletonList(organisaatioNimi);
        return organisaatio;
    }

}
