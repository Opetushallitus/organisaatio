package fi.vm.sade.organisaatio.resource;

import fi.vm.sade.organisaatio.business.LisatietoService;
import fi.vm.sade.organisaatio.dto.LisatietotyyppiCreateDto;
import fi.vm.sade.organisaatio.dto.LisatietotyyppiDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.ws.rs.Path;
import java.util.Set;

@ApiIgnore
@RestController
@RequestMapping("/lisatieto")
@Api(value = "/lisatieto")
public class LisatietoResource {
    private LisatietoService lisatietoService;

    public LisatietoResource(LisatietoService organisaatioFindBusinessService) {
        this.lisatietoService = organisaatioFindBusinessService;
    }

    @GetMapping(path= "/lisatietotyypit", produces = "application/json;charset=UTF-8")
    @ApiOperation(value = "Hakee kaikki mahdolliset lisätiedot organisaatioille",
            response = String.class,
            responseContainer = "Set")
    public Set<String> haeLisatietotyypit() {
        return this.lisatietoService.getLisatietotyypit();
    }

    @GetMapping(path= "/{oid}/lisatietotyypit", produces = "application/json;charset=UTF-8")
    @ApiOperation(value = "Hakee sallitut lisätietotyypit organisaatiolle",
            response = String.class,
            responseContainer = "Set")
    public Set<String> haeLisatietotyypit(@ApiParam(value = "Organisaation oid", required = true) @PathVariable String oid) {
        return this.lisatietoService.getSallitutByOid(oid);
    }

    @PostMapping(path= "/lisatietotyyppi", produces = "text/plain;charset=UTF-8", consumes = "application/json;charset=UTF-8")
    @ApiOperation(value = "Luo uuden lisätietotyypin")
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    public String createLisatietotyyppi(LisatietotyyppiCreateDto lisatietotyyppiCreateDto) {
        return this.lisatietoService.create(lisatietotyyppiCreateDto);
    }

    @DeleteMapping(path= "/lisatietotyyppi/{nimi}")
    @Path("/lisatietotyyppi/{nimi}")
    @ApiOperation(value = "Poistaa lisätietotyypin")
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    public void poistaLisatietotyyppi(@ApiParam(value = "Lisätietotyypin nimi", required = true) @PathVariable String nimi) {
        this.lisatietoService.delete(nimi);
    }

    @GetMapping(path= "/lisatietotyyppi/{nimi}", produces = "application/json;charset=UTF-8")
    @ApiOperation(value = "Hakee lisätietotyypin tiedot nimellä")
    public LisatietotyyppiDto lisatietotyyppiNimella(@ApiParam(value = "Lisätietotyypin nimi", required = true) @PathVariable String nimi) {
        return this.lisatietoService.findByName(nimi);
    }
}
