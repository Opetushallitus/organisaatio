package fi.vm.sade.organisaatio.resource;

import fi.vm.sade.organisaatio.business.OrganisaatioKoodisto;
import fi.vm.sade.organisaatio.dto.Koodi;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Hidden
@RestController
@RequestMapping({"${server.internal.context-path}/koodisto"})
public class KoodistoInternalResource {
    private final OrganisaatioKoodisto organisaatioKoodisto;

    public KoodistoInternalResource(OrganisaatioKoodisto organisaatioKoodisto) {
        this.organisaatioKoodisto = organisaatioKoodisto;
    }

    @GetMapping("/{koodisto}/koodi")
    List<Koodi> getKoodi(@PathVariable OrganisaatioKoodisto.KoodistoUri koodisto,
                         @RequestParam(required = false) Optional<Integer> versio,
                         @RequestParam(required = false) Optional<Boolean> onlyValid
    ) {
        return organisaatioKoodisto.haeKoodit(koodisto, versio, onlyValid);
    }
}
