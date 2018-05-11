package fi.vm.sade.organisaatio.resource;

import fi.vm.sade.organisaatio.business.LisatietoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.stereotype.Controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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


}
