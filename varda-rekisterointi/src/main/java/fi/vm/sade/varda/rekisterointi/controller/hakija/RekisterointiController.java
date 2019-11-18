package fi.vm.sade.varda.rekisterointi.controller.hakija;

import fi.vm.sade.varda.rekisterointi.model.Rekisterointi;
import fi.vm.sade.varda.rekisterointi.service.RekisterointiService;
import fi.vm.sade.varda.rekisterointi.util.RequestContextImpl;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(RekisterointiController.BASE_PATH)
public class RekisterointiController {

    static final String BASE_PATH = "/hakija/api/rekisteroinnit";
    private final RekisterointiService service;

    public RekisterointiController(RekisterointiService service) {
        this.service = service;
    }

    @PostMapping
    public void register(@RequestBody @Validated Rekisterointi dto, HttpServletRequest request, Authentication authentication) {
        service.create(dto, RequestContextImpl.of(request, authentication));
    }

}
