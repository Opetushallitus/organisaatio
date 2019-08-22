package fi.vm.sade.varda.rekisterointi.controller;

import fi.vm.sade.varda.rekisterointi.client.KoodistoClient;
import fi.vm.sade.varda.rekisterointi.model.Koodi;
import fi.vm.sade.varda.rekisterointi.model.KoodistoType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping("/api/koodisto")
public class KoodistoController {

    private final KoodistoClient koodistoClient;

    public KoodistoController(KoodistoClient koodistoClient) {
        this.koodistoClient = koodistoClient;
    }

    @GetMapping("/{koodisto}/koodi")
    Collection<Koodi> getKoodi(@PathVariable KoodistoType koodisto) {
        return koodistoClient.listKoodit(koodisto);
    }

}
