package fi.vm.sade.organisaatio.resource;

import fi.vm.sade.organisaatio.business.LisatietoService;
import fi.vm.sade.organisaatio.dto.LisatietotyyppiCreateDto;
import fi.vm.sade.organisaatio.dto.LisatietotyyppiDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Set;

@Path("/lisatieto")
@Api(value = "/lisatieto", description = "Lisätietoihin ja lisätietotyyppeihin liittyvät operaatiot")
@Controller("lisatietoResource")
public class LisatietoResource {
    private LisatietoService lisatietoService;

    public LisatietoResource(LisatietoService organisaatioFindBusinessService) {
        this.lisatietoService = organisaatioFindBusinessService;
    }

    @GET
    @Path("/lisatietotyypit")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiOperation(value = "Hakee kaikki mahdolliset lisätiedot organisaatioille",
            response = String.class,
            responseContainer = "Set")
    public Set<String> haeLisatietotyypit() {
        return this.lisatietoService.getLisatietotyypit();
    }

    @GET
    @Path("/{oid}/lisatietotyypit")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiOperation(value = "Hakee sallitut lisätietotyypit organisaatiolle",
            response = String.class,
            responseContainer = "Set")
    public Set<String> haeLisatietotyypit(@ApiParam(value = "Organisaation oid", required = true) @PathParam("oid") String oid) {
        return this.lisatietoService.getSallitutByOid(oid);
    }

    @POST
    @Path("/lisatietotyyppi")
    @Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiOperation(value = "Luo uuden lisätietotyypin")
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    public String createLisatietotyyppi(LisatietotyyppiCreateDto lisatietotyyppiCreateDto) {
        return this.lisatietoService.create(lisatietotyyppiCreateDto);
    }

    @DELETE
    @Path("/lisatietotyyppi/{nimi}")
    @ApiOperation(value = "Poistaa lisätietotyypin")
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    public void poistaLisatietotyyppi(@ApiParam(value = "Lisätietotyypin nimi", required = true) @PathParam("nimi") String nimi) {
        this.lisatietoService.delete(nimi);
    }

    @GET
    @Path("/{nimi}")
    @ApiOperation(value = "Hakee lisätietotyypin tiedot nimellä")
    public LisatietotyyppiDto lisatietotyyppiNimella(@ApiParam(value = "Lisätietotyypin nimi", required = true) @PathParam("nimi") String nimi) {
        return this.lisatietoService.findByName(nimi);
    }
}
