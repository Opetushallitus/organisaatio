package fi.vm.sade.security;

import fi.vm.sade.organisaatio.business.OrganisaatioFindBusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


@Component
public class OidProvider {

    @Autowired
    private OrganisaatioFindBusinessService organisaatioFindBusinessService;
    @Value("${root.organisaatio.oid}")
    private String rootOrganisaatioOid;

    public OidProvider() {
    }


    public List<String> getSelfAndParentOids(String organisaatioOid) {
        return Optional.ofNullable(organisaatioOid).map(oid ->
                        Optional.ofNullable(organisaatioFindBusinessService.findById(oid))
                                .map(organisaatio -> Optional.ofNullable(organisaatio.getParentOids())
                                        .map(parentOids -> {
                                            List<String> a = new ArrayList<>(parentOids);
                                            a.add(organisaatioOid);
                                            return a;
                                        })
                                        .orElseGet(() -> Arrays.asList(organisaatioOid)))
                                .orElseGet(() -> Arrays.asList(rootOrganisaatioOid, organisaatioOid)))
                .orElseGet(() -> Arrays.asList(rootOrganisaatioOid));
    }
}
