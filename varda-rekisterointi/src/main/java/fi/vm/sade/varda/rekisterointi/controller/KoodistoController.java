package fi.vm.sade.varda.rekisterointi.controller;

import fi.vm.sade.varda.rekisterointi.client.KoodistoClient;
import fi.vm.sade.varda.rekisterointi.model.Koodi;
import fi.vm.sade.varda.rekisterointi.model.KoodistoType;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping("/api/koodisto")
public class KoodistoController {

    private final KoodistoClient koodistoClient;

    public KoodistoController(KoodistoClient koodistoClient) {
        this.koodistoClient = koodistoClient;
    }

    /**
     * Hakee koodiston koodit.
     *
     * @param koodisto  haluttu koodisto
     * @param versio    koodistoversio (ei pakollinen)
     * @param onlyValid vain voimassaolevat (ei pakollinen)
     * @return löydetyt koodit.
     */
    @GetMapping("/{koodisto}/koodi")
    @ApiOperation("Hae koodiston koodit")
    Collection<Koodi> getKoodi(@ApiParam("haluttu koodisto") @PathVariable KoodistoType koodisto,
                               @ApiParam("haluttu versio") @RequestParam(required = false) Optional<Integer> versio,
                               @ApiParam("listataanko vain voimassaolevat") @RequestParam(required = false) Optional<Boolean> onlyValid) {
        return koodistoClient.listKoodit(koodisto, versio, onlyValid);
    }

}
