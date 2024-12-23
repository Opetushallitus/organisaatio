package fi.vm.sade.varda.rekisterointi.model;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class VirkailijaCriteria {

    public Boolean passivoitu;
    public Boolean duplikaatti;

    public Set<String> organisaatioOids;
    public Map<String, Collection<String>> kayttooikeudet;

}
