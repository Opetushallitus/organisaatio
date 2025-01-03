package fi.vm.sade.varda.rekisterointi.controller.virkailija;

import fi.vm.sade.properties.OphProperties;
import fi.vm.sade.varda.rekisterointi.client.OrganisaatioClient;
import fi.vm.sade.varda.rekisterointi.exception.InvalidInputException;
import fi.vm.sade.varda.rekisterointi.model.*;
import fi.vm.sade.varda.rekisterointi.service.OrganisaatioService;
import fi.vm.sade.varda.rekisterointi.service.RekisterointiService;
import fi.vm.sade.varda.rekisterointi.util.AuthenticationUtils;
import fi.vm.sade.varda.rekisterointi.util.Constants;
import fi.vm.sade.varda.rekisterointi.util.RequestContextImpl;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static fi.vm.sade.varda.rekisterointi.util.FunctionalUtils.exceptionToEmptySupplier;

@RestController
@PreAuthorize(Constants.VIRKAILIJA_PRE_AUTH)
@RequestMapping(VirkailijaController.BASE_PATH)
@RequiredArgsConstructor
public class VirkailijaController {

    static final String BASE_PATH = "/virkailija/api";
    static final String REKISTEROINNIT_PATH = "/rekisteroinnit";
    static final String PAATOKSET_PATH = "/paatokset";
    static final String PAATOKSET_BATCH_PATH = PAATOKSET_PATH + "/batch";

    private static final String ORGANISAATIOT_PATH = "/organisaatiot";
    private static final String OPH_OID = "1.2.246.562.10.00000000001";
    private static final Logger LOGGER = LoggerFactory.getLogger(VirkailijaController.class);

    private final OrganisaatioClient organisaatioClient;
    private final OrganisaatioService organisaatioService;
    private final RekisterointiService rekisterointiService;
    private final OphProperties properties;

    /**
     * Hakee organisaation y-tunnuksella.
     *
     * @param ytunnus   y-tunnus
     *
     * @return organisaatio (mahdollisesti tyhjä)
     */
    @GetMapping(ORGANISAATIOT_PATH + "/ytunnus={ytunnus}")
    public Organisaatio getOrganisaatioByYtunnus(@PathVariable String ytunnus) {
        Organisaatio organisaatio = organisaatioService.muunnaOrganisaatioDto(organisaatioClient.getOrganisaatioByYtunnus(ytunnus)
                .or(exceptionToEmptySupplier(() -> organisaatioClient.getOrganisaatioByYtunnusFromYtj(ytunnus)))
                .orElseGet(() -> OrganisaatioDto.of(ytunnus, "")));
        if ( organisaatio.isKunta() ) {
            throw new InvalidInputException("ERROR_MUNICIPALITY");
        }
        return organisaatio;
    }

    /**
     * Luo rekisteröintihakemuksen.
     *
     * @param dto       rekisteröintihakemus
     * @param request   HTTP-pyyntö
     * @return paluuosoite.
     */
    @PostMapping(REKISTEROINNIT_PATH)
    public String luoRekisterointi(
            Authentication authentication,
            @RequestBody @Validated RekisterointiDto dto,
            HttpServletRequest request) {
        List<String> roles = AuthenticationUtils.getRoles(authentication);
        if (roles.contains("OPH")) {
            rekisterointiService.create(Rekisterointi.from(dto), RequestContextImpl.of(request));
            return properties.url("varda-rekisterointi.virkailija");
        }
        return null;
    }

