package fi.vm.sade.organisaatio.service;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.model.Organisaatio;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class OrganisationHierarchyValidatorSecondLevelTest {

    private static final String ROOT_OID = "1";

    private final OrganisationHierarchyValidator validator = new OrganisationHierarchyValidator(ROOT_OID);
    private final Organisaatio root;

    public OrganisationHierarchyValidatorSecondLevelTest() {
        this.root = new Organisaatio();
        this.root.setOid(ROOT_OID);
    }

    private static Stream<Arguments> parameters() {
        return Stream.of(
                Arguments.of(OrganisaatioTyyppi.KOULUTUSTOIMIJA, true),
                Arguments.of(OrganisaatioTyyppi.OPPILAITOS, false),
                Arguments.of(OrganisaatioTyyppi.TOIMIPISTE, false),
                Arguments.of(OrganisaatioTyyppi.OPPISOPIMUSTOIMIPISTE, false),
                Arguments.of(OrganisaatioTyyppi.MUU_ORGANISAATIO, true),
                Arguments.of(OrganisaatioTyyppi.VARHAISKASVATUKSEN_JARJESTAJA, true),
                Arguments.of(OrganisaatioTyyppi.VARHAISKASVATUKSEN_TOIMIPAIKKA, false),
                Arguments.of(OrganisaatioTyyppi.TYOELAMAJARJESTO, true),
                Arguments.of(OrganisaatioTyyppi.KUNTA, true)
        );
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void test(OrganisaatioTyyppi tyyppi, boolean expected) {
        assertThat(validator.apply(new AbstractMap.SimpleEntry<>(root, getOrg(tyyppi)))).isEqualTo(expected);
    }

    private Organisaatio getOrg(OrganisaatioTyyppi... tyypit) {
        Organisaatio organisaatio = new Organisaatio();
        organisaatio.setTyypit(Arrays.stream(tyypit).map(OrganisaatioTyyppi::koodiValue).collect(Collectors.toSet()));
        return organisaatio;
    }

}
