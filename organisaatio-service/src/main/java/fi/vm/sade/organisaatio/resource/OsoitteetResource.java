package fi.vm.sade.organisaatio.resource;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.model.Email;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.Osoite;
import fi.vm.sade.organisaatio.model.Puhelinnumero;
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
    private final EntityManager em;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

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
        List<Organisaatio> orgs = fetchOrganisaatiosWithYhteystiedot(organisaatioIds);
        Map<Long, String> orgNimet = fetchNimet(organisaatioIds, kieli);
        Map<String, String> opetuskieletMap = fetchOpetuskielet(kieli);
        Map<String, String> kuntaMap = fetchKuntaKoodisto(kieli);
        Map<String, String> postinumeroMap = fetchPostikoodis(kieli);


        List<Hakutulos> result = orgs.stream().map(o -> {
                    Optional<String> sahkoposti = Optional.ofNullable(o.getEmail(kieliKoodi)).map(Email::getEmail);
                    Optional<String> puhelinnumero = Optional.ofNullable(o.getPuhelin(Puhelinnumero.TYYPPI_PUHELIN, kieliKoodi)).map(Puhelinnumero::getPuhelinnumero);
                    Set<String> opetuskielet = o.getKielet().stream().map(k -> parseOpetuskieliKoodi(opetuskieletMap, k)).collect(Collectors.toSet());
                    return new Hakutulos(
                            o.getId(),
                            o.getOid(),
                            orgNimet.get(o.getId()),
                            sahkoposti,
                            o.getYritysmuoto(),
                            puhelinnumero,
                            opetuskielet.isEmpty() ? Optional.empty() : Optional.of(String.join(", ", opetuskielet)),
                            Optional.ofNullable(o.getOppilaitosKoodi()),
                            kuntaMap.get(o.getKotipaikka()),
                            Optional.empty(),
                            o.getYtunnus(),
                            Optional.ofNullable(o.getPostiosoite()).map(osoite -> osoiteToString(postinumeroMap, osoite)),
                            Optional.ofNullable(o.getKayntiosoite()).map(osoite -> osoiteToString(postinumeroMap, osoite))
                    );
                })
                .collect(Collectors.toList());


        log.info("Hakutuloksia: {}", result.size());
        result.sort(Comparator.comparing(Hakutulos::getNimi));
        return result;
    }

    private Map<String, String> fetchKuntaKoodisto(String kieli) {
        String sql = "fi".equals(kieli)
                ? "SELECT koodiuri, nimi_fi AS nimi FROM koodisto_kunta WHERE nimi_fi IS NOT NULL"
                : "SELECT koodiuri, nimi_sv AS nimi FROM koodisto_kunta WHERE nimi_sv IS NOT NULL";
        return jdbcTemplate.query(sql, (rs, i) ->
                Map.entry(rs.getString("koodiuri"), rs.getString("nimi"))
        ).stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, HashMap::new));
    }

    private Map<String, String> fetchOpetuskielet(String kieli) {
        String sql = "fi".equals(kieli)
                ? "SELECT koodiuri, nimi_fi AS nimi FROM koodisto_oppilaitoksenopetuskieli WHERE nimi_fi IS NOT NULL"
                : "SELECT koodiuri, nimi_sv AS nimi FROM koodisto_oppilaitoksenopetuskieli WHERE nimi_sv IS NOT NULL";
        return jdbcTemplate.query(sql, (rs, i) ->
                Map.entry(rs.getString("koodiuri"), rs.getString("nimi"))
        ).stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, HashMap::new));
    }

    private Map<String, String> fetchPostikoodis(String kieli) {
        String sql = "fi".equals(kieli)
                ? "SELECT koodiuri, koodiarvo, nimi_fi AS nimi FROM koodisto_posti WHERE nimi_fi IS NOT NULL"
                : "SELECT koodiuri, koodiarvo, nimi_sv AS nimi FROM koodisto_posti WHERE nimi_sv IS NOT NULL";
        return jdbcTemplate.query(sql, (rs, i) ->
                Map.entry(rs.getString("koodiuri"), String.format("%s %s", rs.getString("koodiarvo"), rs.getString("nimi")))
        ).stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, HashMap::new));
    }

    private String parseOpetuskieliKoodi(Map<String, String> opetuskielet, String versioituKoodiUri) {
        int hashIndex = versioituKoodiUri.lastIndexOf('#');
        String koodiuri = hashIndex == -1 ? versioituKoodiUri : versioituKoodiUri.substring(0, hashIndex);
        return opetuskielet.get(koodiuri);
    }

    private List<Organisaatio> fetchOrganisaatiosWithYhteystiedot(List<Long> organisaatioIds) {
        return em.createQuery("SELECT DISTINCT o FROM Organisaatio o" +
                        " LEFT JOIN FETCH o.yhteystiedot" +
                        " LEFT JOIN FETCH o.kielet" +
                        " LEFT JOIN FETCH o.parentOids" + // Haetaan vain jotta Hibernate ei tee näille N+1 kyselyä
                        " WHERE o.id IN (:ids)", Organisaatio.class)
                .setParameter("ids", organisaatioIds).getResultList();
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
        String sql = "SELECT DISTINCT o.id FROM organisaatio o" +
                " JOIN organisaatio_tyypit ON (organisaatio_id = o.id AND tyypit = :organisaatiotyyppi)" +
                " WHERE o.alkupvm <= current_date AND (o.lakkautuspvm IS NULL OR current_date < o.lakkautuspvm) AND NOT o.organisaatiopoistettu";
        return jdbcTemplate.query(sql, Map.of("organisaatiotyyppi", organisaatiotyyppi), (rs, rowNum) -> rs.getLong("id"));
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

    private String osoiteToString(Map<String, String> postinumeroMap, Osoite osoite) {
        if (osoite.getPostinumero() == null || osoite.getPostitoimipaikka() == null)
            return osoite.getOsoite();
        String postiKoodi = postinumeroMap.get(osoite.getPostinumero());
        if (postiKoodi == null)
            return osoite.getOsoite();

        return String.format("%s, %s", osoite.getOsoite(), postiKoodi);
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
    public Parametrit getParametrit() {
        Map<String, List<String>> ryhmat = Map.of(
                "Perusopetus", List.of("oppilaitostyyppi_12#1", "oppilaitostyyppi_11#1", "oppilaitostyyppi_19#1"),
                "Lukiokoulutus", List.of("oppilaitostyyppi_15#1", "oppilaitostyyppi_19#1"),
                "Ammatillinen koulutus", List.of("oppilaitostyyppi_63#1", "oppilaitostyyppi_29#1", "oppilaitostyyppi_61#1", "oppilaitostyyppi_22#1", "oppilaitostyyppi_21#1", "oppilaitostyyppi_24#1", "oppilaitostyyppi_62#1", "oppilaitostyyppi_23#1"),
                "Korkeakoulutus", List.of("oppilaitostyyppi_42#1", "oppilaitostyyppi_43#1", "oppilaitostyyppi_41#1"),
                "Vapaan sivistystyön koulutus", List.of("oppilaitostyyppi_65#1", "oppilaitostyyppi_62#1", "oppilaitostyyppi_66#1", "oppilaitostyyppi_63#1", "oppilaitostyyppi_64#1"),
                "Taiteen perusopetus", List.of("oppilaitostyyppi_01#1", "oppilaitostyyppi_61#1")
        );
        List<OppilaitostyyppiKoodi> oppilaitostyyppiKoodis = jdbcTemplate.query(
                "SELECT concat(koodiuri, '#', versio) AS koodi, nimi_fi AS nimi FROM koodisto_oppilaitostyyppi",
                (rs, rowNum) -> new OppilaitostyyppiKoodi(rs.getString("koodi"), rs.getString("nimi"))
        );
        return new Parametrit(new OppilaitostyyppiParametrit(oppilaitostyyppiKoodis, ryhmat));
    }
}

@Data
class Parametrit {
    private final OppilaitostyyppiParametrit oppilaitostyypit;
}

@Data
class OppilaitostyyppiParametrit {
    private final List<OppilaitostyyppiKoodi> koodit;
    private final Map<String, List<String>> ryhmat;
}

@Data
class OppilaitostyyppiKoodi {
    private final String koodiUri;
    private final String nimi;
}
