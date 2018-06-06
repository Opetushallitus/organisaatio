package fi.vm.sade.organisaatio.service.util;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import static java.util.Collections.unmodifiableMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import static java.util.stream.Collectors.toSet;
import java.util.stream.Stream;

public final class OrganisaatioTyyppiUtil {

    private static final Map<String, Set<String>> ORG_TYPE_LIMIT;

    static {
        Map<String, Set<String>> tmp = new HashMap<>();
        tmp.put(OrganisaatioTyyppi.KOULUTUSTOIMIJA.value(), newHashSet(OrganisaatioTyyppi.KOULUTUSTOIMIJA,
                OrganisaatioTyyppi.OPPILAITOS, OrganisaatioTyyppi.TOIMIPISTE, OrganisaatioTyyppi.OPPISOPIMUSTOIMIPISTE));
        tmp.put(OrganisaatioTyyppi.OPPILAITOS.value(), newHashSet(OrganisaatioTyyppi.OPPILAITOS,
                OrganisaatioTyyppi.TOIMIPISTE, OrganisaatioTyyppi.OPPISOPIMUSTOIMIPISTE));
        tmp.put(OrganisaatioTyyppi.TOIMIPISTE.value(), newHashSet(OrganisaatioTyyppi.TOIMIPISTE,
                OrganisaatioTyyppi.OPPISOPIMUSTOIMIPISTE));
        tmp.put(OrganisaatioTyyppi.OPPISOPIMUSTOIMIPISTE.value(), newHashSet(OrganisaatioTyyppi.TOIMIPISTE,
                OrganisaatioTyyppi.OPPISOPIMUSTOIMIPISTE));
        tmp.put(OrganisaatioTyyppi.MUU_ORGANISAATIO.value(), newHashSet(OrganisaatioTyyppi.MUU_ORGANISAATIO));
        tmp.put(OrganisaatioTyyppi.TYOELAMAJARJESTO.value(), newHashSet(OrganisaatioTyyppi.TYOELAMAJARJESTO));
        ORG_TYPE_LIMIT = unmodifiableMap(tmp);
    }

    private OrganisaatioTyyppiUtil() {
    }

    /**
     * Palauttaa ylä- ja aliorganisaatioita haettaessa halutut organisaatiotyypit.
     *
     * @param organisaatiotyyppi organisaation tyyppi
     * @return ylä- ja aliorganisaatioiden tyypit
     */
    public static Set<String> getOrgTypeLimit(String organisaatiotyyppi) {
        return ORG_TYPE_LIMIT.get(organisaatiotyyppi);
    }

    private static Set<String> newHashSet(OrganisaatioTyyppi... organisaatiotyypit) {
        return Stream.of(organisaatiotyypit).map(OrganisaatioTyyppi::value).collect(toSet());
    }

}
