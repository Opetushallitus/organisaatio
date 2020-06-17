package fi.vm.sade.organisaatio.resource;

import fi.vm.sade.organisaatio.dto.v4.OrganisaatioSearchCriteriaDTOV4;
import fi.vm.sade.organisaatio.service.KoodistoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/koodisto")
@Api(value = "/koodisto")
public class KoodistoResource {

    private final KoodistoService koodistoService;

    public KoodistoResource(KoodistoService koodistoService) {
        this.koodistoService = koodistoService;
    }

    @PostMapping(path= "/sync/v4", consumes = "application/json")
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.10.00000000001')")
    @ApiOperation("Lisää hakukriteerien mukaiset organisaatiot koodistosynkronointiin")
    public void addKoodistoSyncBy(@RequestBody OrganisaatioSearchCriteriaDTOV4 criteriaV4) {
        koodistoService.addKoodistoSyncBy(criteriaV4);
    }

    @GetMapping(path="/sync", produces = "application/json")
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.10.00000000001')")
    @ApiOperation("Listaa koodistosynkronoinnissa olevat organisaatiot")
    public Collection<String> listKoodistoSyncOids() {
        return koodistoService.listKoodistoSyncOids();
    }

    @PutMapping(path="/sync/{oid}", consumes = "application/json")
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.10.00000000001')")
    @ApiOperation("Lisää organisaation koodistosynkronointiin")
    public void addKoodistoSyncByOid(@PathVariable String oid) {
        koodistoService.addKoodistoSyncByOid(oid);
    }

    @DeleteMapping(path="/sync/{oid}")
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.10.00000000001')")
    @ApiOperation("Poistaa organisaation koodistosynkronoinnista")
    public void removeKoodistoSyncByOid(@PathVariable String oid) {
        koodistoService.removeKoodistoSyncByOid(oid);
    }

}
