package fi.vm.sade.organisaatio.resource.v3;

import fi.vm.sade.organisaatio.dto.v3.OrganisaatioGroupDTOV3;
import fi.vm.sade.organisaatio.dto.v3.OrganisaatioRDTOV3;
import fi.vm.sade.organisaatio.resource.dto.RyhmaCriteriaDtoV3;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * V3 REST services for Organisaatio.
 * <p>
 * Changes to V1 & V2:
 * <ul>
 * <li>ryhmatyypit & kayttoryhmat uses Koodisto</li>
 * <li>POST /organisaatio is used to create Organisaatio (was PUT)</li>
 * <li>PUT /organisaatio/{oid} is used to update Organisaatio (was POST)</li>
 * </ul>
 */
@Hidden
public interface OrganisaatioResourceV3 {

    @PostMapping(path = "/findbyoids", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    List<OrganisaatioRDTOV3> findByOids(@RequestBody List<String> oids);

    @GetMapping(path = "/{oid}/children", produces = MediaType.APPLICATION_JSON_VALUE)
    List<OrganisaatioRDTOV3> children(

            @PathVariable String oid,
            @RequestParam(defaultValue = "false") boolean includeImage) throws Exception;

    @GetMapping(path = "/ryhmat", produces = MediaType.APPLICATION_JSON_VALUE)
    List<OrganisaatioGroupDTOV3> groups(RyhmaCriteriaDtoV3 criteria) throws Exception;

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    OrganisaatioRDTOV3 getOrganisaatioByOID(
            @PathVariable("id") String oid,
            @RequestParam(defaultValue = "false") boolean includeImage);

    @GetMapping(path = "/muutetut", produces = MediaType.APPLICATION_JSON_VALUE)
    List<OrganisaatioRDTOV3> haeMuutetut(
            @RequestParam LocalDate lastModifiedSince,
            @RequestParam(defaultValue = "false") boolean includeImage);

}
