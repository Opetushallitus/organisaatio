package fi.vm.sade.organisaatio.dto.mapping;

import fi.vm.sade.organisaatio.dto.Koodi;

import java.util.function.Function;

public class KoodiToUriVersioMapper implements Function<Koodi, String> {

    @Override
    public String apply(Koodi koodi) {
        return koodi.getUri() + "#" + koodi.getVersio();
    }

}
