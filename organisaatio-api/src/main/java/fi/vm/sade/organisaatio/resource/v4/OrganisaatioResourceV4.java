package fi.vm.sade.organisaatio.resource.v4;

import fi.vm.sade.organisaatio.api.DateParam;
import fi.vm.sade.organisaatio.dto.v4.*;
import io.swagger.annotations.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * V4 REST services for Organisaatio.
 *
 * Changes to V3:
 * <ul>
 * <li>organisaatiotyypit as codeelement values</li>
 * <li>supports varhaiskasvatuksen toimipaikka typed organisations</li>
 * </ul>
 */
@Api(value = "/organisaatio/v4")
public interface OrganisaatioResourceV4 {

    @PostMapping(path = "/findbyoids", produces = "application/json;charset=UTF-8", consumes = "application/json;charset=UTF-8")
    @ApiOperation(
            value = "Hakee monta ei-poistettua organisaatiota kerralla syötetyille OIDeille (maksimissaan 1000)",
            response = OrganisaatioRDTOV4.class,
            responseContainer = "List")
    List<OrganisaatioRDTOV4> findByOids(@ApiParam(value = "JSON-taulukko organisaatio OIDeja: [\"oid1\", \"oid2\", ...]",
            required = true)@RequestBody Set<String> oids);

    @GetMapping(path = "/{oid}/children", produces = "application/json;charset=UTF-8")
    @ApiOperation(
            value = "Hakee organisaation alla olevat organisaatiot",
            notes = "Operaatio palauttaa organisaation alla olevat organisaatiot.",
            response = OrganisaatioRDTOV4.class,
            responseContainer = "List")
    List<OrganisaatioRDTOV4> children(
            @ApiParam(value = "Organisaation oid", required = true) @PathVariable String oid,
            @ApiParam(value = "Palaulautetaanko vastauksen mukana mahdollinen organisaation kuva (voi olla iso).",
                    defaultValue = "false") @RequestParam(defaultValue = "false") boolean includeImage) throws Exception;


    @GetMapping(path = "/{oid}", produces = "application/json;charset=UTF-8")
    @ApiOperation(
            value = "Hakee yhden organisaation annetulla id:llä (id voi olla oid, y-tunnus, virastotunnus, oppilaitoskoodi tai toimipistekoodi).",
            notes = "Operaatio palauttaa id:n määrittämän organisaation tiedot.",
            response = OrganisaatioRDTOV4.class)
    OrganisaatioRDTOV4 getOrganisaatioByOID(
            @ApiParam(value = "Organisaation oid, y-tunnus, virastotunnus, oppilaitoskoodi tai toimipistekoodi.",
                    required = true) @PathVariable String oid,
            @ApiParam(value = "Palaulautetaanko vastauksen mukana mahdollinen organisaation kuva (voi olla iso).",
                    defaultValue = "false") @RequestParam(defaultValue = "false") boolean includeImage);

    @PutMapping(path = "/{oid}", produces = "application/json;charset=UTF-8", consumes="application/json;charset=UTF-8")
    @ApiImplicitParams(
            @ApiImplicitParam(dataType = "java.io.File", name = "organisaatio",
                    value = "Organisaation tiedot json muodossa", paramType = "body"))
    @ApiOperation(
            value = "Päivittää oid:n määrittämän organisaation tiedot",
            notes = "Operaatio päivittää oid:n määrittämän organisaation tiedot.",
            response = OrganisaatioRDTOV4.class)
    ResultRDTOV4 updateOrganisaatio(
            @ApiParam(value = "Organisaation oid", required = true) @PathVariable String oid,
            @ApiParam(access = "hidden") @RequestBody OrganisaatioRDTOV4 ordto);

    @DeleteMapping(path = "/{oid}", produces = "application/json;charset=UTF-8")
    @ApiOperation(
            value = "Poistaa oid:n määrittämän organisaation",
            notes = "Operaatio poistaa organisaation annetulla oid:llä.",
            response = String.class)
    String deleteOrganisaatio(@ApiParam(value = "Organisaation oid", required = true) @PathVariable String oid);


