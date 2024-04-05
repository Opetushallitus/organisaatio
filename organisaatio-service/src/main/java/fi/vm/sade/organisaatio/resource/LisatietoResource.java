package fi.vm.sade.organisaatio.resource;

import fi.vm.sade.organisaatio.business.LisatietoService;
import fi.vm.sade.organisaatio.dto.LisatietotyyppiCreateDto;
import fi.vm.sade.organisaatio.dto.LisatietotyyppiDto;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@Hidden
@RestController
@RequestMapping({"${server.internal.context-path}/lisatieto", "${server.rest.context-path}/lisatieto"})
public class LisatietoResource {
    private LisatietoService lisatietoService;

    public LisatietoResource(LisatietoService organisaatioFindBusinessService) {
        this.lisatietoService = organisaatioFindBusinessService;
    }

    @GetMapping(path = "/lisatietotyypit", produces = MediaType.APPLICATION_JSON_VALUE)
    public Set<String> haeLisatietotyypit() {
        return this.lisatietoService.getLisatietotyypit();
    }

    @GetMapping(path = "/{oid}/lisatietotyypit", produces = MediaType.APPLICATION_JSON_VALUE)
    public Set<String> haeLisatietotyypit(@PathVariable String oid) {
        return this.lisatietoService.getSallitutByOid(oid);
    }

    @PostMapping(path = "/lisatietotyyppi", produces = MediaType.TEXT_PLAIN_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    public String createLisatietotyyppi(LisatietotyyppiCreateDto lisatietotyyppiCreateDto) {
        return this.lisatietoService.create(lisatietotyyppiCreateDto);
    }

    @DeleteMapping(path = "/lisatietotyyppi/{nimi}")
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    public void poistaLisatietotyyppi(@PathVariable String nimi) {
        this.lisatietoService.delete(nimi);
    }

    @GetMapping(path = "/lisatietotyyppi/{nimi}", produces = MediaType.APPLICATION_JSON_VALUE)
    public LisatietotyyppiDto lisatietotyyppiNimella(@PathVariable String nimi) {
        return this.lisatietoService.findByName(nimi);
    }
}
