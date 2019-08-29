package fi.vm.sade.varda.rekisterointi.controller.hakija;

import fi.vm.sade.varda.rekisterointi.model.Rekisterointi;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hakija/api/rekisterointi")
public class RekisterointiController {

    @PostMapping
    public void register(@RequestBody @Validated Rekisterointi dto) {
        System.out.println("Got " + dto);
    }

}
