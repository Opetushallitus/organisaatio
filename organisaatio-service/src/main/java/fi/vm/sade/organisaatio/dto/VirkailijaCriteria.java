package fi.vm.sade.organisaatio.dto;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VirkailijaCriteria {
    private Boolean passivoitu;
    private Boolean duplikaatti;
    private Collection<String> organisaatioOids;
    private Map<String, Collection<String>> kayttooikeudet;
    private Set<String> kayttoOikeusRyhmaNimet;
}
