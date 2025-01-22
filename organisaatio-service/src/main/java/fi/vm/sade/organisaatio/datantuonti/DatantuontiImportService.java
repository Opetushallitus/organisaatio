package fi.vm.sade.organisaatio.datantuonti;

import static java.util.stream.Collectors.toSet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.vm.sade.organisaatio.business.OrganisaatioBusinessService;
import fi.vm.sade.organisaatio.dto.v4.OrganisaatioRDTOV4;
import fi.vm.sade.organisaatio.export.OpintopolkuAwsClients;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioNimiRDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.DownloadFileRequest;

import static fi.vm.sade.organisaatio.datantuonti.DatantuontiExportService.MANIFEST_OBJECT_KEY;

@Service
@Slf4j
@RequiredArgsConstructor
public class DatantuontiImportService {
    private final S3AsyncClient opintopolkuS3Client;
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;
    private final OrganisaatioBusinessService organisaatioBusinessService;
    private final ObjectMapper objectMapper;

    static final String CREATE_DATANTUONTI_ORGANISAATIO = """
            CREATE TABLE IF NOT EXISTS datantuonti_organisaatio_temp(
                oid text,
                parent_oid text,
                oppilaitostyyppi text,
                organisaatiotyypit text,
                ytunnus text,
                piilotettu boolean,
                nimi_fi text,
                nimi_sv text,
                nimi_en text,
                alkupvm timestamp,
                lakkautuspvm timestamp,
                yritysmuoto text,
                kotipaikka text,
                maa text,
                kielet text
            )""";

    static final String CREATE_DATANTUONTI_OSOITE = """
            CREATE TABLE IF NOT EXISTS datantuonti_osoite_temp(
                oid text,
                osoitetyyppi text,
                osoite text,
                postinumero text,
                postitoimipaikka text,
                kieli text
            )""";

    @Value("${organisaatio.tasks.datantuonti.import.bucket.name}")
    private String bucketName;

    @Transactional
    public void importTempTableFromS3() throws IOException {
        DatantuontiManifest manifest = getManifest();

        log.info("Importing tables from S3");
        jdbcTemplate.execute("DROP TABLE IF EXISTS datantuonti_organisaatio_temp");
        jdbcTemplate.execute(CREATE_DATANTUONTI_ORGANISAATIO);
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS datantuonti_organisaatio_temp_oid_idx ON datantuonti_organisaatio_temp(oid)");

        String organisaatioSql = "select * from aws_s3.table_import_from_s3('datantuonti_organisaatio_temp', '',  '(FORMAT CSV,HEADER true)', aws_commons.create_s3_uri(?, ?, ?))";
        String organisaatioTxt = jdbcTemplate.queryForObject(organisaatioSql, String.class, bucketName, manifest.organisaatio(), OpintopolkuAwsClients.REGION.id());
        log.info("Importing datantuontiorganisaatiot from S3 returned {}", organisaatioTxt);

        jdbcTemplate.execute("DROP TABLE IF EXISTS datantuonti_osoite_temp");
        jdbcTemplate.execute(CREATE_DATANTUONTI_OSOITE);
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS datantuonti_osoite_temp_oid_idx ON datantuonti_osoite_temp(oid)");
        String osoiteSql = "select * from aws_s3.table_import_from_s3('datantuonti_osoite_temp', '',  '(FORMAT CSV,HEADER true)', aws_commons.create_s3_uri(?, ?, ?))";
        String osoiteTxt = jdbcTemplate.queryForObject(osoiteSql, String.class, bucketName, manifest.osoite(), OpintopolkuAwsClients.REGION.id());
        log.info("Importing datantuontiosoite from S3 returned {}", osoiteTxt);
    }

    private OrganisaatioRDTOV4 datantuontiToOrganisaatio(DatantuontiOrganisaatio dorg, List<DatantuontiOsoite> osoitteet) {
        OrganisaatioRDTOV4 o = new OrganisaatioRDTOV4();
        o.setOid(dorg.oid());
        o.setParentOid("1.2.246.562.10.00000000001");
        o.setOppilaitosTyyppiUri(dorg.oppilaitostyyppi());
        o.setTyypit(Set.of(dorg.organisaatiotyypit().split(",")));
        o.setYTunnus(dorg.ytunnus());
        o.setPiilotettu(dorg.piilotettu());
        o.setNimi(Map.of(
            "fi", Optional.of(dorg.nimi_fi()).orElse(""),
            "sv", Optional.of(dorg.nimi_sv()).orElse(""),
            "en", Optional.of(dorg.nimi_en()).orElse("")
        ));
        var orgNimi = new OrganisaatioNimiRDTO();
        orgNimi.setNimi(o.getNimi());
        orgNimi.setAlkuPvm(dorg.alkupvm());
        o.setNimet(List.of(orgNimi));
        o.setAlkuPvm(dorg.alkupvm());
        o.setLakkautusPvm(dorg.lakkautuspvm());
        o.setYritysmuoto(dorg.yritysmuoto());
        o.setKotipaikkaUri(dorg.kotipaikka());
        o.setMaaUri(dorg.maa());
        o.setKieletUris(Set.of(dorg.kielet().split(",")));
        o.setYhteystiedot(osoitteet.stream().map(osoite -> Map.of(
            "osoite", Optional.of(osoite.osoite()).orElse(""),
            "osoiteTyyppi", Optional.of(osoite.osoitetyyppi()).orElse(""),
            "postinumeroUri", Optional.of(osoite.postinumero()).orElse(""),
            "postitoimipaikka", Optional.of(osoite.postitoimipaikka()).orElse(""),
            "kieli", Optional.of(osoite.kieli()).orElse("")
        )).collect(toSet()));
        return o;
    }

