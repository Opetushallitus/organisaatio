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
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.poi.ss.util.CellUtil.createCell;

@Hidden
@RestController
@RequestMapping("${server.internal.context-path}/osoitteet")
@RequiredArgsConstructor
@Slf4j
public class OsoitteetResource {
    private final EntityManager em;
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @PostMapping(value = "/hae", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ROLE_APP_OSOITE_CRUD')")
    public List<Hakutulos> hae(@RequestBody HaeRequest request) {
        return getHakutulos(request);
    }

    private List<Hakutulos> getHakutulos(HaeRequest request) {
        List<String> vuosiluokat = request.getVuosiluokat();
        List<String> oppilaitostyypit = request.getOppilaitostyypit();
        String kieli = "fi";
        String kieliKoodi = "kieli_fi#1";
        String organisaatiotyyppi = OrganisaatioTyyppi.KOULUTUSTOIMIJA.koodiValue();
        List<Long> organisaatioIds = new ArrayList<>();

        if (OrganisaatioTyyppi.KOULUTUSTOIMIJA.koodiValue().equals(organisaatiotyyppi)) {
            if (!vuosiluokat.isEmpty() && oppilaitostyypit.stream().noneMatch(perusopetusOppilaitostyypit::contains)) {
                throw new RuntimeException("Vuosiluokkia voi hakea vain perusopetuksen oppilaitostyypeille");
            }

            if (oppilaitostyypit.isEmpty()) {
                organisaatioIds.addAll(searchByOrganisaatioTyyppi(request, organisaatiotyyppi));
            } else {
                organisaatioIds.addAll(findKoulutustoimijatHavingOppilaitosUnderThemWithOppilaitostyyppi(oppilaitostyypit, vuosiluokat, request.getKunnat(), request.getKielet()));
            }
        }

        return makeSearchResult(organisaatioIds, kieli, kieliKoodi);
    }

    @PostMapping(value = "/hae/xls",
    consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE},
    produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE})
    @PreAuthorize("hasAnyRole('ROLE_APP_OSOITE_CRUD')")
    public ResponseEntity<ByteArrayResource> haeXls(@RequestBody MultiValueMap<String, String> request) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            HaeRequest haeRequest = objectMapper.readValue(request.getFirst("request"), HaeRequest.class);
            List<Hakutulos> tulos = getHakutulos(haeRequest);
            Integer columnCount = 12;

            String fileName = "osoitteet.xls";
            Workbook wb = new HSSFWorkbook();
            Sheet sheet = wb.createSheet("Osoitteet");

            Row header = sheet.createRow(0);
            CellStyle style = wb.createCellStyle();
            Font font = wb.createFont();
            font.setBold(true);
            style.setFont(font);
            Integer col = 0;
            createCell(header, col++, "Organisaation nimi", style);
            createCell(header, col++, "Sähköpostiosoite", style);
            createCell(header, col++, "Puhelinnumero", style);
            createCell(header, col++, "Sijaintikunta", style);
            createCell(header, col++, "Yritysmuoto", style);
            createCell(header, col++, "Opetuskieli", style);
            createCell(header, col++, "KOSKI-virheilmoituksen osoite", style);
            createCell(header, col++, "Organisaation OID", style);
            createCell(header, col++, "Oppilaitostunnus", style);
            createCell(header, col++, "Y-tunnus", style);
            createCell(header, col++, "Postiosoite", style);
            createCell(header, col++, "Käyntiosoite", style);

            for (Integer row = 1; row <= tulos.size(); row++) {
                Hakutulos h = tulos.get(row - 1);
                Row r = sheet.createRow(row);
                col = 0;
                r.createCell(col++).setCellValue(h.nimi);
                r.createCell(col++).setCellValue(h.sahkoposti.orElse(""));
                r.createCell(col++).setCellValue(h.puhelinnumero.orElse(""));
                r.createCell(col++).setCellValue(h.kunta);
                r.createCell(col++).setCellValue(h.yritysmuoto);
                r.createCell(col++).setCellValue(h.opetuskieli.orElse(""));
                r.createCell(col++).setCellValue(h.koskiVirheilmoituksenOsoite.orElse(""));
                r.createCell(col++).setCellValue(h.oid);
                r.createCell(col++).setCellValue(h.oppilaitostunnus.orElse(""));
                r.createCell(col++).setCellValue(h.ytunnus);
                r.createCell(col++).setCellValue(h.postiosoite.orElse(""));
                r.createCell(col++).setCellValue(h.kayntiosoite.orElse(""));
            }

            for (Integer i = 0; i < columnCount; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            out.close();
            wb.close();

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                    .body(new ByteArrayResource(out.toByteArray()));
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private List<Hakutulos> makeSearchResult(List<Long> organisaatioIds, String kieli, String kieliKoodi) {
        List<Organisaatio> orgs = fetchOrganisaatiosWithYhteystiedot(organisaatioIds);
        Map<Long, String> orgNimet = fetchNimet(organisaatioIds, kieli);
        Map<Long, String> koskiOsoitteet = fetchKoskiOsoitteet(organisaatioIds, kieliKoodi);
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
                            Optional.ofNullable(koskiOsoitteet.get(o.getId())),
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
        // parentOids haetaan vain jotta Hibernate ei tee näille N+1 kyselyä
        return em.createQuery("""
                        SELECT DISTINCT o FROM Organisaatio o
                        LEFT JOIN FETCH o.yhteystiedot
                        LEFT JOIN FETCH o.kielet
                        LEFT JOIN FETCH o.parentOids
                        WHERE o.id IN (:ids)""", Organisaatio.class)
                .setParameter("ids", organisaatioIds).getResultList();
    }

    private HashMap<Long, String> fetchNimet(List<Long> organisaatioIds, String kieli) {
        if (organisaatioIds.isEmpty())
            return new HashMap<>();

        String sql = """
                SELECT organisaatio.id, monikielinenteksti_values.value FROM organisaatio
                JOIN monikielinenteksti_values ON monikielinenteksti_values.id = organisaatio.nimi_mkt
                WHERE organisaatio.id IN (:ids) AND monikielinenteksti_values.key = :kieli
                """;
        try (Stream<Map.Entry<Long, String>> stream = jdbcTemplate.queryForStream(
                sql,
                Map.of("ids", organisaatioIds, "kieli", kieli),
                (rs, rowNum) -> Map.entry(rs.getLong("id"), rs.getString("value"))
        )) {
            return stream.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, HashMap::new));
        }
    }

    private HashMap<Long, String> fetchKoskiOsoitteet(List<Long> organisaatioIds, String kieliKoodi) {
        if (organisaatioIds.isEmpty())
            return new HashMap<>();

        String sql = """
                SELECT ya.organisaatio_id, ya.arvotext
                FROM yhteystietojentyyppi yt
                JOIN yhteystietoelementti ye ON yt.id = ye.yhteystietojentyyppi_id
                JOIN yhteystietoarvo ya ON ye.id = ya.kentta_id
                WHERE ya.organisaatio_id IN (:ids)
                AND yt.oid = '1.2.246.562.5.79385887983'
                AND ye.tyyppi = 'Email'
                AND ya.kieli = :kieliKoodi
                """;
        try (Stream<Map.Entry<Long, String>> stream = jdbcTemplate.queryForStream(
                sql,
                Map.of("ids", organisaatioIds, "kieliKoodi", kieliKoodi),
                (rs, rowNum) -> Map.entry(rs.getLong("organisaatio_id"), rs.getString("arvotext"))
        )) {
            return stream.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, HashMap::new));
        }
    }

    private List<Long> searchByOrganisaatioTyyppi(HaeRequest req, String organisaatiotyyppi) {
        var sql = new ArrayList<String>();
        var params = new HashMap<String, Object>(Map.of(
                "organisaatiotyyppi", organisaatiotyyppi
        ));

        sql.add("""
                SELECT DISTINCT o.id FROM organisaatio o
                JOIN organisaatio_tyypit ON (organisaatio_tyypit.organisaatio_id = o.id AND tyypit = :organisaatiotyyppi)
                """);
        if (!req.getJarjestamisluvat().isEmpty()) {
            sql.add("""
                    JOIN organisaatio_koulutuslupa ON (organisaatio_koulutuslupa.organisaatio_id = o.id)
                    JOIN koodisto_koulutus ON (organisaatio_koulutuslupa.koulutuskoodiarvo = koodisto_koulutus.koodiarvo)
                    """);
        }
        if (!req.getKielet().isEmpty()) {
            sql.add("JOIN organisaatio_kielet ON (organisaatio_kielet.organisaatio_id = o.id)");
        }

        sql.add("WHERE organisaatio_is_active(o)");
        if (!req.getJarjestamisluvat().isEmpty()) {
            sql.add("AND koodisto_koulutus.koodiuri IN (:jarestamisluvat)");
            params.put("jarestamisluvat", req.getJarjestamisluvat());
        } else if (req.isAnyJarjestamislupa()) {
            sql.add("AND EXISTS (SELECT 1 FROM organisaatio_koulutuslupa WHERE organisaatio_koulutuslupa.organisaatio_id = o.id)");
        }

        if (!req.getKunnat().isEmpty()) {
            sql.add("AND o.kotipaikka IN (:kunnat)");
            params.put("kunnat", req.getKunnat());
        }

        if (!req.getKielet().isEmpty()) {
            sql.add("AND split_part(organisaatio_kielet.kielet, '#', 1) IN (:kielet)");
            params.put("kielet", req.getKielet());
        }

        return jdbcTemplate.query(String.join("\n", sql), params, (rs, rowNum) -> rs.getLong("id"));
    }

    private List<Long> findKoulutustoimijatHavingOppilaitosUnderThemWithOppilaitostyyppi(List<String> oppilaitostyypit, List<String> vuosiluokat, List<String> kunnat, List<String> kielet) {
        Map<String, Object> params = new HashMap<>(Map.of(
                "koulutustoimijaKoodi", OrganisaatioTyyppi.KOULUTUSTOIMIJA.koodiValue(),
                "oppilaitosKoodi", OrganisaatioTyyppi.OPPILAITOS.koodiValue(),
                "oppilaitostyypit", oppilaitostyypit
        ));
        if (!vuosiluokat.isEmpty()) {
            params.put("vuosiluokat", vuosiluokat);
        }

        String sql = """
                SELECT DISTINCT parent.id
                FROM organisaatio parent
                JOIN organisaatio_tyypit parent_tyypit ON (parent_tyypit.organisaatio_id = parent.id AND parent_tyypit.tyypit = :koulutustoimijaKoodi)
                JOIN organisaatiosuhde suhde ON (suhde.parent_id = parent.id)
                JOIN organisaatio child ON (child.id = suhde.child_id)
                JOIN organisaatio_tyypit child_tyypit ON (child_tyypit.organisaatio_id = child.id AND child_tyypit.tyypit = :oppilaitosKoodi)
                """;
        if (!vuosiluokat.isEmpty()) {
            sql += "JOIN organisaatio_vuosiluokat ON (child.id = organisaatio_vuosiluokat.organisaatio_id)";
        }
        if (!kielet.isEmpty()) {
            sql += " JOIN organisaatio_kielet parent_kielet ON (parent_kielet.organisaatio_id = parent.id)";
        }

        sql += """
                WHERE suhde.alkupvm <= current_date AND (suhde.loppupvm IS NULL OR current_date < suhde.loppupvm)
                AND organisaatio_is_active(parent)
                AND organisaatio_is_active(child)
                AND child.oppilaitostyyppi IN (:oppilaitostyypit)
                """;
        if (!vuosiluokat.isEmpty()) {
            sql += " AND organisaatio_vuosiluokat.vuosiluokat IN (:vuosiluokat)";
        }

        if (!kielet.isEmpty()) {
            sql += " AND split_part(parent_kielet.kielet, '#', 1) IN (:kielet)";
            params.put("kielet", kielet);
        }

        if (!kunnat.isEmpty()) {
            params.put("kunnat", kunnat);
            sql += " AND parent.kotipaikka IN (:kunnat)";
        }

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

    private final List<String> perusopetusOppilaitostyypit = List.of("oppilaitostyyppi_12#1", "oppilaitostyyppi_11#1", "oppilaitostyyppi_19#1");

    @GetMapping(value = "/parametrit")
    @PreAuthorize("hasAnyRole('ROLE_APP_OSOITE_CRUD')")
    public Parametrit getParametrit() {
        List<OppilaitosRyhma> ryhmat = List.of(
                new OppilaitosRyhma("Perusopetus", perusopetusOppilaitostyypit),
                new OppilaitosRyhma("Lukiokoulutus", List.of("oppilaitostyyppi_15#1", "oppilaitostyyppi_19#1")),
                new OppilaitosRyhma("Ammatillinen koulutus", List.of("oppilaitostyyppi_63#1", "oppilaitostyyppi_29#1", "oppilaitostyyppi_61#1", "oppilaitostyyppi_22#1", "oppilaitostyyppi_21#1", "oppilaitostyyppi_24#1", "oppilaitostyyppi_62#1", "oppilaitostyyppi_23#1")),
                new OppilaitosRyhma("Korkeakoulutus", List.of("oppilaitostyyppi_42#1", "oppilaitostyyppi_43#1", "oppilaitostyyppi_41#1")),
                new OppilaitosRyhma("Vapaan sivistystyön koulutus", List.of("oppilaitostyyppi_65#1", "oppilaitostyyppi_62#1", "oppilaitostyyppi_66#1", "oppilaitostyyppi_63#1", "oppilaitostyyppi_64#1")),
                new OppilaitosRyhma("Taiteen perusopetus", List.of("oppilaitostyyppi_01#1", "oppilaitostyyppi_61#1"))
        );
        RowMapper<KoodistoKoodi> koodiMapper = (rs, rowNum) -> new KoodistoKoodi(rs.getString("koodi"), rs.getString("nimi"));
        var oppilaitostyyppiKoodis = jdbcTemplate.query("SELECT concat(koodiuri, '#', versio) AS koodi, nimi_fi AS nimi FROM koodisto_oppilaitostyyppi", koodiMapper);
        var vuosiluokat = jdbcTemplate.query("SELECT concat(koodiuri, '#', versio) AS koodi, nimi_fi AS nimi FROM koodisto_vuosiluokat ORDER BY lpad(koodiarvo, 3, '0')", koodiMapper);
        var kunnat = jdbcTemplate.query("SELECT koodiuri AS koodi, nimi_fi AS nimi FROM koodisto_kunta ORDER BY nimi_fi COLLATE \"fi-FI-x-icu\", koodiarvo", koodiMapper);
        List<MaakuntaKoodi> maakunnat = jdbcTemplate.query("""
                        SELECT maakunta.koodiuri AS koodi, maakunta.nimi_fi AS nimi, array_agg(kunta.koodiuri ORDER BY kunta.koodiarvo) AS kunnat
                        FROM koodisto_maakunta maakunta
                        JOIN maakuntakuntarelation ON maakunta.koodiuri = maakuntakuntarelation.maakuntauri
                        JOIN koodisto_kunta kunta ON kunta.koodiuri = maakuntakuntarelation.kuntauri
                        GROUP BY maakunta.koodiuri, maakunta.nimi_fi
                        ORDER BY maakunta.nimi_fi COLLATE "fi-FI-x-icu", maakunta.koodiuri
                        """,
                (rs, rowNum) -> {
                    List<String> sisaltyvatKunnat = new ArrayList<>(List.of((String[]) (rs.getArray("kunnat").getArray())));
                    return new MaakuntaKoodi(rs.getString("koodi"), rs.getString("nimi"), sisaltyvatKunnat);
                }
        );
        List<KoodistoKoodi> jarjestamisluvat = jdbcTemplate.query("""
                WITH luvat AS (
                    SELECT DISTINCT koodiuri FROM koodisto_koulutus
                    JOIN organisaatio_koulutuslupa ON koulutuskoodiarvo = koodiarvo
                    JOIN organisaatio o ON organisaatio_koulutuslupa.organisaatio_id = o.id
                    WHERE organisaatio_is_active(o)
                )
                SELECT koodiuri AS koodi, koodiarvo, nimi_fi AS nimi
                FROM koodisto_koulutus JOIN luvat USING (koodiuri)
                ORDER BY nimi_fi COLLATE "fi-FI-x-icu", koodiarvo
                """, koodiMapper);
        var opetuskielet = jdbcTemplate.query("SELECT koodiuri AS koodi, nimi_fi AS nimi FROM koodisto_oppilaitoksenopetuskieli ORDER BY lpad(koodiarvo, 3, '0')", koodiMapper);

        return new Parametrit(
                new OppilaitostyyppiParametrit(oppilaitostyyppiKoodis, ryhmat),
                vuosiluokat,
                maakunnat,
                kunnat,
                jarjestamisluvat,
                opetuskielet
        );
    }
}

@Data
class Parametrit {
    private final OppilaitostyyppiParametrit oppilaitostyypit;
    private final List<KoodistoKoodi> vuosiluokat;
    private final List<MaakuntaKoodi> maakunnat;
    private final List<KoodistoKoodi> kunnat;
    private final List<KoodistoKoodi> jarjestamisluvat;
    private final List<KoodistoKoodi> kielet;
}

@Data
class OppilaitostyyppiParametrit {
    private final List<KoodistoKoodi> koodit;
    private final List<OppilaitosRyhma> ryhmat;
}

@Data
class OppilaitosRyhma {
    private final String nimi;
    private final List<String> koodit;
}

@Data
class KoodistoKoodi {
    private final String koodiUri;
    private final String nimi;
}

@Data
class MaakuntaKoodi {
    private final String koodiUri;
    private final String nimi;
    private final List<String> kunnat;
}

@Data
class HaeRequest {
    private List<String> organisaatiotyypit;
    private List<String> oppilaitostyypit;
    private List<String> vuosiluokat;
    private List<String> kunnat;
    private boolean anyJarjestamislupa;
    private List<String> jarjestamisluvat;
    private List<String> kielet;
}