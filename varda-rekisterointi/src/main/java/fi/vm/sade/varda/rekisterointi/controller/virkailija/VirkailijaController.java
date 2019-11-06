package fi.vm.sade.varda.rekisterointi.controller.virkailija;

import fi.vm.sade.varda.rekisterointi.util.RequestContextImpl;
import fi.vm.sade.varda.rekisterointi.client.OrganisaatioClient;
import fi.vm.sade.varda.rekisterointi.model.*;
import fi.vm.sade.varda.rekisterointi.service.OrganisaatioService;
import fi.vm.sade.varda.rekisterointi.service.RekisterointiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import static fi.vm.sade.varda.rekisterointi.util.FunctionalUtils.exceptionToEmptySupplier;

@RestController
@PreAuthorize("hasAnyRole('ROLE_APP_YKSITYISTEN_REKISTEROITYMINEN_CRUD')")
@RequestMapping(VirkailijaController.BASE_PATH)
@RequiredArgsConstructor
public class VirkailijaController {

    static final String BASE_PATH = "/virkailija/api";
    static final String ORGANISAATIOT_PATH = "/organisaatiot";
    static final String REKISTEROINNIT_PATH = "/rekisteroinnit";
    static final String PAATOKSET_PATH = "/paatokset";
    static final String PAATOKSET_BATCH_PATH = PAATOKSET_PATH + "/batch";

    private final OrganisaatioClient organisaatioClient;
    private final OrganisaatioService organisaatioService;
    private final RekisterointiService rekisterointiService;

    @GetMapping(ORGANISAATIOT_PATH + "/ytunnus={ytunnus}")
    public Organisaatio getOrganisaatioByYtunnus(@PathVariable String ytunnus) {
        return organisaatioService.muunnaV4Dto(organisaatioClient.getV4ByYtunnus(ytunnus)
                .or(exceptionToEmptySupplier(() -> organisaatioClient.getV4ByYtunnusFromYtj(ytunnus)))
                .orElseGet(() -> OrganisaatioV4Dto.of(ytunnus, "")));
    }

    @PostMapping(REKISTEROINNIT_PATH)
    public void luoRekisterointi(@RequestBody @Validated Rekisterointi dto, HttpServletRequest request) {
        rekisterointiService.create(dto, RequestContextImpl.of(request));
    }

    @GetMapping(REKISTEROINNIT_PATH)
    public Iterable<Rekisterointi> listaaRekisteroinnit(
            @RequestParam("tila") Rekisterointi.Tila tila,
            @RequestParam(value = "hakutermi", required = false) String hakutermi) {
        return rekisterointiService.listByTilaAndOrganisaatio(tila, hakutermi); // TODO: kaiva virkailija, rajaa hakua, ks. KJHH-1709
    }

    @PostMapping(PAATOKSET_PATH)
    @ResponseStatus(HttpStatus.CREATED)
    public Rekisterointi luoPaatos(Authentication authentication, @RequestBody @Validated PaatosDto paatos, HttpServletRequest request) {
        return rekisterointiService.resolve(authentication.getName(), paatos, RequestContextImpl.of(request));
    }

    @PostMapping(PAATOKSET_BATCH_PATH)
    @ResponseStatus(HttpStatus.CREATED)
    public void luoPaatokset(Authentication authentication, @RequestBody @Validated PaatosBatch paatokset, HttpServletRequest request) {
        rekisterointiService.resolveBatch(authentication.getName(), paatokset, RequestContextImpl.of(request));
    }
}
