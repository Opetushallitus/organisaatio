package fi.vm.sade.organisaatio.resource;

import fi.vm.sade.organisaatio.dto.v4.OrganisaatioSearchCriteriaDTOV4;
import fi.vm.sade.organisaatio.service.KoodistoService;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@Hidden
@RestController
@RequestMapping({"${server.rest.context-path}/koodisto"})
public class KoodistoResource {

    private final KoodistoService koodistoService;

    public KoodistoResource(KoodistoService koodistoService) {
        this.koodistoService = koodistoService;
    }

    @PostMapping(path = "/sync/v4", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.10.00000000001')")
    public void addKoodistoSyncBy(@RequestBody OrganisaatioSearchCriteriaDTOV4 criteriaV4) {
        koodistoService.addKoodistoSyncBy(criteriaV4);
    }

    @GetMapping(path = "/sync", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.10.00000000001')")
    public Collection<String> listKoodistoSyncOids() {
        return koodistoService.listKoodistoSyncOids();
    }

    @PutMapping(path = "/sync/{oid}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.10.00000000001')")
    public void addKoodistoSyncByOid(@PathVariable String oid) {
        koodistoService.addKoodistoSyncByOid(oid);
    }

    @DeleteMapping(path = "/sync/{oid}")
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.10.00000000001')")
    public void removeKoodistoSyncByOid(@PathVariable String oid) {
        koodistoService.removeKoodistoSyncByOid(oid);
    }
}