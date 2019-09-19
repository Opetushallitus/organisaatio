package fi.vm.sade.varda.rekisterointi.controller.hakija;

import fi.vm.sade.varda.rekisterointi.exception.NotFoundException;
import fi.vm.sade.varda.rekisterointi.model.OrganisaatioV4Dto;
import fi.vm.sade.varda.rekisterointi.model.Valtuudet;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping(OrganisaatioController.BASE_PATH)
@Scope("session")
public class OrganisaatioController {

    static final String BASE_PATH = "/hakija/api/organisaatiot";
    private final Valtuudet valtuudet;

    public OrganisaatioController(Valtuudet valtuudet) {
        this.valtuudet = valtuudet;
    }

    @GetMapping
    public OrganisaatioV4Dto getOrganisaatio() {
        return Optional.ofNullable(this.valtuudet).flatMap(v -> Optional.ofNullable(v.organisaatio))
                .orElseThrow(() -> new NotFoundException("Organisaatiota ei löydy istunnosta"));
    }

}
