package fi.vm.sade.varda.rekisterointi.controller.virkailija;

import fi.vm.sade.varda.rekisterointi.client.OrganisaatioClient;
import fi.vm.sade.varda.rekisterointi.model.Organisaatio;
import fi.vm.sade.varda.rekisterointi.model.OrganisaatioV4Dto;
import fi.vm.sade.varda.rekisterointi.model.Paatos;
import fi.vm.sade.varda.rekisterointi.model.Rekisterointi;
import fi.vm.sade.varda.rekisterointi.service.OrganisaatioService;
import fi.vm.sade.varda.rekisterointi.service.RekisterointiService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static fi.vm.sade.varda.rekisterointi.util.FunctionalUtils.exceptionToEmptySupplier;

@RestController
@RequestMapping(VirkailijaController.BASE_PATH)
@RequiredArgsConstructor
public class VirkailijaController {
    // TODO: rajaa pääsy ainoastaan virkailijoille

    static final String BASE_PATH = "/virkailija/api";
    static final String ORGANISAATIOT_PATH = "/organisaatiot";
    static final String REKISTEROINNIT_PATH = "/rekisteroinnit";
    static final String PAATOKSET_PATH = "/paatokset";

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
    public void luoRekisterointi(@RequestBody @Validated Rekisterointi dto) {
        rekisterointiService.create(dto);
    }

    @GetMapping(REKISTEROINNIT_PATH)
    public Iterable<Rekisterointi> listaaRekisteroinnit(
            @RequestParam("tila") Rekisterointi.Tila tila,
            @RequestParam(value = "hakutermi", required = false) String hakutermi) {
        return rekisterointiService.listByTilaAndOrganisaatio(tila, hakutermi); // TODO: kaiva virkailija, rajaa hakua, ks. KJHH-1709
    }

    @PostMapping(PAATOKSET_PATH)
    public Rekisterointi luoPaatos(@RequestBody @Validated Paatos paatos) {
        return rekisterointiService.resolve(paatos);
    }
}
