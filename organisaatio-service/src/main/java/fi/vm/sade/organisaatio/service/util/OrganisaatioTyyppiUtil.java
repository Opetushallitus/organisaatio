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
        tmp.put(OrganisaatioTyyppi.KOULUTUSTOIMIJA.koodiValue(), newHashSet(OrganisaatioTyyppi.KOULUTUSTOIMIJA,
                OrganisaatioTyyppi.OPPILAITOS, OrganisaatioTyyppi.TOIMIPISTE, OrganisaatioTyyppi.OPPISOPIMUSTOIMIPISTE));
        tmp.put(OrganisaatioTyyppi.OPPILAITOS.koodiValue(), newHashSet(OrganisaatioTyyppi.OPPILAITOS,
                OrganisaatioTyyppi.TOIMIPISTE, OrganisaatioTyyppi.OPPISOPIMUSTOIMIPISTE));
        tmp.put(OrganisaatioTyyppi.TOIMIPISTE.koodiValue(), newHashSet(OrganisaatioTyyppi.TOIMIPISTE,
                OrganisaatioTyyppi.OPPISOPIMUSTOIMIPISTE));
        tmp.put(OrganisaatioTyyppi.OPPISOPIMUSTOIMIPISTE.koodiValue(), newHashSet(OrganisaatioTyyppi.TOIMIPISTE,
                OrganisaatioTyyppi.OPPISOPIMUSTOIMIPISTE));
        tmp.put(OrganisaatioTyyppi.MUU_ORGANISAATIO.koodiValue(), newHashSet(OrganisaatioTyyppi.MUU_ORGANISAATIO));
        tmp.put(OrganisaatioTyyppi.RYHMA.koodiValue(), newHashSet(OrganisaatioTyyppi.RYHMA));
        tmp.put(OrganisaatioTyyppi.VARHAISKASVATUKSEN_JARJESTAJA.koodiValue(), newHashSet(
                OrganisaatioTyyppi.VARHAISKASVATUKSEN_JARJESTAJA, OrganisaatioTyyppi.VARHAISKASVATUKSEN_TOIMIPAIKKA));
        tmp.put(OrganisaatioTyyppi.VARHAISKASVATUKSEN_TOIMIPAIKKA.koodiValue(), newHashSet(OrganisaatioTyyppi.VARHAISKASVATUKSEN_TOIMIPAIKKA));
        tmp.put(OrganisaatioTyyppi.TYOELAMAJARJESTO.koodiValue(), newHashSet(OrganisaatioTyyppi.TYOELAMAJARJESTO));
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
        return Stream.of(organisaatiotyypit).map(OrganisaatioTyyppi::koodiValue).collect(toSet());
    }

}
