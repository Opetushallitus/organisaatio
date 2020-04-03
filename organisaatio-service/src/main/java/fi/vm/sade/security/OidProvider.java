package fi.vm.sade.security;

import fi.vm.sade.organisaatio.dao.OrganisaatioDAO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Component
public class OidProvider {

    private final String rootOrganisaatioOid;
    private final OrganisaatioDAO organisaatioDAO;

    public OidProvider(@Value("${root.organisaatio.oid}") String rootOrganisaatioOid,
                       OrganisaatioDAO organisaatioDAO) {
        this.rootOrganisaatioOid = rootOrganisaatioOid;
        this.organisaatioDAO = organisaatioDAO;
    }

    public List<String> getSelfAndParentOids(String organisaatioOid) {
        Stream<String> parentOids = Optional.ofNullable(organisaatioDAO.findByOid(organisaatioOid))
                .map(organisaatio -> organisaatio.getParentOids().stream())
                .orElseGet(() -> Stream.of(rootOrganisaatioOid));
        return Stream.concat(Stream.of(organisaatioOid), parentOids).collect(
                Collectors.collectingAndThen(toList(), strings -> {
                    Collections.reverse(strings);
                    return strings;
                }));
    }

}
