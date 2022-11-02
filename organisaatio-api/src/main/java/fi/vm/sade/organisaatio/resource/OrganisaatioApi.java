package fi.vm.sade.organisaatio.resource;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.dto.OrganisaatioNimiDTO;
import fi.vm.sade.organisaatio.dto.OrganisaatioNimiUpdateDTO;
import fi.vm.sade.organisaatio.dto.v3.OrganisaatioGroupDTOV3;
import fi.vm.sade.organisaatio.dto.v4.*;
import fi.vm.sade.organisaatio.resource.dto.HakutoimistoDTO;
import fi.vm.sade.organisaatio.resource.dto.RyhmaCriteriaDtoV3;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.time.LocalDateTime;
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

@Validated
public interface OrganisaatioApi {

    @GetMapping(path = "/oids", produces = MediaType.APPLICATION_JSON_VALUE)
    List<String> oids(@RequestParam(required = false) OrganisaatioTyyppi type,
                        @RequestParam(name = "count", defaultValue = "0") @Min(0) int count,
                        @RequestParam(defaultValue = "0") @Min(0) int startIndex);

    @Operation(summary = "Hakee monta ei-poistettua organisaatiota kerralla syötetyille OIDeille (maksimissaan 1000)")
    @PostMapping(path = "/findbyoids", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    List<OrganisaatioRDTOV4> findByOids(@RequestBody Set<String> oids);

    @Operation(summary = "Hakee organisaation alla olevat organisaatiot")
    @GetMapping(path = "/{oid}/children", produces = MediaType.APPLICATION_JSON_VALUE)
    List<OrganisaatioRDTOV4> children(
            @Parameter(description = "Organisaation oid", required = true) @PathVariable String oid,
            @Parameter(description = "Palaulautetaanko vastauksen mukana mahdollinen organisaation kuva (voi olla iso).", deprecated = true) @RequestParam(defaultValue = "false") boolean includeImage
    );

    @Operation(summary = "Hakee yhden organisaation annetulla id:llä (id voi olla oid, y-tunnus, virastotunnus, oppilaitoskoodi tai toimipistekoodi).")
    @GetMapping(path = "/{oid}", produces = MediaType.APPLICATION_JSON_VALUE)
    OrganisaatioRDTOV4 getOrganisaatioByOID(
            @Parameter(description = "Organisaation oid, y-tunnus, virastotunnus, oppilaitoskoodi tai toimipistekoodi.", required = true) @PathVariable String oid,
            @Parameter(description = "Palaulautetaanko vastauksen mukana mahdollinen organisaation kuva (voi olla iso).", deprecated = true) @RequestParam(defaultValue = "false") boolean includeImage
    );

    @GetMapping(path = "/{oid}/parentoids", produces = MediaType.APPLICATION_JSON_VALUE)
    List<String> parentoids(@PathVariable String oid);

    @GetMapping(path = "/{oid}/childoids", produces = MediaType.APPLICATION_JSON_VALUE)
    List<String> childoids(@PathVariable String oid,
                           @RequestParam(defaultValue = "false") boolean rekursiivisesti,
                           @RequestParam(defaultValue = "true") boolean aktiiviset,
                           @RequestParam(defaultValue = "true") boolean suunnitellut,
                           @RequestParam(defaultValue = "true") boolean lakkautetut);

    @Operation(summary = "Päivittää oid:n määrittämän organisaation tiedot")
    @PutMapping(path = "/{oid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    ResultRDTOV4 updateOrganisaatio(
            @Parameter(description = "Organisaation oid", required = true) @PathVariable String oid,
            @RequestBody OrganisaatioRDTOV4 ordto
    );

    @Operation(summary = "Operaatio luo uuden organisaation annetusta JSON:sta.")
    @PostMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    ResultRDTOV4 newOrganisaatio(@RequestBody OrganisaatioRDTOV4 ordto);

    @Operation(summary = "Hakee organisaatioiden tiedot, joita muutettu annetun päivämäärän jälkeen")
    @GetMapping(path = "/muutetut", produces = MediaType.APPLICATION_JSON_VALUE)
    List<OrganisaatioRDTOV4> haeMuutetut(
            @Parameter(description = "Muokattu jälkeen", required = true) @NotNull @RequestParam LocalDateTime lastModifiedSince,
            @Parameter(description = "Palaulautetaanko vastauksen mukana mahdollinen organisaation kuva (voi olla iso).", deprecated = true) @RequestParam(defaultValue = "false") boolean includeImage,
            @Parameter(description = "Halutut organisaatiotyypit") @RequestParam(defaultValue = "") List<String> organizationType,
            @Parameter(description = "Rajataanko lakkautetut organisaatiot pois") @RequestParam(defaultValue = "false") boolean excludeDiscontinued
    );

    @GetMapping(path = "/muutetut/oid", produces = MediaType.APPLICATION_JSON_VALUE)
    List<String> haeMuutettujenOid(
            @Parameter(description = "Muokattu jälkeen", required = true) @NotNull @RequestParam LocalDateTime lastModifiedSince,
            @Parameter(description = "Halutut organisaatiotyypit") @RequestParam(required = false, defaultValue = "") List<OrganisaatioTyyppi> organisaatioTyypit,
            @Parameter(description = "Rajataanko lakkautetut organisaatiot pois") @RequestParam(required = false, defaultValue = "false") boolean excludeDiscontinued);

    @Operation(summary = "Hakee organisaation rakennehistorian.")
    @GetMapping(path = "/{oid}/historia", produces = MediaType.APPLICATION_JSON_VALUE)
    OrganisaatioHistoriaRDTOV4 getOrganizationHistory(
            @Parameter(description = "Organisaation oid", required = true) @PathVariable String oid
    );

    @GetMapping(path = "/hae", produces = MediaType.APPLICATION_JSON_VALUE)
    OrganisaatioHakutulosV4 searchOrganisaatiot(@ParameterObject() OrganisaatioSearchCriteriaDTOV4 hakuEhdot);


    @Operation(
            summary = "Hakee organisaatiot puurakenteena annetuilla hakuehdoilla",
            description = "Operaatio palauttaa hakuehtoja vastaavat organisaatiot puurakenteena. "
                    + "Hakuehtojen osuessa hierarkiassa ylemmän tason organisaatioon, "
                    + "palautetaan alemman tason organisaatio myös, siis puurakenne lehtiin asti."
                    + "Hakuehtojen osuessa hierarkiassa alemman tason organisaatioon, "
                    + "palautetaan puurakenne juureen asti (ellei hakuehdot sitä estä).")
    @GetMapping(path = "/hierarkia/hae", produces = MediaType.APPLICATION_JSON_VALUE)
    OrganisaatioHakutulosV4 searchOrganisaatioHierarkia(@ParameterObject() OrganisaatioSearchCriteriaDTOV4 hakuEhdot);

    @Operation(summary = "Palauttaa organisaation jälkeläiset")
    @GetMapping(path = "/{oid}/jalkelaiset", produces = MediaType.APPLICATION_JSON_VALUE)
    OrganisaatioHakutulosV4 findDescendants(
            @Parameter(description = "Organisaation oid", required = true) @PathVariable String oid
    );

    @Operation(summary = "Siirtää organisaatiota puussa toisen ylemmän organisaation alle tai yhdistää kaksi samanarvoista organisaatiota")
    @PutMapping(path = "/{oid}/organisaatiosuhde/{parentoid}", produces = MediaType.APPLICATION_JSON_VALUE)
    OrganisaatioRDTOV4 changeOrganisationRelationship(
            @Parameter(description = "Organisaation oid", required = true) @PathVariable("oid") String oid,
            @Parameter(description = "Uusi isäntäorganisaatio", required = true) @PathVariable("parentoid") String parentoid,
            @Parameter(description = "Sulautus", required = true) @RequestParam("merge") boolean merge,
            @Parameter(description = "Siirto päivämäärä, jos päivämäärää ei ole asetettu käytetään tätä päivämäärää", required = true) @RequestParam("moveDate") LocalDateTime moveDate
    );


    @Operation(summary = "Poistaa oid:n määrittämän organisaation")
    @DeleteMapping(path = "/{oid}")
    void deleteOrganisaatio(@Parameter(description = "Organisaation oid", required = true) @PathVariable("oid") String oid);

    @Operation(summary = "Palauttaa organisaation viimeinen päivittäjä.")
    @GetMapping(path = "/{oid}/paivittaja", produces = MediaType.APPLICATION_JSON_VALUE)
    OrganisaatioPaivittajaDTO getOrganisaatioPaivittaja(@Parameter(description = "Organisaation oid", required = true) @PathVariable("oid") String oid);

    // nimen muokkausta varten alla:
    @Operation(summary = "Operaatio luo uuden nimen organisaatiolle annetusta JSON:sta.")
    @PostMapping(path = "/{oid}/nimet", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    OrganisaatioNimiDTO newOrganisaatioNimi(@Parameter(description = "Organisaation oid", required = true) @PathVariable("oid") String oid, @RequestBody OrganisaatioNimiDTO nimidto);

    @Operation(summary = "Operaatio päivittää oid:n määrittämän organisaation nimen, jonka aikaisempi alkupäivämäärä on annettu date.")
    @PutMapping(path = "/{oid}/nimet", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    OrganisaatioNimiDTO updateOrganisaatioNimi(@Parameter(description = "Organisaation oid", required = true) @PathVariable("oid") String oid, @RequestBody OrganisaatioNimiUpdateDTO nimiUpdateDto);

    @Operation(summary = "Operaatio poistaa oid:n määrittämän organisaation nimen, jonka aikaisempi alkupäivämäärä on annettu date.")
    @DeleteMapping(path = "/{oid}/nimet")
    void deleteOrganisaatioNimi(@Parameter(description = "Organisaation oid", required = true) @PathVariable("oid") String oid, @RequestBody OrganisaatioNimiDTO nimidto);

    @Hidden
    @Operation(summary = "Operaatio asettaa organisaatiolle nykyhetken tarkastuspäivämääräksi ja palauttaa tarkastuksen aikaleiman.")
    @PutMapping(path = "/{oid}/tarkasta", produces = MediaType.APPLICATION_JSON_VALUE)
    Timestamp updateTarkastusPvm(@Parameter(description = "Organisaation oid", required = true) @PathVariable("oid") String oid);

    @GetMapping(path = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
    void authHello();

    @GetMapping(path = "/{oid}/nimet", produces = MediaType.APPLICATION_JSON_VALUE)
    List<OrganisaatioNimiDTO> getOrganisaatioNimet(@PathVariable("oid") String oid);

    @GetMapping(path = "/ryhmat", produces = MediaType.APPLICATION_JSON_VALUE)
    List<OrganisaatioGroupDTOV3> groups(RyhmaCriteriaDtoV3 criteria);

    @GetMapping(path = "/{oid}/hakutoimisto", produces = MediaType.APPLICATION_JSON_VALUE)
    HakutoimistoDTO hakutoimisto(@PathVariable("oid") String organisaatioOid);

}