    String newOrganisationQuery = """
            SELECT
                d.oid,
                d.parent_oid,
                d.oppilaitostyyppi,
                d.organisaatiotyypit,
                d.ytunnus,
                d.piilotettu,
                d.nimi_fi,
                d.nimi_sv,
                d.nimi_en,
                d.alkupvm,
                d.lakkautuspvm,
                d.yritysmuoto,
                d.kotipaikka,
                d.maa,
                d.kielet
            FROM datantuonti_organisaatio_temp d
            LEFT JOIN organisaatio ON d.oid = organisaatio.oid
            WHERE organisaatio.oid IS NULL
            ORDER BY d.parent_oid NULLS FIRST
    """;

    RowMapper<DatantuontiOrganisaatio> datantuontiOrganisaatioRowMapper = (rs, rn) -> new DatantuontiOrganisaatio(
        rs.getString("oid"),
        rs.getString("parent_oid"),
        rs.getString("oppilaitostyyppi"),
        rs.getString("organisaatiotyypit"),
        rs.getString("ytunnus"),
        rs.getBoolean("piilotettu"),
        rs.getString("nimi_fi"),
        rs.getString("nimi_sv"),
        rs.getString("nimi_en"),
        rs.getObject("alkupvm", Date.class),
        rs.getObject("lakkautuspvm", Date.class),
        rs.getString("yritysmuoto"),
        rs.getString("kotipaikka"),
        rs.getString("maa"),
        rs.getString("kielet")
    );

    public void createNewOrganisations() {
        try (Stream<DatantuontiOrganisaatio> newOrganisations = namedJdbcTemplate.queryForStream(
            newOrganisationQuery,
            Map.of(),
            datantuontiOrganisaatioRowMapper
        )) {
            var createdOrgs = newOrganisations
                .map(this::mapWithYhteystiedot)
                .peek(this::saveNewOrganisation)
                .toList();
            createdOrgs.stream().forEach(this::saveParentOid);
        }
    }

    private OrganisaatioWithParentOid mapWithYhteystiedot(DatantuontiOrganisaatio o) {
        log.info("Saving new organisation oid {}", o.oid());
        try (Stream<DatantuontiOsoite> osoitteet = namedJdbcTemplate.queryForStream(
            """
                SELECT osoitetyyppi, osoite, postinumero, postitoimipaikka, kieli
                FROM datantuonti_osoite_temp
                WHERE oid = :oid
            """,
            Map.of("oid", o.oid()),
            (rs, rn) -> new DatantuontiOsoite(rs.getString("osoitetyyppi"), rs.getString("osoite"), rs.getString("postinumero"), rs.getString("postitoimipaikka"), rs.getString("kieli"))
        )) {
            return new OrganisaatioWithParentOid(datantuontiToOrganisaatio(o, osoitteet.toList()), o.parent_oid());
        } catch (Exception e) {
            log.error("Failed to fetch yhteystiedot for organisaatio oid " + o.oid(), e);
            throw e;
        }
    }

    private void saveNewOrganisation(OrganisaatioWithParentOid o) {
        log.info("Saving new organisation oid {}", o.organisaatio().getOid());
        try {
            organisaatioBusinessService.saveDatantuontiOrganisaatio(o.organisaatio());
        } catch (Exception e) {
            log.error("Failed to create new organisation with oid " + o.organisaatio().getOid(), e);
            throw e;
        }
    }

    private void saveParentOid(OrganisaatioWithParentOid o) {
        if (o.parentOid() != null) {
            try {
                log.info("Updating parent oids for organisation oid {}", o.organisaatio().getOid());
                o.organisaatio().setParentOid(o.parentOid());
                organisaatioBusinessService.updateDatantuontiOrganisaatio(o.organisaatio());
            } catch (Exception e) {
                log.error("Failed to update parent oids for organisation oid " + o.organisaatio().getOid(), e);
                throw e;
            }
        }
    }

    private DatantuontiManifest getManifest() throws IOException {
        @SuppressWarnings("java:S5443")
        var temporaryFile = File.createTempFile("export", ".csv");
        try {
            log.info("Downloading manifest from S3: {}/{}", bucketName, MANIFEST_OBJECT_KEY);
            try (var downloader = S3TransferManager.builder().s3Client(opintopolkuS3Client).build()) {
                var fileDownload = downloader.downloadFile(DownloadFileRequest.builder()
                        .getObjectRequest(b -> b.bucket(bucketName).key(MANIFEST_OBJECT_KEY))
                        .destination(temporaryFile)
                        .build());
                fileDownload.completionFuture().join();
                return objectMapper.readValue(temporaryFile, DatantuontiManifest.class);
            }
        } finally {
            Files.deleteIfExists(temporaryFile.toPath());
        }
    }
}

record OrganisaatioWithParentOid(
    OrganisaatioRDTOV4 organisaatio,
    String parentOid
) {};

record DatantuontiOrganisaatio(
    String oid,
    String parent_oid,
    String oppilaitostyyppi,
    String organisaatiotyypit,
    String ytunnus,
    Boolean piilotettu,
    String nimi_fi,
    String nimi_sv,
    String nimi_en,
    Date alkupvm,
    Date lakkautuspvm,
    String yritysmuoto,
    String kotipaikka,
    String maa,
    String kielet
) {};

record DatantuontiOsoite(
    String osoitetyyppi,
    String osoite,
    String postinumero,
    String postitoimipaikka,
    String kieli
) {};