    /**
     * Listaa rekisteröintihakemukset tilan perusteella. Tuloksia voi rajata antamalla
     * organisaatioon kohdistuvan hakutermin.
     *
     * @param authentication    autentikointi
     * @param tila              hakemusten tila
     * @param hakutermi         hakutermi organisaatiolle
     * @return löytyneet hakemukset.
     */
    @GetMapping(REKISTEROINNIT_PATH)
    public Iterable<Rekisterointi> listaaRekisteroinnit(
            Authentication authentication,
            @RequestParam("tila") Rekisterointi.Tila tila,
            @RequestParam(value = "hakutermi", required = false) String hakutermi) {
        List<String> organisaatioOidit = haeOrganisaatioOidit(authentication.getAuthorities());
        if (onOphVirkailija(organisaatioOidit)) {
            LOGGER.info("Käyttäjällä on oikeus nähdä kaikki rekisteröinnit.");
            return rekisterointiService.listByTilaAndOrganisaatio(tila, hakutermi);
        }
        List<String> kunnat = virkailijanKunnat(organisaatioOidit);
        if (kunnat.isEmpty()) {
            LOGGER.info("Käyttäjällä ei ole oikeutta nähdä yhdenkään kunnan rekisteröintejä.");
            return List.of();
        }
        return rekisterointiService.listByTilaAndKunnatAndOrganisaatio(
                tila, kunnat.toArray(new String[0]), hakutermi);
    }

    /**
     * Listaa rekisteröintihakemukset käyttäjän oikeuksien perusteella.
     *
     * @param authentication autentikointi
     *
     * @return hakemukset.
     */
    @GetMapping("/rekisterointi")
    public Iterable<Rekisterointi> listRegistrations(Authentication authentication) {
        String[] registrationTypes = AuthenticationUtils.getRegistrationTypes(authentication);
        if (registrationTypes.length == 1 && registrationTypes[0].equals("varda")) {
            List<String> organisaatioOidit = haeOrganisaatioOidit(authentication.getAuthorities());
            List<String> kunnat = virkailijanKunnat(organisaatioOidit);
            return rekisterointiService.listByVardaKunnat(kunnat.toArray(new String[0]));
        } else {
            return rekisterointiService.listByRegistrationTypes(registrationTypes);
        }
    }

    /**
     * Luo päätöksen hakemukselle.
     *
     * @param authentication    autentikointi
     * @param paatos            päätös
     * @param request           HTTP-pyyntö
     * @return rekisteröintihakemus, jolle päätös luotiin.
     */
    @PostMapping(PAATOKSET_PATH)
    @ResponseStatus(HttpStatus.CREATED)
    public Rekisterointi luoPaatos(
            Authentication authentication,
            @RequestBody @Validated PaatosDto paatos,
            HttpServletRequest request) {
        return rekisterointiService.resolve(authentication.getName(), paatos, RequestContextImpl.of(request));
    }

    /**
     * Luo päätöksen useammalle hakemukselle kerralla.
     *
     * @param authentication    autentikointi
     * @param paatokset         luotavat päätökset
     * @param request           HTTP-pyyntö
     */
    @PostMapping(PAATOKSET_BATCH_PATH)
    @ResponseStatus(HttpStatus.CREATED)
    public void luoPaatokset(
            Authentication authentication,
            @RequestBody @Validated PaatosBatch paatokset,
            HttpServletRequest request) {
        rekisterointiService.resolveBatch(authentication.getName(), paatokset, RequestContextImpl.of(request));
    }

    List<String> haeOrganisaatioOidit(Collection<? extends GrantedAuthority> grantedAuthorities) {
        return grantedAuthorities.stream()
                .filter(grantedAuthority -> grantedAuthority.getAuthority().contains(Constants.VARDA_ROLE + '_'))
                .map(grantedAuthority -> {
                    String authority = grantedAuthority.getAuthority();
                    return authority.substring(authority.lastIndexOf('_') + 1);
                }).collect(Collectors.toList());
    }

    private boolean onOphVirkailija(List<String> organisaatioOidit) {
        return organisaatioOidit.contains(OPH_OID);
    }

    private List<String> virkailijanKunnat(List<String> organisaatioOidit) {
        // TODO: batch API useammalle kunnalle tai cachetus?
        return organisaatioOidit.stream()
                .map(organisaatioClient::getKuntaByOid)
                .filter(Optional::isPresent)
                .map(optOrganisaatio -> optOrganisaatio.get().kotipaikkaUri)
                .collect(Collectors.toList());
    }
}
