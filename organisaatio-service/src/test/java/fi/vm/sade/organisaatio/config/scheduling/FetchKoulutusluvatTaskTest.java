package fi.vm.sade.organisaatio.config.scheduling;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Sql({"/data/truncate_tables.sql", "/data/basic_organisaatio_data.sql"})
class FetchKoulutusluvatTaskTest {
    @Autowired
    private FetchKoulutusluvatTask fetchKoulutusluvatTask;
    @Autowired
    private FetchKoodistotTask fetchKoodistotTask;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void beforeEach() {
        fetchKoodistotTask.execute();
    }

    @Test
    void testDataImportFromOiva() {
        fetchKoulutusluvatTask.execute();
        assertKoulutuslupaCount("1492449-0", 3);
        assertKoulutuslupaCount("2255802-1", 0); // koulutusluvalla päättymispäivä menneisyydessä
    }

    private void assertKoulutuslupaCount(String ytunnus, int expectedCount) {
        var sql = "SELECT count(*) FROM organisaatio" +
                " JOIN organisaatio_koulutuslupa ON (id = organisaatio_id)" +
                " WHERE ytunnus = ?";
        var count = jdbcTemplate.queryForObject(sql, Long.class, ytunnus);
        assertThat(count).isEqualTo(expectedCount);
    }
}