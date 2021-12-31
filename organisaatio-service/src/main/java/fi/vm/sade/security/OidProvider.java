package fi.vm.sade.security;

import fi.vm.sade.organisaatio.business.OrganisaatioFindBusinessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


@Component
public class OidProvider {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    @Autowired
    private OrganisaatioFindBusinessService organisaatioFindBusinessService;
    @Value("${root.organisaatio.oid}")
    private String rootOrganisaatioOid;

    public OidProvider() {
    }


    public List<String> getSelfAndParentOids(String organisaatioOid) {
        return Optional.ofNullable(organisaatioFindBusinessService.findById(organisaatioOid))
                .map(organisaatio -> Optional.ofNullable(organisaatio.getParentOids())
                        .map(parentOids -> {
                            List<String> a = new ArrayList<>(parentOids);
                            a.add(organisaatioOid);
                            return a;
                        })
                        .orElseGet(() -> Arrays.asList(organisaatioOid)))
                .orElseGet(() -> Arrays.asList(rootOrganisaatioOid, organisaatioOid));
    }
}
