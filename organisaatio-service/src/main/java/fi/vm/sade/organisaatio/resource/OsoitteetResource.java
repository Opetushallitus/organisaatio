package fi.vm.sade.organisaatio.resource;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonValue;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.model.Email;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.Osoite;
import fi.vm.sade.organisaatio.model.Puhelinnumero;
import fi.vm.sade.organisaatio.repository.OrganisaatioRepository;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Hidden
@RestController
@RequestMapping("${server.internal.context-path}/osoitteet")
@RequiredArgsConstructor
@Slf4j
public class OsoitteetResource {
    private final OrganisaatioRepository organisaatioRepository;
    private final EntityManager em;

    @GetMapping(value = "/hae", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ROLE_APP_OSOITE_CRUD')")
    public List<Hakutulos> hae(
            @RequestParam("organisaatiotyypit[]") List<String> organisaatiotyypit,
            @RequestParam(value = "oppilaitostyypit[]", defaultValue = "", required = false) List<String> oppilaitostyypit
    ) throws InterruptedException {
        String kieli = "fi";
        String kieliKoodi = "kieli_fi#1";
        String organisaatiotyyppi = OrganisaatioTyyppi.KOULUTUSTOIMIJA.koodiValue();
        List<Organisaatio> organisaatios = new ArrayList<>();

        if (OrganisaatioTyyppi.KOULUTUSTOIMIJA.koodiValue().equals(organisaatiotyyppi)) {
            if (oppilaitostyypit.isEmpty()) {
                organisaatios.addAll(searchByOrganisaatioTyyppi(organisaatiotyyppi));
            } else {
                List<Long> koulutustoimijaIds = findKoulutustoimijatHavingOppilaitosUnderThemWithOppilaitostyyppi(oppilaitostyypit);
                organisaatios.addAll(organisaatioRepository.findAllById(koulutustoimijaIds));
            }
        }


        List<Hakutulos> result = organisaatios.stream()
                .map(o -> {
                    String nimi = o.getNimi().getString(kieli);
                    Optional<String> sahkoposti = Optional.ofNullable(o.getEmail(kieliKoodi)).map(Email::getEmail);
                    Optional<String> puhelinnumero = Optional.ofNullable(o.getPuhelin(Puhelinnumero.TYYPPI_PUHELIN, kieliKoodi)).map(Puhelinnumero::getPuhelinnumero);
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
                            Optional.ofNullable(o.getPostiosoite()).map(this::osoiteToString),
                            Optional.ofNullable(o.getKayntiosoite()).map(this::osoiteToString)
                    );
                })
                .collect(Collectors.toList());


        log.info("Hakutuloksia: {}", result.size());
        result.sort(Comparator.comparing(Hakutulos::getNimi));
        return result;
    }

    private List<Organisaatio> searchByOrganisaatioTyyppi(String organisaatiotyyppi) {
        return em.createQuery(
                        "SELECT o FROM Organisaatio o" +
                                " WHERE :organisaatiotyyppi MEMBER OF o.tyypit" +
                                " AND (o.lakkautusPvm IS NULL OR o.lakkautusPvm > current_date())" +
                                " AND o.organisaatioPoistettu != true",
                        Organisaatio.class
                )
                .setParameter("organisaatiotyyppi", organisaatiotyyppi)
                .getResultList();
    }

    private List<Organisaatio> searchByOrganisaatioTyyppiAndOppilaitostyypit(String organisaatiotyyppi, List<String> oppilaitostyypit) {
        return em.createQuery(
                        "SELECT o FROM Organisaatio o" +
                                " WHERE :organisaatiotyyppi MEMBER OF o.tyypit" +
                                " AND o.oppilaitosTyyppi IN (:oppilaitostyypit)" +
                                " AND (o.lakkautusPvm IS NULL OR o.lakkautusPvm > current_date())" +
                                " AND o.organisaatioPoistettu != true",
                        Organisaatio.class
                )
                .setParameter("organisaatiotyyppi", organisaatiotyyppi)
                .setParameter("oppilaitostyypit", oppilaitostyypit)
                .getResultList();
    }

    private List<Long> findKoulutustoimijatHavingOppilaitosUnderThemWithOppilaitostyyppi(List<String> oppilaitostyypit) {
        return em.createQuery(
                        "SELECT parent.id FROM Organisaatio parent" +
                                " JOIN parent.childSuhteet suhde" +
                                " JOIN suhde.child child" +
                                " WHERE :koulutustoimijaKoodi MEMBER OF parent.tyypit" +
                                " AND (suhde.loppuPvm IS NULL OR (suhde.alkuPvm <= current_date() AND current_date() < suhde.loppuPvm))" +
                                " AND (parent.lakkautusPvm IS NULL OR current_date() < parent.lakkautusPvm)" +
                                " AND parent.organisaatioPoistettu != true" +
                                " AND :organisaatiotyyppi MEMBER OF child.tyypit" +
                                " AND child.oppilaitosTyyppi IN (:oppilaitostyypit)" +
                                " AND (child.lakkautusPvm IS NULL OR current_date() < child.lakkautusPvm)" +
                                " AND child.organisaatioPoistettu != true",
                        Long.class
                )
                .setParameter("koulutustoimijaKoodi", OrganisaatioTyyppi.KOULUTUSTOIMIJA.koodiValue())
                .setParameter("organisaatiotyyppi", OrganisaatioTyyppi.OPPILAITOS.koodiValue())
                .setParameter("oppilaitostyypit", oppilaitostyypit)
                .getResultList();
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
        final Optional<String> postiosoite;
        final Optional<String> kayntiosoite;
    }