    @PostMapping(path = "/", produces = "application/json;charset=UTF-8", consumes = "application/json;charset=UTF-8")
    @ApiImplicitParams(
            @ApiImplicitParam(dataType = "java.io.File", name = "organisaatio",
                    value = "Luotavan organisaation tiedot json muodossa", paramType = "body"))
    @ApiOperation(
            value = "Luo uuden organisaation",
            notes = "Operaatio luo uuden organisaation annetusta JSON:sta.",
            response = OrganisaatioRDTOV4.class)
    ResultRDTOV4 newOrganisaatio(@ApiParam(access = "hidden") @RequestBody OrganisaatioRDTOV4 ordto);

    @GetMapping(path = "/muutetut", produces = "application/json;charset=UTF-8")
    @ApiOperation(
            value = "Hakee organisaatioiden tiedot, joita muutettu annetun päivämäärän jälkeen",
            response = OrganisaatioRDTOV4.class,
            responseContainer = "List")
    List<OrganisaatioRDTOV4> haeMuutetut(
            @ApiParam(
                value = "Muokattu jälkeen", required = true) @RequestParam DateParam lastModifiedSince,
            @ApiParam(
                value = "Palaulautetaanko vastauksen mukana mahdollinen organisaation kuva (voi olla iso).",
                defaultValue = "false") @RequestParam(defaultValue = "false") boolean includeImage,
            @ApiParam(
                value = "Halutut organisaatiotyypit") @RequestParam List<String> organizationType,
            @ApiParam(
                value = "Rajataanko lakkautetut organisaatiot pois",
                defaultValue = "false") @RequestParam(defaultValue = "false") boolean excludeDiscontinued);

    @GetMapping(path = "/{oid}/historia", produces = "application/json;charset=UTF-8")
    @ApiOperation(
            value = "Hakee organisaation rakennehistorian.",
            notes = "Operaatio palauttaa oid:n määrittelemän organisaation rakennehistorian.",
            response = OrganisaatioHistoriaRDTOV4.class)
    OrganisaatioHistoriaRDTOV4 getOrganizationHistory(@ApiParam(value = "Organisaation oid", required = true) @PathVariable String oid) throws Exception;

    @GetMapping(path = "/hae", produces = "application/json;charset=UTF-8")
    @ApiImplicitParams({
            @ApiImplicitParam(dataType = "String",  name = "searchStr", value = "Hakuteksti", paramType = "query"),
            @ApiImplicitParam(dataType = "boolean", name = "aktiiviset", value = "Aktiiviset organisaatiot mukaan hakutuloksiin", paramType = "query", required = true),
            @ApiImplicitParam(dataType = "boolean", name = "suunnitellut", value = "Suunnitellut organisaatiot mukaan hakutuloksiin", paramType = "query", required = true),
            @ApiImplicitParam(dataType = "boolean", name = "lakkautetut", value = "Lakkautetut organisaatiot mukaan hakutuloksiin", paramType = "query", required = true, defaultValue = "false"),
            @ApiImplicitParam(dataType = "String", allowMultiple = true,  name = "yritysmuoto", value = "Haettavan organisaation yritysmuoto tai lista yritysmuodoista", paramType = "query"),
            @ApiImplicitParam(dataType = "String", allowMultiple = true,  name = "kunta", value = "Haettavan organisaation kunta tai lista kunnista", paramType = "query"),
            @ApiImplicitParam(dataType = "String",  name = "organisaatiotyyppi", value = "Haettavan organisaation tyyppi koodiarvona", paramType = "query"),
            @ApiImplicitParam(dataType = "String", allowMultiple = true,  name = "oppilaitostyyppi", value = "Haettavan oppilaitoksen tyyppi tai lista tyypeistä", paramType = "query"),
            @ApiImplicitParam(dataType = "String", allowMultiple = true,  name = "kieli", value = "Haettavan organisaation kieli tai lista kielistä", paramType = "query"),
            @ApiImplicitParam(dataType = "String", allowMultiple = true,  name = "oidResctrictionList", value = "Lista sallituista organisaatioiden oid:stä", paramType = "query"),
            @ApiImplicitParam(dataType = "String",  name = "oid", value = "Haku oid:lla. Hakuteksti jätetään huomioimatta jos oid on annettu.", paramType = "query"),
            @ApiImplicitParam(dataType = "boolean", name = "skipParents", value = "Jätetäänkö yläorganisaatiot pois hakutuloksista", paramType = "query")})
    @ApiOperation(
            value = "Hakee organisaatiot, jotka osuvat annetuihin hakuehtoihin",
            notes = "Operaatio palauttaa vain hakuehtoja vastaavat organisaatiot.",
            response = OrganisaatioHakutulosV4.class)
    OrganisaatioHakutulosV4 searchOrganisaatiot(@ApiParam(access = "hidden")
                                                             OrganisaatioSearchCriteriaDTOV4 hakuEhdot);

