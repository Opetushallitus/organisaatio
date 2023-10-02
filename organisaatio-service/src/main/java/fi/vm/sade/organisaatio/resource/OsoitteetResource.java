package fi.vm.sade.organisaatio.resource;


import com.fasterxml.jackson.annotation.JsonInclude;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.model.Email;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.Osoite;
import fi.vm.sade.organisaatio.model.Puhelinnumero;
import fi.vm.sade.organisaatio.repository.OrganisaatioRepository;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Hidden
@RestController
@RequestMapping("${server.internal.context-path}/osoitteet")
@RequiredArgsConstructor
public class OsoitteetResource {
    private final OrganisaatioRepository organisaatioRepository;

    @GetMapping(value = "/hae", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ROLE_APP_OSOITE_CRUD')")
    public List<Hakutulos> hae() throws InterruptedException {
        String kieli = "fi";
        String koulutustoimijaKoodi = OrganisaatioTyyppi.KOULUTUSTOIMIJA.koodiValue();

        List<Organisaatio> koulutustoimijat = organisaatioRepository.findByOrganisaatiotyyppi(koulutustoimijaKoodi);

        return koulutustoimijat.stream()
                .map(o -> {
                    String nimi = o.getNimi().getString(kieli);
                    Optional<String> sahkoposti = Optional.ofNullable(o.getEmail(kieli)).map(Email::getEmail);
                    Optional<String> puhelinnumero = Optional.ofNullable(o.getPuhelin(Puhelinnumero.TYYPPI_PUHELIN, kieli)).map(Puhelinnumero::getPuhelinnumero);
                    return new Hakutulos(
                            o.getId(),
                            o.getOid(),
                            nimi,
                            sahkoposti,
                            o.getYritysmuoto(),
                            puhelinnumero,
                            Optional.empty(),
                            Optional.empty(),
                            o.getKotipaikka(),
                            Optional.empty(),
                            o.getYtunnus(),
                            osoiteToString(o.getPostiosoite()),
                            osoiteToString(o.getKayntiosoite())
                    );
                })
                .collect(Collectors.toList());
    }

    private String osoiteToString(Osoite osoite) {
        return String.format("%s, %s %s",
                osoite.getOsoite(),
                osoite.getPostinumero(),
                osoite.getPostitoimipaikka()
        );
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    static class Hakutulos {
        final Long id;
        final String oid;
        final String nimi;
        final Optional<String> sahkoposti;
        final String yritysmuoto;
        final Optional<String> puhelinnumero;
        final Optional<String> opetuskieli;
        final Optional<String> oppilaitostunnus;
        final String kunta;
        final Optional<String> koskiVirheilmoituksenOsoite;
        final String ytunnus;
        final String postiosoite;
        final String kayntiosoite;
    }
}
