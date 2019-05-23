package fi.vm.sade.varda.rekisterointi.model;

import fi.vm.sade.suomifi.valtuudet.OrganisationDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Organisaatio extends BaseDto {

    public String oid;

    public String parentOid;
    public String ytunnus;
    public Map<String, String> nimi;
    public List<OrganisaatioNimi> nimet;

    public LocalDate alkuPvm;
    public LocalDate lakkautusPvm;

    public String ytjKieli; // koodisto 'kieli'
    public LocalDate ytjPaivitysPvm;

    public Set<String> kieletUris; // koodisto 'oppilaitoksen opetuskieli'
    public Set<String> tyypit; // koodisto 'organisaatiotyyppi'
    public String maaUri; // koodisto 'maat ja valtiot 1'
    public String kotipaikkaUri; // koodisto 'kunta'

    public static Organisaatio of(OrganisationDto dto) {
        Organisaatio organisaatio = new Organisaatio();
        organisaatio.ytunnus = dto.identifier;
        organisaatio.nimi = Map.of("fi", dto.name);
        return organisaatio;
    }

}
