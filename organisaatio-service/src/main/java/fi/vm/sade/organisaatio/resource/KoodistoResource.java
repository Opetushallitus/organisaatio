package fi.vm.sade.organisaatio.resource;

import fi.vm.sade.organisaatio.dto.v4.OrganisaatioSearchCriteriaDTOV4;
import fi.vm.sade.organisaatio.service.KoodistoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collection;

@Path("/koodisto")
@Api("/koodisto")
@Component("koodistoResource")
@Produces(MediaType.APPLICATION_JSON)
public class KoodistoResource {

    private final KoodistoService koodistoService;

    public KoodistoResource(KoodistoService koodistoService) {
        this.koodistoService = koodistoService;
    }

    @POST
    @Path("/sync/v4")
    @Consumes(MediaType.APPLICATION_JSON)
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.10.00000000001')")
    @ApiOperation("Lisää hakukriteerien mukaiset organisaatiot koodistosynkronointiin")
    public void addKoodistoSyncBy(OrganisaatioSearchCriteriaDTOV4 criteriaV4) {
        koodistoService.addKoodistoSyncBy(criteriaV4);
    }

    @GET
    @Path("/sync")
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.10.00000000001')")
    @ApiOperation("Listaa koodistosynkronoinnissa olevat organisaatiot")
    public Collection<String> listKoodistoSyncOids() {
        return koodistoService.listKoodistoSyncOids();
    }

    @PUT
    @Path("/sync/{oid}")
    @Consumes(MediaType.APPLICATION_JSON)
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.10.00000000001')")
    @ApiOperation("Lisää organisaation koodistosynkronointiin")
    public void addKoodistoSyncByOid(@PathParam("oid") String oid) {
        koodistoService.addKoodistoSyncByOid(oid);
    }

    @DELETE
    @Path("/sync/{oid}")
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.10.00000000001')")
    @ApiOperation("Poistaa organisaation koodistosynkronoinnista")
    public void removeKoodistoSyncByOid(@PathParam("oid") String oid) {
        koodistoService.removeKoodistoSyncByOid(oid);
    }

}