    @GetMapping(path = "/hierarkia/hae", produces = "application/json;charset=UTF-8")
    @ApiImplicitParams({
            @ApiImplicitParam(dataType = "String",  name = "searchStr", value = "Hakuteksti", paramType = "query"),
            @ApiImplicitParam(dataType = "boolean", name = "aktiiviset", value = "Aktiiviset organisaatiot mukaan hakutuloksiin", paramType = "query", required = true),
            @ApiImplicitParam(dataType = "boolean", name = "suunnitellut", value = "Suunnitellut organisaatiot mukaan hakutuloksiin", paramType = "query", required = true),
            @ApiImplicitParam(dataType = "boolean", name = "lakkautetut", value = "Lakkautetut organisaatiot mukaan hakutuloksiin", paramType = "query", required = true, defaultValue = "false"),
            @ApiImplicitParam(dataType = "String", allowMultiple = true,  name = "kunta", value = "Haettavan organisaation kunta tai lista kunnista", paramType = "query"),
            @ApiImplicitParam(dataType = "String",  name = "organisaatiotyyppi", value = "Haettavan organisaation tyyppi koodiarvona", paramType = "query"),
            @ApiImplicitParam(dataType = "String", allowMultiple = true,  name = "oppilaitostyyppi", value = "Haettavan oppilaitoksen tyyppi tai lista tyypeistä", paramType = "query"),
            @ApiImplicitParam(dataType = "String", allowMultiple = true,  name = "kieli", value = "Haettavan organisaation kieli tai lista kielistä", paramType = "query"),
            @ApiImplicitParam(dataType = "String", allowMultiple = true,  name = "oidResctrictionList", value = "Lista sallituista organisaatioiden oid:stä", paramType = "query"),
            @ApiImplicitParam(dataType = "String",  name = "oid", value = "Haku oid:lla. Hakuteksti jätetään huomioimatta jos oid on annettu.", paramType = "query"),
            @ApiImplicitParam(dataType = "boolean", name = "skipParents", value = "Jätetäänkö yläorganisaatiot pois hakutuloksista", paramType = "query")})
    @ApiOperation(
            value = "Hakee organisaatiot puurakenteena annetuilla hakuehdoilla",
            notes = "Operaatio palauttaa hakuehtoja vastaavat organisaatiot puurakenteena. "
                    + "Hakuehtojen osuessa hierarkiassa ylemmän tason organisaatioon, "
                    + "palautetaan alemman tason organisaatio myös, siis puurakenne lehtiin asti."
                    + "Hakuehtojen osuessa hierarkiassa alemman tason organisaatioon, "
                    + "palautetaan puurakenne juureen asti (ellei hakuehdot sitä estä).",
            response = OrganisaatioHakutulosV4.class)
    OrganisaatioHakutulosV4 searchOrganisaatioHierarkia(@ApiParam(access = "hidden")
                                                                     OrganisaatioSearchCriteriaDTOV4 hakuEhdot);

}