    @GetMapping(value = "/parametrit")
    @PreAuthorize("hasAnyRole('ROLE_APP_OSOITE_CRUD')")
    public RawJson getParametrit() {
        return new RawJson("{ \"oppilaitostyypit\": " + oppilaitostyyppiKoodit + "}");
    }
    private String oppilaitostyyppiKoodit = "[\n" +
            "  {\n" +
            "    \"koodiUri\": \"oppilaitostyyppi_22\",\n" +
            "    \"resourceUri\": \"https://virkailija.testiopintopolku.fi/koodisto-service/rest/codeelement/oppilaitostyyppi_22\",\n" +
            "    \"version\": 12,\n" +
            "    \"versio\": 1,\n" +
            "    \"koodisto\": {\n" +
            "      \"koodistoUri\": \"oppilaitostyyppi\",\n" +
            "      \"organisaatioOid\": \"1.2.246.562.10.00000000001\",\n" +
            "      \"koodistoVersios\": [\n" +
            "        1\n" +
            "      ]\n" +
            "    },\n" +
            "    \"koodiArvo\": \"22\",\n" +
            "    \"paivitysPvm\": \"2018-02-06\",\n" +
            "    \"paivittajaOid\": null,\n" +
            "    \"voimassaAlkuPvm\": \"1990-01-01\",\n" +
            "    \"voimassaLoppuPvm\": null,\n" +
            "    \"tila\": \"LUONNOS\",\n" +
            "    \"metadata\": [\n" +
            "      {\n" +
            "        \"nimi\": \"Specialyrkesläroanstalter\",\n" +
            "        \"kuvaus\": \"Specialyrkesläroanstalter\",\n" +
            "        \"lyhytNimi\": \"Specialyrkesläroanstalter\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"SV\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"nimi\": \"Vocational special education institutions\",\n" +
            "        \"kuvaus\": \"Vocational special education institutions\",\n" +
            "        \"lyhytNimi\": \"Vocational special education institutions\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"EN\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"nimi\": \"Ammatilliset erityisoppilaitokset\",\n" +
            "        \"kuvaus\": \"Ammatilliset erityisoppilaitokset\",\n" +
            "        \"lyhytNimi\": \"Ammatilliset erityisoppilaitokset\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"FI\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"koodiUri\": \"oppilaitostyyppi_28\",\n" +
            "    \"resourceUri\": \"https://virkailija.testiopintopolku.fi/koodisto-service/rest/codeelement/oppilaitostyyppi_28\",\n" +
            "    \"version\": 4,\n" +
            "    \"versio\": 1,\n" +
            "    \"koodisto\": {\n" +
            "      \"koodistoUri\": \"oppilaitostyyppi\",\n" +
            "      \"organisaatioOid\": \"1.2.246.562.10.00000000001\",\n" +
            "      \"koodistoVersios\": [\n" +
            "        1\n" +
            "      ]\n" +
            "    },\n" +
            "    \"koodiArvo\": \"28\",\n" +
            "    \"paivitysPvm\": \"2020-11-04\",\n" +
            "    \"paivittajaOid\": \"1.2.246.562.24.25909319372\",\n" +
            "    \"voimassaAlkuPvm\": \"1990-01-01\",\n" +
            "    \"voimassaLoppuPvm\": null,\n" +
            "    \"tila\": \"LUONNOS\",\n" +
            "    \"metadata\": [\n" +
            "      {\n" +
            "        \"nimi\": \"Palo-, poliisi- ja vartiointialojen oppilaitokset\",\n" +
            "        \"kuvaus\": \"Palo-, poliisi- ja vartiointialojen oppilaitokset\",\n" +
            "        \"lyhytNimi\": \"Palo-, poliisi- ja vartiointialojen oppilaitokset\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"FI\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"nimi\": \"Läroanstalter inom brand-, polis- och bevakningsväsendet\",\n" +
            "        \"kuvaus\": \"Läroanstalter inom brand-, polis- och bevakningsväsendet\",\n" +
            "        \"lyhytNimi\": \"Läroanstalter inom brand-, polis- och bevakningsväsendet\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"SV\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"nimi\": \"Fire, police and security service institutes\",\n" +
            "        \"kuvaus\": \"Fire, police and security service institutes\",\n" +
            "        \"lyhytNimi\": \"Fire, police and security service institutesitokset\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"EN\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"koodiUri\": \"oppilaitostyyppi_93\",\n" +
            "    \"resourceUri\": \"https://virkailija.testiopintopolku.fi/koodisto-service/rest/codeelement/oppilaitostyyppi_93\",\n" +
            "    \"version\": 8,\n" +
            "    \"versio\": 1,\n" +
            "    \"koodisto\": {\n" +
            "      \"koodistoUri\": \"oppilaitostyyppi\",\n" +
            "      \"organisaatioOid\": \"1.2.246.562.10.00000000001\",\n" +
            "      \"koodistoVersios\": [\n" +
            "        1\n" +
            "      ]\n" +
            "    },\n" +
            "    \"koodiArvo\": \"93\",\n" +
            "    \"paivitysPvm\": \"2020-11-04\",\n" +
            "    \"paivittajaOid\": \"1.2.246.562.24.25909319372\",\n" +
            "    \"voimassaAlkuPvm\": \"1990-01-01\",\n" +
            "    \"voimassaLoppuPvm\": null,\n" +
            "    \"tila\": \"LUONNOS\",\n" +
            "    \"metadata\": [\n" +
            "      {\n" +
            "        \"nimi\": \"Muut koulutuksen järjestäjät\",\n" +
            "        \"kuvaus\": \"Koodiarvo ei sisälly viralliseen Tilastokeskuksen ylläpitämään oppilaitostyyppiluokitukseen\",\n" +
            "        \"lyhytNimi\": \"Muut koulutuksen järjestäjät\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"FI\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"nimi\": \"Övriga utbildningsanordnare\",\n" +
            "        \"kuvaus\": \"Övriga utbildningsanordnare\",\n" +
            "        \"lyhytNimi\": \"Övriga utbildningsanordnare\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"SV\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"nimi\": \"Muut koulutuksen järjestäjät\",\n" +
            "        \"kuvaus\": \"Muut koulutuksen järjestäjät\",\n" +
            "        \"lyhytNimi\": \"Muut koulutuksen järjestäjät\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"EN\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"koodiUri\": \"oppilaitostyyppi_12\",\n" +
            "    \"resourceUri\": \"https://virkailija.testiopintopolku.fi/koodisto-service/rest/codeelement/oppilaitostyyppi_12\",\n" +
            "    \"version\": 5,\n" +
            "    \"versio\": 1,\n" +
            "    \"koodisto\": {\n" +
            "      \"koodistoUri\": \"oppilaitostyyppi\",\n" +
            "      \"organisaatioOid\": \"1.2.246.562.10.00000000001\",\n" +
            "      \"koodistoVersios\": [\n" +
            "        1\n" +
            "      ]\n" +
            "    },\n" +
            "    \"koodiArvo\": \"12\",\n" +
            "    \"paivitysPvm\": \"2018-02-06\",\n" +
            "    \"paivittajaOid\": null,\n" +
            "    \"voimassaAlkuPvm\": \"1990-01-01\",\n" +
            "    \"voimassaLoppuPvm\": null,\n" +
            "    \"tila\": \"LUONNOS\",\n" +
            "    \"metadata\": [\n" +
            "      {\n" +
            "        \"nimi\": \"Peruskouluasteen erityiskoulut\",\n" +
            "        \"kuvaus\": \"Peruskouluasteen erityiskoulut\",\n" +
            "        \"lyhytNimi\": \"Peruskouluasteen erityiskoulut\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"FI\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"nimi\": \"Special education schools\",\n" +
            "        \"kuvaus\": \"Special education schools\",\n" +
            "        \"lyhytNimi\": \"Special education schools\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"EN\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"nimi\": \"Specialskolor på grundskolenivå\",\n" +
            "        \"kuvaus\": \"Specialskolor på grundskolenivå\",\n" +
            "        \"lyhytNimi\": \"Specialskolor på grundskolenivå\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"SV\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"koodiUri\": \"oppilaitostyyppi_41\",\n" +
            "    \"resourceUri\": \"https://virkailija.testiopintopolku.fi/koodisto-service/rest/codeelement/oppilaitostyyppi_41\",\n" +
            "    \"version\": 5,\n" +
            "    \"versio\": 1,\n" +
            "    \"koodisto\": {\n" +
            "      \"koodistoUri\": \"oppilaitostyyppi\",\n" +
            "      \"organisaatioOid\": \"1.2.246.562.10.00000000001\",\n" +
            "      \"koodistoVersios\": [\n" +
            "        1\n" +
            "      ]\n" +
            "    },\n" +
            "    \"koodiArvo\": \"41\",\n" +
            "    \"paivitysPvm\": \"2018-02-06\",\n" +
            "    \"paivittajaOid\": null,\n" +
            "    \"voimassaAlkuPvm\": \"1990-01-01\",\n" +
            "    \"voimassaLoppuPvm\": null,\n" +
            "    \"tila\": \"LUONNOS\",\n" +
            "    \"metadata\": [\n" +
            "      {\n" +
            "        \"nimi\": \"Polytechnics\",\n" +
            "        \"kuvaus\": \"Polytechnics\",\n" +
            "        \"lyhytNimi\": \"Polytechnics\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"EN\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"nimi\": \"Yrkeshögskolor\",\n" +
            "        \"kuvaus\": \"Yrkeshögskolor\",\n" +
            "        \"lyhytNimi\": \"Yrkeshögskolor\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"SV\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"nimi\": \"Ammattikorkeakoulut\",\n" +
            "        \"kuvaus\": \"Ammattikorkeakoulut\",\n" +
            "        \"lyhytNimi\": \"Ammattikorkeakoulut\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"FI\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"koodiUri\": \"oppilaitostyyppi_64\",\n" +
            "    \"resourceUri\": \"https://virkailija.testiopintopolku.fi/koodisto-service/rest/codeelement/oppilaitostyyppi_64\",\n" +
            "    \"version\": 8,\n" +
            "    \"versio\": 1,\n" +
            "    \"koodisto\": {\n" +
            "      \"koodistoUri\": \"oppilaitostyyppi\",\n" +
            "      \"organisaatioOid\": \"1.2.246.562.10.00000000001\",\n" +
            "      \"koodistoVersios\": [\n" +
            "        1\n" +
            "      ]\n" +
            "    },\n" +
            "    \"koodiArvo\": \"64\",\n" +
            "    \"paivitysPvm\": \"2018-02-06\",\n" +
            "    \"paivittajaOid\": null,\n" +
            "    \"voimassaAlkuPvm\": \"1990-01-01\",\n" +
            "    \"voimassaLoppuPvm\": null,\n" +
            "    \"tila\": \"LUONNOS\",\n" +
            "    \"metadata\": [\n" +
            "      {\n" +
            "        \"nimi\": \"Kansalaisopistot\",\n" +
            "        \"kuvaus\": \"Kansalaisopistot\",\n" +
            "        \"lyhytNimi\": \"Kansalaisopistot\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"FI\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"nimi\": \"Medborgarinstitut\",\n" +
            "        \"kuvaus\": \"Medborgarinstitut\",\n" +
            "        \"lyhytNimi\": \"Medborgarinstitut\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"SV\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"nimi\": \"Adult education centres\",\n" +
            "        \"kuvaus\": \"Adult education centres\",\n" +
            "        \"lyhytNimi\": \"Adult education centres\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"EN\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"koodiUri\": \"oppilaitostyyppi_01\",\n" +
            "    \"resourceUri\": \"https://virkailija.testiopintopolku.fi/koodisto-service/rest/codeelement/oppilaitostyyppi_01\",\n" +
            "    \"version\": 5,\n" +
            "    \"versio\": 1,\n" +
            "    \"koodisto\": {\n" +
            "      \"koodistoUri\": \"oppilaitostyyppi\",\n" +
            "      \"organisaatioOid\": \"1.2.246.562.10.00000000001\",\n" +
            "      \"koodistoVersios\": [\n" +
            "        1\n" +
            "      ]\n" +
            "    },\n" +
            "    \"koodiArvo\": \"67\",\n" +
            "    \"paivitysPvm\": \"2023-03-15\",\n" +
            "    \"paivittajaOid\": \"1.2.246.562.24.25909319372\",\n" +
            "    \"voimassaAlkuPvm\": \"2015-10-01\",\n" +
            "    \"voimassaLoppuPvm\": null,\n" +
            "    \"tila\": \"LUONNOS\",\n" +
            "    \"metadata\": [\n" +
            "      {\n" +
            "        \"nimi\": \"Taiteen perusopetuksen oppilaitokset (ei musiikki)\",\n" +
            "        \"kuvaus\": \"Taiteen perusopetuksen oppilaitokset (ei musiikki)\",\n" +
            "        \"lyhytNimi\": \"Taiteen perusopetuksen oppilaitokset (ei musiikki)\",\n" +
            "        \"kayttoohje\": null,\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": null,\n" +
            "        \"eiSisallaMerkitysta\": null,\n" +
            "        \"huomioitavaKoodi\": null,\n" +
            "        \"sisaltaaKoodiston\": null,\n" +
            "        \"kieli\": \"FI\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"nimi\": \"Art schools (not music)\",\n" +
            "        \"kuvaus\": \"Art schools (not music)\",\n" +
            "        \"lyhytNimi\": \"Art schools (not music)\",\n" +
            "        \"kayttoohje\": null,\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": null,\n" +
            "        \"eiSisallaMerkitysta\": null,\n" +
            "        \"huomioitavaKoodi\": null,\n" +
            "        \"sisaltaaKoodiston\": null,\n" +
            "        \"kieli\": \"EN\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"nimi\": \"Läroanstalt inom grundläggande konstundervisning (inte musik)\",\n" +
            "        \"kuvaus\": \"Läroanstalt inom grundläggande konstundervisning (inte musik)\",\n" +
            "        \"lyhytNimi\": \"Läroanstalt inom grundläggande konstundervisning (inte musik)\",\n" +
            "        \"kayttoohje\": null,\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": null,\n" +
            "        \"eiSisallaMerkitysta\": null,\n" +
            "        \"huomioitavaKoodi\": null,\n" +
            "        \"sisaltaaKoodiston\": null,\n" +
            "        \"kieli\": \"SV\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"koodiUri\": \"oppilaitostyyppi_61\",\n" +
            "    \"resourceUri\": \"https://virkailija.testiopintopolku.fi/koodisto-service/rest/codeelement/oppilaitostyyppi_61\",\n" +
            "    \"version\": 8,\n" +
            "    \"versio\": 1,\n" +
            "    \"koodisto\": {\n" +
            "      \"koodistoUri\": \"oppilaitostyyppi\",\n" +
            "      \"organisaatioOid\": \"1.2.246.562.10.00000000001\",\n" +
            "      \"koodistoVersios\": [\n" +
            "        1\n" +
            "      ]\n" +
            "    },\n" +
            "    \"koodiArvo\": \"61\",\n" +
            "    \"paivitysPvm\": \"2018-02-06\",\n" +
            "    \"paivittajaOid\": null,\n" +
            "    \"voimassaAlkuPvm\": \"1990-01-01\",\n" +
            "    \"voimassaLoppuPvm\": null,\n" +
            "    \"tila\": \"LUONNOS\",\n" +
            "    \"metadata\": [\n" +
            "      {\n" +
            "        \"nimi\": \"Musikläroanstalter\",\n" +
            "        \"kuvaus\": \"Musikläroanstalter\",\n" +
            "        \"lyhytNimi\": \"Musikläroanstalter\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"SV\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"nimi\": \"Music institutes\",\n" +
            "        \"kuvaus\": \"Music institutes\",\n" +
            "        \"lyhytNimi\": \"Music institutes\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"EN\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"nimi\": \"Musiikkioppilaitokset\",\n" +
            "        \"kuvaus\": \"Musiikkioppilaitokset\",\n" +
            "        \"lyhytNimi\": \"Musiikkioppilaitokset\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"FI\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"koodiUri\": \"oppilaitostyyppi_19\",\n" +
            "    \"resourceUri\": \"https://virkailija.testiopintopolku.fi/koodisto-service/rest/codeelement/oppilaitostyyppi_19\",\n" +
            "    \"version\": 10,\n" +
            "    \"versio\": 1,\n" +
            "    \"koodisto\": {\n" +
            "      \"koodistoUri\": \"oppilaitostyyppi\",\n" +
            "      \"organisaatioOid\": \"1.2.246.562.10.00000000001\",\n" +
            "      \"koodistoVersios\": [\n" +
            "        1\n" +
            "      ]\n" +
            "    },\n" +
            "    \"koodiArvo\": \"19\",\n" +
            "    \"paivitysPvm\": \"2018-02-06\",\n" +
            "    \"paivittajaOid\": null,\n" +
            "    \"voimassaAlkuPvm\": \"1990-01-01\",\n" +
            "    \"voimassaLoppuPvm\": null,\n" +
            "    \"tila\": \"LUONNOS\",\n" +
            "    \"metadata\": [\n" +
            "      {\n" +
            "        \"nimi\": \"Skolor som omfattar grundskole- och gymnasienivå\",\n" +
            "        \"kuvaus\": \"Skolor som omfattar grundskole- och gymnasienivå\",\n" +
            "        \"lyhytNimi\": \"Skolor som omfattar grundskole- och gymnasienivå\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"SV\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"nimi\": \"Perus- ja lukioasteen koulut\",\n" +
            "        \"kuvaus\": \"Perus- ja lukioasteen koulut\",\n" +
            "        \"lyhytNimi\": \"Perus- ja lukioasteen koulut\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"FI\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"nimi\": \"Basic and general upper secondary schools\",\n" +
            "        \"kuvaus\": \"Basic and general upper secondary schools\",\n" +
            "        \"lyhytNimi\": \"Basic and general upper secondary schools\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"EN\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"koodiUri\": \"oppilaitostyyppi_21\",\n" +
            "    \"resourceUri\": \"https://virkailija.testiopintopolku.fi/koodisto-service/rest/codeelement/oppilaitostyyppi_21\",\n" +
            "    \"version\": 14,\n" +
            "    \"versio\": 1,\n" +
            "    \"koodisto\": {\n" +
            "      \"koodistoUri\": \"oppilaitostyyppi\",\n" +
            "      \"organisaatioOid\": \"1.2.246.562.10.00000000001\",\n" +
            "      \"koodistoVersios\": [\n" +
            "        1\n" +
            "      ]\n" +
            "    },\n" +
            "    \"koodiArvo\": \"21\",\n" +
            "    \"paivitysPvm\": \"2018-02-06\",\n" +
            "    \"paivittajaOid\": null,\n" +
            "    \"voimassaAlkuPvm\": \"1990-01-01\",\n" +
            "    \"voimassaLoppuPvm\": null,\n" +
            "    \"tila\": \"LUONNOS\",\n" +
            "    \"metadata\": [\n" +
            "      {\n" +
            "        \"nimi\": \"Vocational institutions\",\n" +
            "        \"kuvaus\": \"Vocational institutions\",\n" +
            "        \"lyhytNimi\": \"Vocational institutions\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"EN\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"nimi\": \"Yrkesläroanstalter\",\n" +
            "        \"kuvaus\": \"Yrkesläroanstalter\",\n" +
            "        \"lyhytNimi\": \"Yrkesläroanstalter\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"SV\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"nimi\": \"Ammatilliset oppilaitokset\",\n" +
            "        \"kuvaus\": \"Ammatilliset oppilaitokset\",\n" +
            "        \"lyhytNimi\": \"Ammatilliset oppilaitokset\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"FI\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"koodiUri\": \"oppilaitostyyppi_24\",\n" +
            "    \"resourceUri\": \"https://virkailija.testiopintopolku.fi/koodisto-service/rest/codeelement/oppilaitostyyppi_24\",\n" +
            "    \"version\": 10,\n" +
            "    \"versio\": 1,\n" +
            "    \"koodisto\": {\n" +
            "      \"koodistoUri\": \"oppilaitostyyppi\",\n" +
            "      \"organisaatioOid\": \"1.2.246.562.10.00000000001\",\n" +
            "      \"koodistoVersios\": [\n" +
            "        1\n" +
            "      ]\n" +
            "    },\n" +
            "    \"koodiArvo\": \"24\",\n" +
            "    \"paivitysPvm\": \"2018-02-06\",\n" +
            "    \"paivittajaOid\": null,\n" +
            "    \"voimassaAlkuPvm\": \"1990-01-01\",\n" +
            "    \"voimassaLoppuPvm\": null,\n" +
            "    \"tila\": \"LUONNOS\",\n" +
            "    \"metadata\": [\n" +
            "      {\n" +
            "        \"nimi\": \"Vocational adult education centres\",\n" +
            "        \"kuvaus\": \"Vocational adult education centres\",\n" +
            "        \"lyhytNimi\": \"Vocational adult education centres\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"EN\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"nimi\": \"Ammatilliset aikuiskoulutuskeskukset\",\n" +
            "        \"kuvaus\": \"Ammatilliset aikuiskoulutuskeskukset\",\n" +
            "        \"lyhytNimi\": \"Ammatilliset aikuiskoulutuskeskukset\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"FI\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"nimi\": \"Yrkesutbildningscentrer för vuxna\",\n" +
            "        \"kuvaus\": \"Yrkesutbildningscentrer för vuxna\",\n" +
            "        \"lyhytNimi\": \"Yrkesutbildningscentrer för vuxna\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"SV\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"koodiUri\": \"oppilaitostyyppi_xx\",\n" +
            "    \"resourceUri\": \"https://virkailija.testiopintopolku.fi/koodisto-service/rest/codeelement/oppilaitostyyppi_xx\",\n" +
            "    \"version\": 10,\n" +
            "    \"versio\": 1,\n" +
            "    \"koodisto\": {\n" +
            "      \"koodistoUri\": \"oppilaitostyyppi\",\n" +
            "      \"organisaatioOid\": \"1.2.246.562.10.00000000001\",\n" +
            "      \"koodistoVersios\": [\n" +
            "        1\n" +
            "      ]\n" +
            "    },\n" +
            "    \"koodiArvo\": \"XX\",\n" +
            "    \"paivitysPvm\": \"2022-11-22\",\n" +
            "    \"paivittajaOid\": \"1.2.246.562.24.25909319372\",\n" +
            "    \"voimassaAlkuPvm\": \"1990-01-01\",\n" +
            "    \"voimassaLoppuPvm\": null,\n" +
            "    \"tila\": \"LUONNOS\",\n" +
            "    \"metadata\": [\n" +
            "      {\n" +
            "        \"nimi\": \"Okänd läroanstaltstyp\",\n" +
            "        \"kuvaus\": \"Okänd läroanstaltstyp\",\n" +
            "        \"lyhytNimi\": \"Okänd läroanstaltstyp\",\n" +
            "        \"kayttoohje\": null,\n" +
            "        \"kasite\": null,\n" +
            "        \"sisaltaaMerkityksen\": null,\n" +
            "        \"eiSisallaMerkitysta\": null,\n" +
            "        \"huomioitavaKoodi\": null,\n" +
            "        \"sisaltaaKoodiston\": null,\n" +
            "        \"kieli\": \"SV\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"nimi\": \"Ei tiedossa (oppilaitostyyppi)\",\n" +
            "        \"kuvaus\": \"Ei tiedossa (oppilaitostyyppi)\",\n" +
            "        \"lyhytNimi\": \"Ei tiedossa (oppilaitostyyppi)\",\n" +
            "        \"kayttoohje\": null,\n" +
            "        \"kasite\": null,\n" +
            "        \"sisaltaaMerkityksen\": null,\n" +
            "        \"eiSisallaMerkitysta\": null,\n" +
            "        \"huomioitavaKoodi\": null,\n" +
            "        \"sisaltaaKoodiston\": null,\n" +
            "        \"kieli\": \"FI\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"nimi\": \"Not known (type of institution)\",\n" +
            "        \"kuvaus\": \"Not known (type of institution)\",\n" +
            "        \"lyhytNimi\": \"Not known (type of institution)\",\n" +
            "        \"kayttoohje\": null,\n" +
            "        \"kasite\": null,\n" +
            "        \"sisaltaaMerkityksen\": null,\n" +
            "        \"eiSisallaMerkitysta\": null,\n" +
            "        \"huomioitavaKoodi\": null,\n" +
            "        \"sisaltaaKoodiston\": null,\n" +
            "        \"kieli\": \"EN\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"koodiUri\": \"oppilaitostyyppi_62\",\n" +
            "    \"resourceUri\": \"https://virkailija.testiopintopolku.fi/koodisto-service/rest/codeelement/oppilaitostyyppi_62\",\n" +
            "    \"version\": 11,\n" +
            "    \"versio\": 1,\n" +
            "    \"koodisto\": {\n" +
            "      \"koodistoUri\": \"oppilaitostyyppi\",\n" +
            "      \"organisaatioOid\": \"1.2.246.562.10.00000000001\",\n" +
            "      \"koodistoVersios\": [\n" +
            "        1\n" +
            "      ]\n" +
            "    },\n" +
            "    \"koodiArvo\": \"62\",\n" +
            "    \"paivitysPvm\": \"2018-02-06\",\n" +
            "    \"paivittajaOid\": null,\n" +
            "    \"voimassaAlkuPvm\": \"1990-01-01\",\n" +
            "    \"voimassaLoppuPvm\": null,\n" +
            "    \"tila\": \"LUONNOS\",\n" +
            "    \"metadata\": [\n" +
            "      {\n" +
            "        \"nimi\": \"Liikunnan koulutuskeskukset\",\n" +
            "        \"kuvaus\": \"Liikunnan koulutuskeskukset\",\n" +
            "        \"lyhytNimi\": \"Liikunnan koulutuskeskukset\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"FI\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"nimi\": \"Idrottsutbildningscentrer\",\n" +
            "        \"kuvaus\": \"Idrottsutbildningscentrer\",\n" +
            "        \"lyhytNimi\": \"Idrottsutbildningscentrer\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"SV\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"nimi\": \"Sports institutes\",\n" +
            "        \"kuvaus\": \"Sports institutes\",\n" +
            "        \"lyhytNimi\": \"Sports institutes\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"EN\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"koodiUri\": \"oppilaitostyyppi_63\",\n" +
            "    \"resourceUri\": \"https://virkailija.testiopintopolku.fi/koodisto-service/rest/codeelement/oppilaitostyyppi_63\",\n" +
            "    \"version\": 18,\n" +
            "    \"versio\": 1,\n" +
            "    \"koodisto\": {\n" +
            "      \"koodistoUri\": \"oppilaitostyyppi\",\n" +
            "      \"organisaatioOid\": \"1.2.246.562.10.00000000001\",\n" +
            "      \"koodistoVersios\": [\n" +
            "        1\n" +
            "      ]\n" +
            "    },\n" +
            "    \"koodiArvo\": \"63\",\n" +
            "    \"paivitysPvm\": \"2018-02-06\",\n" +
            "    \"paivittajaOid\": null,\n" +
            "    \"voimassaAlkuPvm\": \"1990-01-01\",\n" +
            "    \"voimassaLoppuPvm\": null,\n" +
            "    \"tila\": \"LUONNOS\",\n" +
            "    \"metadata\": [\n" +
            "      {\n" +
            "        \"nimi\": \"Folk high schools\",\n" +
            "        \"kuvaus\": \"Folk high schools\",\n" +
            "        \"lyhytNimi\": \"Folk high schools\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"EN\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"nimi\": \"Folkhögskolor\",\n" +
            "        \"kuvaus\": \"Folkhögskolor\",\n" +
            "        \"lyhytNimi\": \"Folkhögskolor\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"SV\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"nimi\": \"Kansanopistot\",\n" +
            "        \"kuvaus\": \"Kansanopistot\",\n" +
            "        \"lyhytNimi\": \"Kansanopistot\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"FI\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"koodiUri\": \"oppilaitostyyppi_65\",\n" +
            "    \"resourceUri\": \"https://virkailija.testiopintopolku.fi/koodisto-service/rest/codeelement/oppilaitostyyppi_65\",\n" +
            "    \"version\": 3,\n" +
            "    \"versio\": 1,\n" +
            "    \"koodisto\": {\n" +
            "      \"koodistoUri\": \"oppilaitostyyppi\",\n" +
            "      \"organisaatioOid\": \"1.2.246.562.10.00000000001\",\n" +
            "      \"koodistoVersios\": [\n" +
            "        1\n" +
            "      ]\n" +
            "    },\n" +
            "    \"koodiArvo\": \"65\",\n" +
            "    \"paivitysPvm\": \"2020-11-04\",\n" +
            "    \"paivittajaOid\": \"1.2.246.562.24.25909319372\",\n" +
            "    \"voimassaAlkuPvm\": \"1990-01-01\",\n" +
            "    \"voimassaLoppuPvm\": null,\n" +
            "    \"tila\": \"LUONNOS\",\n" +
            "    \"metadata\": [\n" +
            "      {\n" +
            "        \"nimi\": \"Study circle centres\",\n" +
            "        \"kuvaus\": \"Study circle centres\",\n" +
            "        \"lyhytNimi\": \"Study circle centres\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"EN\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"nimi\": \"Studiecentraler\",\n" +
            "        \"kuvaus\": \"Studiecentraler\",\n" +
            "        \"lyhytNimi\": \"Studiecentraler\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"SV\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"nimi\": \"Opintokeskukset\",\n" +
            "        \"kuvaus\": \"Opintokeskukset\",\n" +
            "        \"lyhytNimi\": \"Opintokeskukset\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"FI\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"koodiUri\": \"oppilaitostyyppi_11\",\n" +
            "    \"resourceUri\": \"https://virkailija.testiopintopolku.fi/koodisto-service/rest/codeelement/oppilaitostyyppi_11\",\n" +
            "    \"version\": 6,\n" +
            "    \"versio\": 1,\n" +
            "    \"koodisto\": {\n" +
            "      \"koodistoUri\": \"oppilaitostyyppi\",\n" +
            "      \"organisaatioOid\": \"1.2.246.562.10.00000000001\",\n" +
            "      \"koodistoVersios\": [\n" +
            "        1\n" +
            "      ]\n" +
            "    },\n" +
            "    \"koodiArvo\": \"11\",\n" +
            "    \"paivitysPvm\": \"2018-02-06\",\n" +
            "    \"paivittajaOid\": null,\n" +
            "    \"voimassaAlkuPvm\": \"1990-01-01\",\n" +
            "    \"voimassaLoppuPvm\": null,\n" +
            "    \"tila\": \"LUONNOS\",\n" +
            "    \"metadata\": [\n" +
            "      {\n" +
            "        \"nimi\": \"Grundskolor\",\n" +
            "        \"kuvaus\": \"Grundskolor\",\n" +
            "        \"lyhytNimi\": \"Grundskolor\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"SV\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"nimi\": \"Comprehensive schools\",\n" +
            "        \"kuvaus\": \"Comprehensive schools\",\n" +
            "        \"lyhytNimi\": \"Comprehensive schools\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"EN\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"nimi\": \"Peruskoulut\",\n" +
            "        \"kuvaus\": \"Peruskoulut\",\n" +
            "        \"lyhytNimi\": \"Peruskoulut\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"FI\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"koodiUri\": \"oppilaitostyyppi_42\",\n" +
            "    \"resourceUri\": \"https://virkailija.testiopintopolku.fi/koodisto-service/rest/codeelement/oppilaitostyyppi_42\",\n" +
            "    \"version\": 6,\n" +
            "    \"versio\": 1,\n" +
            "    \"koodisto\": {\n" +
            "      \"koodistoUri\": \"oppilaitostyyppi\",\n" +
            "      \"organisaatioOid\": \"1.2.246.562.10.00000000001\",\n" +
            "      \"koodistoVersios\": [\n" +
            "        1\n" +
            "      ]\n" +
            "    },\n" +
            "    \"koodiArvo\": \"42\",\n" +
            "    \"paivitysPvm\": \"2018-02-06\",\n" +
            "    \"paivittajaOid\": null,\n" +
            "    \"voimassaAlkuPvm\": \"1990-01-01\",\n" +
            "    \"voimassaLoppuPvm\": null,\n" +
            "    \"tila\": \"LUONNOS\",\n" +
            "    \"metadata\": [\n" +
            "      {\n" +
            "        \"nimi\": \"Universities\",\n" +
            "        \"kuvaus\": \"Universities\",\n" +
            "        \"lyhytNimi\": \"Universities\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"EN\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"nimi\": \"Universitet\",\n" +
            "        \"kuvaus\": \"Universitet\",\n" +
            "        \"lyhytNimi\": \"Universitet\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"SV\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"nimi\": \"Yliopistot\",\n" +
            "        \"kuvaus\": \"Yliopistot\",\n" +
            "        \"lyhytNimi\": \"Yliopistot\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"FI\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"koodiUri\": \"oppilaitostyyppi_43\",\n" +
            "    \"resourceUri\": \"https://virkailija.testiopintopolku.fi/koodisto-service/rest/codeelement/oppilaitostyyppi_43\",\n" +
            "    \"version\": 5,\n" +
            "    \"versio\": 1,\n" +
            "    \"koodisto\": {\n" +
            "      \"koodistoUri\": \"oppilaitostyyppi\",\n" +
            "      \"organisaatioOid\": \"1.2.246.562.10.00000000001\",\n" +
            "      \"koodistoVersios\": [\n" +
            "        1\n" +
            "      ]\n" +
            "    },\n" +
            "    \"koodiArvo\": \"43\",\n" +
            "    \"paivitysPvm\": \"2018-02-06\",\n" +
            "    \"paivittajaOid\": null,\n" +
            "    \"voimassaAlkuPvm\": \"1990-01-01\",\n" +
            "    \"voimassaLoppuPvm\": null,\n" +
            "    \"tila\": \"LUONNOS\",\n" +
            "    \"metadata\": [\n" +
            "      {\n" +
            "        \"nimi\": \"Sotilaskorkeakoulut\",\n" +
            "        \"kuvaus\": \"Sotilaskorkeakoulut\",\n" +
            "        \"lyhytNimi\": \"Sotilaskorkeakoulut\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"FI\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"nimi\": \"Military universities\",\n" +
            "        \"kuvaus\": \"Military universities\",\n" +
            "        \"lyhytNimi\": \"Military universities\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"EN\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"nimi\": \"Militära högskolor\",\n" +
            "        \"kuvaus\": \"Militära högskolor\",\n" +
            "        \"lyhytNimi\": \"Militära högskolor\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"SV\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"koodiUri\": \"oppilaitostyyppi_23\",\n" +
            "    \"resourceUri\": \"https://virkailija.testiopintopolku.fi/koodisto-service/rest/codeelement/oppilaitostyyppi_23\",\n" +
            "    \"version\": 8,\n" +
            "    \"versio\": 1,\n" +
            "    \"koodisto\": {\n" +
            "      \"koodistoUri\": \"oppilaitostyyppi\",\n" +
            "      \"organisaatioOid\": \"1.2.246.562.10.00000000001\",\n" +
            "      \"koodistoVersios\": [\n" +
            "        1\n" +
            "      ]\n" +
            "    },\n" +
            "    \"koodiArvo\": \"23\",\n" +
            "    \"paivitysPvm\": \"2018-02-06\",\n" +
            "    \"paivittajaOid\": null,\n" +
            "    \"voimassaAlkuPvm\": \"1990-01-01\",\n" +
            "    \"voimassaLoppuPvm\": null,\n" +
            "    \"tila\": \"LUONNOS\",\n" +
            "    \"metadata\": [\n" +
            "      {\n" +
            "        \"nimi\": \"Specialised vocational education and training institutions\",\n" +
            "        \"kuvaus\": \"Specialised vocational education and training institutions\",\n" +
            "        \"lyhytNimi\": \"Specialised vocational education and training institutions\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"EN\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"nimi\": \"Ammatilliset erikoisoppilaitokset\",\n" +
            "        \"kuvaus\": \"Ammatilliset erikoisoppilaitokset\",\n" +
            "        \"lyhytNimi\": \"Ammatilliset erikoisoppilaitokset\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"FI\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"nimi\": \"Särskilda yrkesläroanstalter\",\n" +
            "        \"kuvaus\": \"Särskilda yrkesläroanstalter\",\n" +
            "        \"lyhytNimi\": \"Särskilda yrkesläroanstalter\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"SV\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"koodiUri\": \"oppilaitostyyppi_15\",\n" +
            "    \"resourceUri\": \"https://virkailija.testiopintopolku.fi/koodisto-service/rest/codeelement/oppilaitostyyppi_15\",\n" +
            "    \"version\": 12,\n" +
            "    \"versio\": 1,\n" +
            "    \"koodisto\": {\n" +
            "      \"koodistoUri\": \"oppilaitostyyppi\",\n" +
            "      \"organisaatioOid\": \"1.2.246.562.10.00000000001\",\n" +
            "      \"koodistoVersios\": [\n" +
            "        1\n" +
            "      ]\n" +
            "    },\n" +
            "    \"koodiArvo\": \"15\",\n" +
            "    \"paivitysPvm\": \"2019-09-25\",\n" +
            "    \"paivittajaOid\": \"1.2.246.562.24.51707373173\",\n" +
            "    \"voimassaAlkuPvm\": \"1990-01-01\",\n" +
            "    \"voimassaLoppuPvm\": null,\n" +
            "    \"tila\": \"LUONNOS\",\n" +
            "    \"metadata\": [\n" +
            "      {\n" +
            "        \"nimi\": \"General upper secondary schools\",\n" +
            "        \"kuvaus\": \"General upper secondary schools\",\n" +
            "        \"lyhytNimi\": \"General upper secondary schools\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"EN\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"nimi\": \"Lukiot\",\n" +
            "        \"kuvaus\": \"Lukiot\",\n" +
            "        \"lyhytNimi\": \"Lukiot\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"FI\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"nimi\": \"Gymnasier\",\n" +
            "        \"kuvaus\": \"Gymnasier\",\n" +
            "        \"lyhytNimi\": \"Gymnasier\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"SV\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"koodiUri\": \"oppilaitostyyppi_29\",\n" +
            "    \"resourceUri\": \"https://virkailija.testiopintopolku.fi/koodisto-service/rest/codeelement/oppilaitostyyppi_29\",\n" +
            "    \"version\": 3,\n" +
            "    \"versio\": 1,\n" +
            "    \"koodisto\": {\n" +
            "      \"koodistoUri\": \"oppilaitostyyppi\",\n" +
            "      \"organisaatioOid\": \"1.2.246.562.10.00000000001\",\n" +
            "      \"koodistoVersios\": [\n" +
            "        1\n" +
            "      ]\n" +
            "    },\n" +
            "    \"koodiArvo\": \"29\",\n" +
            "    \"paivitysPvm\": \"2020-11-04\",\n" +
            "    \"paivittajaOid\": \"1.2.246.562.24.25909319372\",\n" +
            "    \"voimassaAlkuPvm\": \"1990-01-01\",\n" +
            "    \"voimassaLoppuPvm\": null,\n" +
            "    \"tila\": \"LUONNOS\",\n" +
            "    \"metadata\": [\n" +
            "      {\n" +
            "        \"nimi\": \"Military vocational institutes\",\n" +
            "        \"kuvaus\": \"Military vocational institutes\",\n" +
            "        \"lyhytNimi\": \"Military vocational institutes\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"EN\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"nimi\": \"Sotilasalan ammatilliset oppilaitokset\",\n" +
            "        \"kuvaus\": \"Sotilasalan ammatilliset oppilaitokset\",\n" +
            "        \"lyhytNimi\": \"Sotilasalan ammatilliset oppilaitokset\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"FI\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"nimi\": \"Militära yrkesläroanstalter\",\n" +
            "        \"kuvaus\": \"Militära yrkesläroanstalter\",\n" +
            "        \"lyhytNimi\": \"Militära yrkesläroanstalter\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"SV\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"koodiUri\": \"oppilaitostyyppi_66\",\n" +
            "    \"resourceUri\": \"https://virkailija.testiopintopolku.fi/koodisto-service/rest/codeelement/oppilaitostyyppi_66\",\n" +
            "    \"version\": 5,\n" +
            "    \"versio\": 1,\n" +
            "    \"koodisto\": {\n" +
            "      \"koodistoUri\": \"oppilaitostyyppi\",\n" +
            "      \"organisaatioOid\": \"1.2.246.562.10.00000000001\",\n" +
            "      \"koodistoVersios\": [\n" +
            "        1\n" +
            "      ]\n" +
            "    },\n" +
            "    \"koodiArvo\": \"66\",\n" +
            "    \"paivitysPvm\": \"2018-02-06\",\n" +
            "    \"paivittajaOid\": null,\n" +
            "    \"voimassaAlkuPvm\": \"1990-01-01\",\n" +
            "    \"voimassaLoppuPvm\": null,\n" +
            "    \"tila\": \"LUONNOS\",\n" +
            "    \"metadata\": [\n" +
            "      {\n" +
            "        \"nimi\": \"Sommaruniversitet\",\n" +
            "        \"kuvaus\": \"Sommaruniversitet\",\n" +
            "        \"lyhytNimi\": \"Sommaruniversitet\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"SV\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"nimi\": \"Kesäyliopistot\",\n" +
            "        \"kuvaus\": \"Kesäyliopistot\",\n" +
            "        \"lyhytNimi\": \"Kesäyliopistot\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"FI\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"nimi\": \"Summer universities\",\n" +
            "        \"kuvaus\": \"Summer universities\",\n" +
            "        \"lyhytNimi\": \"Summer universities\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"EN\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"koodiUri\": \"oppilaitostyyppi_99\",\n" +
            "    \"resourceUri\": \"https://virkailija.testiopintopolku.fi/koodisto-service/rest/codeelement/oppilaitostyyppi_99\",\n" +
            "    \"version\": 8,\n" +
            "    \"versio\": 1,\n" +
            "    \"koodisto\": {\n" +
            "      \"koodistoUri\": \"oppilaitostyyppi\",\n" +
            "      \"organisaatioOid\": \"1.2.246.562.10.00000000001\",\n" +
            "      \"koodistoVersios\": [\n" +
            "        1\n" +
            "      ]\n" +
            "    },\n" +
            "    \"koodiArvo\": \"99\",\n" +
            "    \"paivitysPvm\": \"2020-11-04\",\n" +
            "    \"paivittajaOid\": \"1.2.246.562.24.25909319372\",\n" +
            "    \"voimassaAlkuPvm\": \"1990-01-01\",\n" +
            "    \"voimassaLoppuPvm\": null,\n" +
            "    \"tila\": \"LUONNOS\",\n" +
            "    \"metadata\": [\n" +
            "      {\n" +
            "        \"nimi\": \"Övriga läroanstalter\",\n" +
            "        \"kuvaus\": \"Övriga läroanstalter\",\n" +
            "        \"lyhytNimi\": \"Övriga läroanstalter\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"SV\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"nimi\": \"Other educational institutions\",\n" +
            "        \"kuvaus\": \"Other educational institutions\",\n" +
            "        \"lyhytNimi\": \"Other educational institutions\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"EN\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"nimi\": \"Muut oppilaitokset\",\n" +
            "        \"kuvaus\": \"Muut oppilaitokset\",\n" +
            "        \"lyhytNimi\": \"Muut oppilaitokset\",\n" +
            "        \"kayttoohje\": \"\",\n" +
            "        \"kasite\": \"\",\n" +
            "        \"sisaltaaMerkityksen\": \"\",\n" +
            "        \"eiSisallaMerkitysta\": \"\",\n" +
            "        \"huomioitavaKoodi\": \"\",\n" +
            "        \"sisaltaaKoodiston\": \"\",\n" +
            "        \"kieli\": \"FI\"\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "]";
}

class RawJson {
    private String payload;

    public RawJson(String payload) {
        this.payload = payload;
    }

    public static RawJson from(String payload) {
        return new RawJson(payload);
    }

    @JsonValue
    @JsonRawValue
    public String getPayload() {
        return this.payload;
    }
}
