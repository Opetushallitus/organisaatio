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
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Hidden
@RestController
@RequestMapping("${server.internal.context-path}/osoitteet")
@RequiredArgsConstructor
@Slf4j
public class OsoitteetResource {
    private final OrganisaatioRepository organisaatioRepository;
    private final EntityManager em;
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @GetMapping(value = "/hae", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ROLE_APP_OSOITE_CRUD')")
    public List<Hakutulos> hae(
            @RequestParam("organisaatiotyypit[]") List<String> organisaatiotyypit,
            @RequestParam(value = "oppilaitostyypit[]", defaultValue = "", required = false) List<String> oppilaitostyypit
    ) throws InterruptedException {
        String kieli = "fi";
        String kieliKoodi = "kieli_fi#1";
        String organisaatiotyyppi = OrganisaatioTyyppi.KOULUTUSTOIMIJA.koodiValue();
        List<Long> organisaatioIds = new ArrayList<>();

        if (OrganisaatioTyyppi.KOULUTUSTOIMIJA.koodiValue().equals(organisaatiotyyppi)) {
            if (oppilaitostyypit.isEmpty()) {
                organisaatioIds.addAll(searchByOrganisaatioTyyppi(organisaatiotyyppi));
            } else {
                organisaatioIds.addAll(findKoulutustoimijatHavingOppilaitosUnderThemWithOppilaitostyyppi(oppilaitostyypit));
            }
        }


        return makeSearchResult(organisaatioIds, kieli, kieliKoodi);
    }

    private List<Hakutulos> makeSearchResult(List<Long> organisaatioIds, String kieli, String kieliKoodi) {
        Map<Long, String> orgNimet = fetchNimet(organisaatioIds, kieli);

        List<Hakutulos> result = organisaatioRepository.findAllById(organisaatioIds).stream()
                .map(o -> {
                    Optional<String> sahkoposti = Optional.ofNullable(o.getEmail(kieliKoodi)).map(Email::getEmail);
                    Optional<String> puhelinnumero = Optional.ofNullable(o.getPuhelin(Puhelinnumero.TYYPPI_PUHELIN, kieliKoodi)).map(Puhelinnumero::getPuhelinnumero);
                    return new Hakutulos(
                            o.getId(),
                            o.getOid(),
                            orgNimet.get(o.getId()),
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

    private HashMap<Long, String> fetchNimet(List<Long> organisaatioIds, String kieli) {
        if (organisaatioIds.isEmpty())
            return new HashMap<>();

        String sql = "SELECT organisaatio.id, monikielinenteksti_values.value FROM organisaatio" +
                " JOIN monikielinenteksti_values ON monikielinenteksti_values.id = organisaatio.nimi_mkt" +
                " WHERE organisaatio.id IN (:ids) AND monikielinenteksti_values.key = :kieli";
        try (Stream<Map.Entry<Long, String>> stream = jdbcTemplate.queryForStream(
                sql,
                Map.of("ids", organisaatioIds, "kieli", kieli),
                (rs, rowNum) -> Map.entry(rs.getLong("id"), rs.getString("value"))
        )) {
            return stream.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, HashMap::new));
        }

    }

    private List<Long> searchByOrganisaatioTyyppi(String organisaatiotyyppi) {
        return em.createQuery(
                        "SELECT o.id FROM Organisaatio o" +
                                " WHERE :organisaatiotyyppi MEMBER OF o.tyypit" +
                                " AND (o.lakkautusPvm IS NULL OR o.lakkautusPvm > current_date())" +
                                " AND o.organisaatioPoistettu != true",
                        Long.class
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
        String sql = "SELECT DISTINCT parent.id" +
                " FROM organisaatio parent" +
                " JOIN organisaatio_tyypit parent_tyypit ON (parent_tyypit.organisaatio_id = parent.id AND parent_tyypit.tyypit = :koulutustoimijaKoodi)" +
                " JOIN organisaatiosuhde suhde ON (suhde.parent_id = parent.id)" +
                " JOIN organisaatio child ON (child.id = suhde.child_id)" +
                " JOIN organisaatio_tyypit child_tyypit ON (child_tyypit.organisaatio_id = child.id AND child_tyypit.tyypit = :oppilaitosKoodi)" +
                " WHERE suhde.alkupvm <= current_date AND (suhde.loppupvm IS NULL OR current_date < suhde.loppupvm)" +
                " AND parent.alkupvm <= current_date AND (parent.lakkautuspvm IS NULL OR current_date < parent.lakkautuspvm) AND NOT parent.organisaatiopoistettu" +
                " AND child.alkupvm <= current_date AND (child.lakkautuspvm IS NULL OR current_date < child.lakkautuspvm) AND NOT child.organisaatiopoistettu" +
                " AND child.oppilaitostyyppi IN (:oppilaitostyypit)";
        Map<String, Object> params = Map.of(
                "koulutustoimijaKoodi", OrganisaatioTyyppi.KOULUTUSTOIMIJA.koodiValue(),
                "oppilaitosKoodi", OrganisaatioTyyppi.OPPILAITOS.koodiValue(),
                "oppilaitostyypit", oppilaitostyypit
        );
        return jdbcTemplate.query(sql, params, (rs, rowNum) -> rs.getLong("id"));
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
            "    \"koodiUri\": \"oppilaitostyyppi_22#1\",\n" +
            "    \"nimi\": \"Ammatilliset erityisoppilaitokset\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"koodiUri\": \"oppilaitostyyppi_28#1\",\n" +
            "    \"nimi\": \"Palo-, poliisi- ja vartiointialojen oppilaitokset\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"koodiUri\": \"oppilaitostyyppi_93#1\",\n" +
            "    \"nimi\": \"Muut koulutuksen järjestäjät\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"koodiUri\": \"oppilaitostyyppi_12#1\",\n" +
            "    \"nimi\": \"Peruskouluasteen erityiskoulut\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"koodiUri\": \"oppilaitostyyppi_41#1\",\n" +
            "    \"nimi\": \"Ammattikorkeakoulut\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"koodiUri\": \"oppilaitostyyppi_64#1\",\n" +
            "    \"nimi\": \"Kansalaisopistot\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"koodiUri\": \"oppilaitostyyppi_01#1\",\n" +
            "    \"nimi\": \"Taiteen perusopetuksen oppilaitokset (ei musiikki)\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"koodiUri\": \"oppilaitostyyppi_61#1\",\n" +
            "    \"nimi\": \"Musiikkioppilaitokset\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"koodiUri\": \"oppilaitostyyppi_19#1\",\n" +
            "    \"nimi\": \"Perus- ja lukioasteen koulut\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"koodiUri\": \"oppilaitostyyppi_21#1\",\n" +
            "    \"nimi\": \"Ammatilliset oppilaitokset\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"koodiUri\": \"oppilaitostyyppi_24#1\",\n" +
            "    \"nimi\": \"Ammatilliset aikuiskoulutuskeskukset\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"koodiUri\": \"oppilaitostyyppi_xx#1\",\n" +
            "    \"nimi\": \"Ei tiedossa (oppilaitostyyppi)\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"koodiUri\": \"oppilaitostyyppi_62#1\",\n" +
            "    \"nimi\": \"Liikunnan koulutuskeskukset\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"koodiUri\": \"oppilaitostyyppi_63#1\",\n" +
            "    \"nimi\": \"Kansanopistot\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"koodiUri\": \"oppilaitostyyppi_65#1\",\n" +
            "    \"nimi\": \"Opintokeskukset\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"koodiUri\": \"oppilaitostyyppi_11#1\",\n" +
            "    \"nimi\": \"Peruskoulut\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"koodiUri\": \"oppilaitostyyppi_42#1\",\n" +
            "    \"nimi\": \"Yliopistot\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"koodiUri\": \"oppilaitostyyppi_43#1\",\n" +
            "    \"nimi\": \"Sotilaskorkeakoulut\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"koodiUri\": \"oppilaitostyyppi_23#1\",\n" +
            "    \"nimi\": \"Ammatilliset erikoisoppilaitokset\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"koodiUri\": \"oppilaitostyyppi_15#1\",\n" +
            "    \"nimi\": \"Lukiot\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"koodiUri\": \"oppilaitostyyppi_29#1\",\n" +
            "    \"nimi\": \"Sotilasalan ammatilliset oppilaitokset\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"koodiUri\": \"oppilaitostyyppi_66#1\",\n" +
            "    \"nimi\": \"Kesäyliopistot\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"koodiUri\": \"oppilaitostyyppi_99#1\",\n" +
            "    \"nimi\": \"Muut oppilaitokset\"\n" +
            "  }\n" +
            "]\n";
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
