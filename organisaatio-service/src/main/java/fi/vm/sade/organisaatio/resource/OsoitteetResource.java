package fi.vm.sade.organisaatio.resource;


import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.model.Email;
import fi.vm.sade.organisaatio.model.Organisaatio;
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
                    Optional<Email> emailYhteystieto = o.getYhteystiedot().stream()
                            .filter(yhteystieto -> kieli.equals(yhteystieto.getKieli()) && yhteystieto instanceof Email)
                            .map(yhteystieto -> (Email) yhteystieto)
                            .findFirst();
                    Optional<String> sahkoposti = emailYhteystieto.map(Email::getEmail);
                    return new Hakutulos(o.getId(), nimi, sahkoposti);
                })
                .collect(Collectors.toList());
    }

    @Data
    public static class Hakutulos {
        public final Long organisaatioId;
        public final String nimi;
        public final Optional<String> sahkoposti;
    }
}
