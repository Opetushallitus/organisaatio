package fi.vm.sade.varda.rekisterointi.controller;

import fi.vm.sade.varda.rekisterointi.client.KoodistoClient;
import fi.vm.sade.varda.rekisterointi.model.Koodi;
import fi.vm.sade.varda.rekisterointi.model.KoodistoType;
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
    Collection<Koodi> getKoodi(@PathVariable KoodistoType koodisto,
                               @RequestParam(required = false) Optional<Integer> versio,
                               @RequestParam(required = false) Optional<Boolean> onlyValid) {
        return koodistoClient.listKoodit(koodisto, versio, onlyValid);
    }

}
