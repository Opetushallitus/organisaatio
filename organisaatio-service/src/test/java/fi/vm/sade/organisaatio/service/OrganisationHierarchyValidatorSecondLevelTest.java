package fi.vm.sade.organisaatio.service;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.model.Organisaatio;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class OrganisationHierarchyValidatorSecondLevelTest {

    private static final String ROOT_OID = "1";

    private final OrganisationHierarchyValidator validator;
    private final Organisaatio root;
    private final OrganisaatioTyyppi tyyppi;
    private final boolean expected;

    public OrganisationHierarchyValidatorSecondLevelTest(OrganisaatioTyyppi tyyppi, boolean expected) {
        this.validator = new OrganisationHierarchyValidator(ROOT_OID);
        this.root = new Organisaatio();
        this.root.setOid(ROOT_OID);
        this.tyyppi = tyyppi;
        this.expected = expected;
    }

    @Parameterized.Parameters(name = "parent:root + child:{0} -> {1}")
    public static Collection<Object[]> parameters() throws IOException {
        return asList(new Object[][] {
                {OrganisaatioTyyppi.KOULUTUSTOIMIJA, true},
                {OrganisaatioTyyppi.OPPILAITOS, false},
                {OrganisaatioTyyppi.TOIMIPISTE, false},
                {OrganisaatioTyyppi.OPPISOPIMUSTOIMIPISTE, false},
                {OrganisaatioTyyppi.MUU_ORGANISAATIO, true},
                {OrganisaatioTyyppi.VARHAISKASVATUKSEN_JARJESTAJA, true},
                {OrganisaatioTyyppi.VARHAISKASVATUKSEN_TOIMIPAIKKA, false},
                {OrganisaatioTyyppi.TYOELAMAJARJESTO, true},
        });
    }

    @Test
    public void test() {
        assertThat(validator.apply(new AbstractMap.SimpleEntry<>(root, getOrg(tyyppi)))).isEqualTo(expected);
    }

    private Organisaatio getOrg(OrganisaatioTyyppi... tyypit) {
        Organisaatio organisaatio = new Organisaatio();
        organisaatio.setTyypit(Arrays.stream(tyypit).map(OrganisaatioTyyppi::koodiValue).collect(toList()));
        return organisaatio;
    }

}
