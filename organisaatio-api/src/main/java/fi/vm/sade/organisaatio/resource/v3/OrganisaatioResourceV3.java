package fi.vm.sade.organisaatio.resource.v3;

import fi.vm.sade.organisaatio.api.DateParam;
import fi.vm.sade.organisaatio.dto.v3.OrganisaatioGroupDTOV3;
import fi.vm.sade.organisaatio.dto.v3.OrganisaatioRDTOV3;
import fi.vm.sade.organisaatio.dto.v3.ResultRDTOV3;
import fi.vm.sade.organisaatio.dto.v4.OrganisaatioRDTOV4;
import fi.vm.sade.organisaatio.resource.dto.RyhmaCriteriaDtoV3;
import io.swagger.annotations.*;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

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
@Api(value = "/organisaatio/v3")
public interface OrganisaatioResourceV3 {

    @PostMapping(path = "/findbyoids", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8", consumes = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    @ApiOperation(
            value = "Hakee monta ei-poistettua organisaatiota kerralla syötetyille OIDeille (maksimissaan 1000)",
            response = OrganisaatioRDTOV4.class,
            responseContainer = "List")
    public List<OrganisaatioRDTOV3> findByOids(@ApiParam(value = "JSON-taulukko organisaatio OIDeja: [\"oid1\", \"oid2\", ...]",
            required = true) @RequestBody List<String> oids);

    @GetMapping(path = "/{oid}/children", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    @ApiOperation(
            value = "Hakee organisaation alla olevat organisaatiot",
            notes = "Operaatio palauttaa organisaation alla olevat organisaatiot.",
            response = OrganisaatioRDTOV4.class,
            responseContainer = "List")
    public List<OrganisaatioRDTOV3> children(
            @ApiParam(value = "Organisaation oid", required = true) @PathVariable String oid,
            @ApiParam(value = "Palaulautetaanko vastauksen mukana mahdollinen organisaation kuva (voi olla iso).",
                    required = false, defaultValue = "false") @RequestParam(defaultValue = "false") boolean includeImage) throws Exception;

    @GetMapping(path = "/ryhmat", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    @ApiOperation(
            value = "Hakee organisaation alla olevat ryhmät",
            notes = "Operaatio palauttaa organisaation alla olevat ryhmät.",
            response = OrganisaatioGroupDTOV3.class)
    public List<OrganisaatioGroupDTOV3> groups(RyhmaCriteriaDtoV3 criteria) throws Exception;

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    @ApiOperation(
            value = "Hakee yhden organisaation annetulla id:llä (id voi olla oid, y-tunnus, virastotunnus, oppilaitoskoodi tai toimipistekoodi).",
            notes = "Operaatio palauttaa id:n määrittämän organisaation tiedot.",
            response = OrganisaatioRDTOV3.class)
    public OrganisaatioRDTOV3 getOrganisaatioByOID(
            @ApiParam(value = "Organisaation oid, y-tunnus, virastotunnus, oppilaitoskoodi tai toimipistekoodi.",
                    required = true) @PathVariable String oid,
            @ApiParam(value = "Palaulautetaanko vastauksen mukana mahdollinen organisaation kuva (voi olla iso).",
                    required = false, defaultValue = "false") @RequestParam(defaultValue = "false") boolean includeImage);

    @PutMapping(path = "/{oid}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8", consumes = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    @ApiImplicitParams(
            @ApiImplicitParam(dataType = "java.io.File", name = "organisaatio",
                    value = "Organisaation tiedot json muodossa", paramType = "body"))
    @ApiOperation(
            value = "Päivittää oid:n määrittämän organisaation tiedot",
            notes = "Operaatio päivittää oid:n määrittämän organisaation tiedot.",
            response = ResultRDTOV3.class)
    public ResultRDTOV3 updateOrganisaatio(
            @ApiParam(value = "Organisaation oid", required = true) @PathVariable String oid,
            @ApiParam(access = "hidden") OrganisaatioRDTOV3 ordto);

    @DeleteMapping(path = "/{oid}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    @ApiOperation(
            value = "Poistaa oid:n määrittämän organisaation",
            notes = "Operaatio poistaa organisaation annetulla oid:llä.",
            response = String.class)
    public String deleteOrganisaatio(
            @ApiParam(value = "Organisaation oid", required = true) @PathVariable String oid);

    @PostMapping(path = "/", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8", consumes = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    @ApiImplicitParams(
            @ApiImplicitParam(dataType = "java.io.File", name = "organisaatio",
                    value = "Luotavan organisaation tiedot json muodossa", paramType = "body"))
    @ApiOperation(
            value = "Luo uuden organisaation",
            notes = "Operaatio luo uuden organisaation annetusta JSON:sta.",
            response = ResultRDTOV3.class)
    public ResultRDTOV3 newOrganisaatio(@ApiParam(access = "hidden") OrganisaatioRDTOV3 ordto);

    @GetMapping(path = "/muutetut", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    @ApiOperation(
            value = "Hakee organisaatioiden tiedot, joita muutettu annetun päivämäärän jälkeen",
            response = OrganisaatioRDTOV3.class,
            responseContainer = "List")
    public List<OrganisaatioRDTOV3> haeMuutetut(@ApiParam(value = "Muokattu jälkeen", required = true) @RequestParam DateParam lastModifiedSince,
                                                @ApiParam(value = "Palaulautetaanko vastauksen mukana mahdollinen organisaation kuva (voi olla iso).",
                                                        required = false, defaultValue = "false") @RequestParam(defaultValue = "false") boolean includeImage);

}
