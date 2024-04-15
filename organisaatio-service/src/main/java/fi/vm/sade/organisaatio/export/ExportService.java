package fi.vm.sade.organisaatio.export;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ExportService {
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void createSchema() {
        jdbcTemplate.execute("DROP SCHEMA IF EXISTS exportnew CASCADE");
        jdbcTemplate.execute("CREATE SCHEMA exportnew");
        jdbcTemplate.execute("""
                CREATE TABLE exportnew.organisaatio AS
                SELECT
                     o.oid as organisaatio_oid,
                     (SELECT v.value
                        FROM monikielinenteksti_values v
                        WHERE v.id = o.nimi_mkt
                        AND v.key = 'fi') as nimi_fi,
                     (SELECT v.value
                        FROM monikielinenteksti_values v
                        WHERE v.id = o.nimi_mkt
                        AND v.key = 'sv') as nimi_sv,
                     (SELECT string_agg(tyypit, ',')
                        FROM organisaatio_tyypit
                        WHERE organisaatio_id = o.id) as organisaatiotyypit,
                     o.oppilaitostyyppi,
                     o.oppilaitoskoodi as oppilaitosnumero,
                     o.kotipaikka,
                     o.ytunnus as y_tunnus,
                     o.tuontipvm,
                     o.paivityspvm
                FROM organisaatio AS o
                WHERE organisaatio_is_active(o)
                """);
        jdbcTemplate.execute("""
                CREATE TABLE exportnew.organisaatiosuhde AS
                SELECT suhdetyyppi,
                       (SELECT o.oid
                          FROM organisaatio o
                          WHERE o.id = parent_id) AS parent_oid,
                       (SELECT o.oid
                          FROM organisaatio o
                          WHERE id = child_id) AS child_oid
                       FROM organisaatiosuhde;
                """);
        jdbcTemplate.execute("DROP SCHEMA IF EXISTS export CASCADE");
        jdbcTemplate.execute("ALTER SCHEMA exportnew RENAME TO export");
    }
}
