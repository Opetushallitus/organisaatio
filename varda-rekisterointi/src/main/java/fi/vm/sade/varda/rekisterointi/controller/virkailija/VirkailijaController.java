package fi.vm.sade.varda.rekisterointi.controller.virkailija;

import fi.vm.sade.varda.rekisterointi.model.Paatos;
import fi.vm.sade.varda.rekisterointi.model.Rekisterointi;
import fi.vm.sade.varda.rekisterointi.service.RekisterointiService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(VirkailijaController.BASE_PATH)
public class VirkailijaController {
    // TODO: rajaa pääsy ainoastaan virkailijoille

    static final String BASE_PATH = "/virkailija/api";
    static final String REKISTEROINNIT_PATH = "/rekisteroinnit";
    static final String PAATOKSET_PATH = "/paatokset";

    private final RekisterointiService rekisterointiService;

    public VirkailijaController(RekisterointiService rekisterointiService) {
        this.rekisterointiService = rekisterointiService;
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
