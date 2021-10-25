package fi.vm.sade.organisaatio.resource;

import fi.vm.sade.organisaatio.business.OrganisaatioKoodisto;
import fi.vm.sade.organisaatio.dto.Koodi;
import fi.vm.sade.organisaatio.dto.v4.OrganisaatioSearchCriteriaDTOV4;
import fi.vm.sade.organisaatio.service.KoodistoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@ApiIgnore
@RestController
@RequestMapping("/koodisto")
@Api(value = "/koodisto")
public class KoodistoResource {

    private final KoodistoService koodistoService;
    private final OrganisaatioKoodisto organisaatioKoodisto;

    public KoodistoResource(KoodistoService koodistoService, OrganisaatioKoodisto organisaatioKoodisto) {
        this.koodistoService = koodistoService;
        this.organisaatioKoodisto = organisaatioKoodisto;
    }

    @PostMapping(path = "/sync/v4", consumes = "application/json")
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.10.00000000001')")
    @ApiOperation("Lisää hakukriteerien mukaiset organisaatiot koodistosynkronointiin")
    public void addKoodistoSyncBy(@RequestBody OrganisaatioSearchCriteriaDTOV4 criteriaV4) {
        koodistoService.addKoodistoSyncBy(criteriaV4);
    }

    @GetMapping(path = "/sync", produces = "application/json")
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.10.00000000001')")
    @ApiOperation("Listaa koodistosynkronoinnissa olevat organisaatiot")
    public Collection<String> listKoodistoSyncOids() {
        return koodistoService.listKoodistoSyncOids();
    }

    @PutMapping(path = "/sync/{oid}", consumes = "application/json")
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.10.00000000001')")
    @ApiOperation("Lisää organisaation koodistosynkronointiin")
    public void addKoodistoSyncByOid(@PathVariable String oid) {
        koodistoService.addKoodistoSyncByOid(oid);
    }

    @DeleteMapping(path = "/sync/{oid}")
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.10.00000000001')")
    @ApiOperation("Poistaa organisaation koodistosynkronoinnista")
    public void removeKoodistoSyncByOid(@PathVariable String oid) {
        koodistoService.removeKoodistoSyncByOid(oid);
    }

    @GetMapping("/{koodisto}/koodi")
    List<Koodi> getKoodi(@PathVariable OrganisaatioKoodisto.KoodistoUri koodisto,
                         @RequestParam(required = false) Optional<Integer> versio,
                         @RequestParam(required = false) Optional<Boolean> onlyValid
    ) {
        return organisaatioKoodisto.haeKoodit(koodisto, versio, onlyValid);
    }

}
