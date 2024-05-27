package fi.vm.sade.organisaatio.resource;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.business.OrganisaatioBusinessService;
import fi.vm.sade.organisaatio.business.OrganisaatioYtjService;
import fi.vm.sade.organisaatio.client.KayttooikeusClient;
import fi.vm.sade.organisaatio.dto.v4.OrganisaatioRDTOV4;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.VardaRekisterointi;
import fi.vm.sade.organisaatio.repository.OrganisaatioRepository;
import fi.vm.sade.organisaatio.resource.dto.ApiErrorDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Organisaatioiden rekisteröinti")
@RestController
@RequestMapping("${server.api.context-path}/rekisterointi")
@RequiredArgsConstructor
@Slf4j
public class RekisterointiResource {
    private final ConversionService conversionService;
    private final KayttooikeusClient kayttooikeusClient;
    private final OrganisaatioRepository organisaatioRepository;
    private final OrganisaatioBusinessService organisaatioBusinessService;
    private final OrganisaatioYtjService organisaatioYtjService;

    @Value("${varda-rekisterointi.kayttooikeusryhma-id}")
    private Long vardaKayttooikeusryhmaId;

    @Operation(summary = "Varda-organisaation rekisteröinti", description = """
            Rekisteröi uuden varhaiskasvatuksen järjestäjän YTJ-tietojen perusteella. Jos Y-tunnuksella löytyy jo
            organisaatio, sille asetetaan tarvittaessa tyypiksi varhaiskasvatuksen järjestäjä.

            Organisaation luonnin tai päivityksen jälkeen kutsutaan annettu pääkäyttäjä.
            """)
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Rekisteröinti onnistui."),
                            @ApiResponse(responseCode = "409", description = "Organisaatio on lopetettu YTJ:n mukaan.",
                                content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorDTO.class))}),
                            @ApiResponse(responseCode = "503", description = "Organisaation tietojen haku YTJ:stä epäonnistui.",
                                content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorDTO.class))}) })
    @PostMapping(value = "/varda", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ROLE_APP_VARDA_REKISTEROINTI')")
    public OrganisaatioRDTOV4 vardaRekisterointi(@RequestBody VardaRekisterointi request) throws OrganisaatioResourceException {
        Organisaatio existing = organisaatioRepository.findByYTunnus(request.getYtunnus());
        OrganisaatioRDTOV4 result = existing != null
            ? updateExistingVardaOrganisation(existing)
            : createNewVardaOrganisation(request);
        inviteUser(request, result.getOid());
        return result;
    }

    private OrganisaatioRDTOV4 updateExistingVardaOrganisation(Organisaatio existing) {
        log.info("Registering existing organisation {} as Varda organisation", existing.getOid());
        existing.setLakkautusPvm(null);
        existing.setTyypit(getTyypitWithVarhaiskasvatuksenJarjestaja(existing));
        organisaatioRepository.save(existing);
        return conversionService.convert(existing, OrganisaatioRDTOV4.class);
    }

    private OrganisaatioRDTOV4 createNewVardaOrganisation(VardaRekisterointi request) {
        log.info("Registering new organisation {} as Varda organisation", request.getYtunnus());
        Organisaatio organisaatio = organisaatioYtjService.findOrganisaatioByYtunnus(request.getYtunnus());
        organisaatio.setTyypit(getTyypitWithVarhaiskasvatuksenJarjestaja(organisaatio));
        OrganisaatioRDTOV4 dto = conversionService.convert(organisaatio, OrganisaatioRDTOV4.class);
        return organisaatioBusinessService.saveOrUpdate(dto).getOrganisaatio();
    }

    private Set<String> getTyypitWithVarhaiskasvatuksenJarjestaja(Organisaatio organisaatio) {
        Set<String> tyypit = new HashSet<>();
        tyypit.addAll(organisaatio.getTyypit());
        tyypit.add(OrganisaatioTyyppi.VARHAISKASVATUKSEN_JARJESTAJA.koodiValue());
        return tyypit;
    }

    private void inviteUser(VardaRekisterointi request, String organisaatioOid) {
        kayttooikeusClient.kutsuKayttaja(request.getPaakayttaja(), organisaatioOid, vardaKayttooikeusryhmaId, "test@test.fi");
    }
}
