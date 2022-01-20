package fi.vm.sade.organisaatio.resource;

import fi.vm.sade.organisaatio.business.OrganisaatioKoodisto;
import fi.vm.sade.organisaatio.dto.Koodi;
import fi.vm.sade.organisaatio.dto.v4.OrganisaatioSearchCriteriaDTOV4;
import fi.vm.sade.organisaatio.service.KoodistoService;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
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
