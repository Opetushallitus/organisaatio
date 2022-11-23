package fi.vm.sade.organisaatio.service;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.model.Organisaatio;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public class OrganisationHierarchyValidatorTypeTest {

    private static final String ROOT_OID = "1";
    private final OrganisationHierarchyValidator validator = new OrganisationHierarchyValidator(ROOT_OID);

    private static Stream<Arguments> parameters() throws IOException {
        try (Stream<String> rows = Files.lines(Paths.get("src/test/resources/OrganisationHierarchyValidator.csv"))) {
            return rows.reduce(new ArrayList<>(), new ParametersAccumulator<>(), (t, u) -> {
                throw new UnsupportedOperationException("Parallel streaming not implemented");
            }).stream();
        }
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void test(OrganisaatioTyyppi parent, OrganisaatioTyyppi child, boolean expected) {
        assertThat(validator.apply(new AbstractMap.SimpleEntry<>(getOrg(parent), getOrg(child)))).isEqualTo(expected);
    }

    private static class ParametersAccumulator<T extends List<Arguments>> implements BiFunction<T, String, T> {

        private List<OrganisaatioTyyppi> ylaorganisaatiotyypit;

        @Override
        public T apply(T parameters, String row) {
            if (ylaorganisaatiotyypit == null) {
                ylaorganisaatiotyypit = Arrays.stream(row.split("\\s+"))
                        .map(String::trim)
                        .filter(tyyppi -> !tyyppi.isEmpty())
                        .map(OrganisaatioTyyppi::valueOf)
                        .collect(toList());
                return parameters;
            }
            String[] cells = row.split("\\s+");
            OrganisaatioTyyppi childType = OrganisaatioTyyppi.valueOf(cells[0]);
            IntStream.range(1, cells.length).forEach(i -> {
                OrganisaatioTyyppi parentType = ylaorganisaatiotyypit.get(i - 1);
                parameters.add(Arguments.of(parentType, childType, Boolean.parseBoolean(cells[i])));
            });
            return parameters;
        }

    }

    private Organisaatio getOrg(OrganisaatioTyyppi... tyypit) {
        Organisaatio organisaatio = new Organisaatio();
        organisaatio.setTyypit(Arrays.stream(tyypit).map(OrganisaatioTyyppi::koodiValue).collect(Collectors.toSet()));
        return organisaatio;
    }

}
