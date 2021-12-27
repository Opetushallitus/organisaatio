package fi.vm.sade.organisaatio.resource;

import fi.vm.sade.organisaatio.api.DateParam;
import fi.vm.sade.organisaatio.dto.v4.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * V4 REST services for Organisaatio.
 * <p>
 * Changes to V3:
 * <ul>
 * <li>organisaatiotyypit as codeelement values</li>
 * <li>supports varhaiskasvatuksen toimipaikka typed organisations</li>
 * </ul>
 */
public interface OrganisaatioApi {

    @PostMapping(path = "/findbyoids", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Hakee monta ei-poistettua organisaatiota kerralla syötetyille OIDeille (maksimissaan 1000)")
    List<OrganisaatioRDTOV4> findByOids(
            @Parameter(description = "JSON-taulukko organisaatio OIDeja: [\"oid1\", \"oid2\", ...]", required = true) @RequestBody Set<String> oids
    );

    @GetMapping(path = "/{oid}/children", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Hakee organisaation alla olevat organisaatiot",
            description = "Operaatio palauttaa organisaation alla olevat organisaatiot.")
    List<OrganisaatioRDTOV4> children(
            @Parameter(description = "Organisaation oid", required = true) @PathVariable String oid,
            @Parameter(description = "Palaulautetaanko vastauksen mukana mahdollinen organisaation kuva (voi olla iso).") @RequestParam(defaultValue = "false") boolean includeImage
    );

    @GetMapping(path = "/{oid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Hakee yhden organisaation annetulla id:llä (id voi olla oid, y-tunnus, virastotunnus, oppilaitoskoodi tai toimipistekoodi).",
            description = "Operaatio palauttaa id:n määrittämän organisaation tiedot.")
    OrganisaatioRDTOV4 getOrganisaatioByOID(
            @Parameter(description = "Organisaation oid, y-tunnus, virastotunnus, oppilaitoskoodi tai toimipistekoodi.", required = true) @PathVariable String oid,
            @Parameter(description = "Palaulautetaanko vastauksen mukana mahdollinen organisaation kuva (voi olla iso).") @RequestParam(defaultValue = "false") boolean includeImage
    );

    @PutMapping(path = "/{oid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Parameter(name = "organisaatio", description = "Organisaation tiedot json muodossa")
    @Operation(
            summary = "Päivittää oid:n määrittämän organisaation tiedot",
            description = "Operaatio päivittää oid:n määrittämän organisaation tiedot.")
    ResultRDTOV4 updateOrganisaatio(
            @Parameter(description = "Organisaation oid", required = true) @PathVariable String oid,
            @Parameter(description = "hidden") @RequestBody OrganisaatioRDTOV4 ordto
    );

    @PostMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Parameter(name = "organisaatio", description = "Luotavan organisaation tiedot json muodossa")
    @Operation(
            summary = "Luo uuden organisaation",
            description = "Operaatio luo uuden organisaation annetusta JSON:sta.")
    ResultRDTOV4 newOrganisaatio(
            @Parameter(hidden = true) @RequestBody OrganisaatioRDTOV4 ordto
    );

    @GetMapping(path = "/muutetut", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Hakee organisaatioiden tiedot, joita muutettu annetun päivämäärän jälkeen")
    List<OrganisaatioRDTOV4> haeMuutetut(
            @Parameter(description = "Muokattu jälkeen", required = true) @RequestParam DateParam lastModifiedSince,
            @Parameter(description = "Palaulautetaanko vastauksen mukana mahdollinen organisaation kuva (voi olla iso).") @RequestParam(defaultValue = "false") boolean includeImage,
            @Parameter(description = "Halutut organisaatiotyypit") @RequestParam(defaultValue = "") List<String> organizationTypes,
            @Parameter(description = "Rajataanko lakkautetut organisaatiot pois") @RequestParam(defaultValue = "false") boolean excludeDiscontinued
    );

    @GetMapping(path = "/{oid}/historia", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Hakee organisaation rakennehistorian.",
            description = "Operaatio palauttaa oid:n määrittelemän organisaation rakennehistorian.")
    OrganisaatioHistoriaRDTOV4 getOrganizationHistory(
            @Parameter(description = "Organisaation oid", required = true) @PathVariable String oid
    );

    @GetMapping(path = "/hae", produces = MediaType.APPLICATION_JSON_VALUE)
    @Parameter(name = "searchStr", description = "Hakuteksti")
    @Parameter(name = "aktiiviset", description = "Aktiiviset organisaatiot mukaan hakutuloksiin", required = true)
    @Parameter(name = "suunnitellut", description = "Suunnitellut organisaatiot mukaan hakutuloksiin", required = true)
    @Parameter(name = "lakkautetut", description = "Lakkautetut organisaatiot mukaan hakutuloksiin", required = true)
    @Parameter(name = "yritysmuoto", description = "Haettavan organisaation yritysmuoto tai lista yritysmuodoista")
    @Parameter(name = "kunta", description = "Haettavan organisaation kunta tai lista kunnista")
    @Parameter(name = "organisaatiotyyppi", description = "Haettavan organisaation tyyppi koodiarvona")
    @Parameter(name = "oppilaitostyyppi", description = "Haettavan oppilaitoksen tyyppi tai lista tyypeistä")
    @Parameter(name = "kieli", description = "Haettavan organisaation kieli tai lista kielistä")
    @Parameter(name = "oidRestrictionList", description = "Lista sallituista organisaatioiden oid:stä")
    @Parameter(name = "oid", description = "Haku oid:lla. Hakuteksti jätetään huomioimatta jos oid on annettu.")
    @Parameter(name = "skipParents", description = "Jätetäänkö yläorganisaatiot pois hakutuloksista")
    @Operation(
            summary = "Hakee organisaatiot, jotka osuvat annetuihin hakuehtoihin",
            description = "Operaatio palauttaa vain hakuehtoja vastaavat organisaatiot.")
    OrganisaatioHakutulosV4 searchOrganisaatiot(
            @Parameter(hidden = true) OrganisaatioSearchCriteriaDTOV4 hakuEhdot
    );

    @GetMapping(path = "/hierarkia/hae", produces = MediaType.APPLICATION_JSON_VALUE)
    @Parameter(name = "searchStr", description = "Hakuteksti")
    @Parameter(name = "aktiiviset", description = "Aktiiviset organisaatiot mukaan hakutuloksiin", required = true)
    @Parameter(name = "suunnitellut", description = "Suunnitellut organisaatiot mukaan hakutuloksiin", required = true)
    @Parameter(name = "lakkautetut", description = "Lakkautetut organisaatiot mukaan hakutuloksiin", required = true)
    @Parameter(name = "kunta", description = "Haettavan organisaation kunta tai lista kunnista")
    @Parameter(name = "organisaatiotyyppi", description = "Haettavan organisaation tyyppi koodiarvona")
    @Parameter(name = "oppilaitostyyppi", description = "Haettavan oppilaitoksen tyyppi tai lista tyypeistä")
    @Parameter(name = "kieli", description = "Haettavan organisaation kieli tai lista kielistä")
    @Parameter(name = "oidRestrictionList", description = "Lista sallituista organisaatioiden oid:stä")
    @Parameter(name = "oid", description = "Haku oid:lla. Hakuteksti jätetään huomioimatta jos oid on annettu.")
    @Parameter(name = "skipParents", description = "Jätetäänkö yläorganisaatiot pois hakutuloksista")
    @Operation(
            summary = "Hakee organisaatiot puurakenteena annetuilla hakuehdoilla",
            description = "Operaatio palauttaa hakuehtoja vastaavat organisaatiot puurakenteena. "
                    + "Hakuehtojen osuessa hierarkiassa ylemmän tason organisaatioon, "
                    + "palautetaan alemman tason organisaatio myös, siis puurakenne lehtiin asti."
                    + "Hakuehtojen osuessa hierarkiassa alemman tason organisaatioon, "
                    + "palautetaan puurakenne juureen asti (ellei hakuehdot sitä estä).")
    OrganisaatioHakutulosV4 searchOrganisaatioHierarkia(
            @Parameter(hidden = true) OrganisaatioSearchCriteriaDTOV4 hakuEhdot
    );

    @GetMapping(path = "/{oid}/jalkelaiset", produces = MediaType.APPLICATION_JSON_VALUE)
    OrganisaatioHakutulosV4 findDescendants(
            @Parameter(description = "Organisaation oid", required = true) @PathVariable String oid
    );

    @PutMapping(path = "/{oid}/organisaatiosuhde/{parentoid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Siirtää organisaatiota puussa toisen ylemmän organisaation alle tai yhdistää kaksi samanarvoista organisaatiota")
    OrganisaatioRDTOV4 changeOrganisationRelationship(
            @Parameter(description = "Organisaation oid", required = true) @PathVariable("oid") String oid,
            @Parameter(description = "Uusi isäntäorganisaatio", required = true) @PathVariable("parentoid") String parentoid,
            @Parameter(description = "Sulautus", required = true) @RequestParam("merge") boolean merge,
            @Parameter(description = "Siirto päivämäärä, jos päivämäärää ei ole asetettu käytetään tätä päivämäärää", required = true) @RequestParam("moveDate") DateParam moveDate
    );

    @DeleteMapping(path = "/{oid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Poistaa oid:n määrittämän organisaation",
            description = "Operaatio poistaa organisaation annetulla oid:llä.")
    void deleteOrganisaatio(
            @Parameter(description = "Organisaation oid", required = true) @PathVariable String oid
    );
    @GetMapping(path = "/{oid}/paivittaja", produces = MediaType.APPLICATION_JSON_VALUE)
    OrganisaatioPaivittajaDTO getOrganisaatioPaivittaja(@PathVariable("oid") String oid);

}
