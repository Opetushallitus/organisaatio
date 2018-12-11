package fi.vm.sade.organisaatio.service;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.model.Organisaatio;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class OrganisationHierarchyValidatorTypeTest {

    private static final String ROOT_OID = "1";

    private OrganisationHierarchyValidator validator;
    private final OrganisaatioTyyppi parent;
    private final OrganisaatioTyyppi child;
    private final boolean expected;

    public OrganisationHierarchyValidatorTypeTest(OrganisaatioTyyppi parent, OrganisaatioTyyppi child, boolean expected) {
        this.validator = new OrganisationHierarchyValidator(ROOT_OID);
        this.parent = parent;
        this.child = child;
        this.expected = expected;
    }

    @Parameterized.Parameters(name = "parent:{0} + child:{1} -> {2}")
    public static Collection<Object[]> parameters() throws IOException {
        try (Stream<String> rows = Files.lines(Paths.get("src/test/resources/OrganisationHierarchyValidator.csv"))) {
            return rows.reduce(new ArrayList<>(), new ParametersAccumulator<>(), (t, u) -> {
                throw new UnsupportedOperationException("Parallel streaming not implemented");
            });
        }
    }

    @Test
    public void test() {
        assertThat(validator.apply(new AbstractMap.SimpleEntry<>(getOrg(parent), getOrg(child)))).isEqualTo(expected);
    }

    private static class ParametersAccumulator<T extends Collection<Object[]>> implements BiFunction<T, String, T> {

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
                parameters.add(new Object[]{parentType, childType, Boolean.parseBoolean(cells[i])});
            });
            return parameters;
        }

    }

    private Organisaatio getOrg(OrganisaatioTyyppi... tyypit) {
        Organisaatio organisaatio = new Organisaatio();
        organisaatio.setTyypit(Arrays.stream(tyypit).map(OrganisaatioTyyppi::koodiValue).collect(toList()));
        return organisaatio;
    }

}
