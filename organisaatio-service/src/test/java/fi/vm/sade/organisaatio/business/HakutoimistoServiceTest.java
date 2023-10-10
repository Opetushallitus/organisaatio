package fi.vm.sade.organisaatio.business;

import fi.vm.sade.organisaatio.business.exception.HakutoimistoNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import static org.junit.jupiter.api.Assertions.assertThrows;

@Sql({"/data/truncate_tables.sql"})
@Sql({"/data/basic_organisaatio_data.sql"})
@SpringBootTest
class HakutoimistoServiceTest {

    @Autowired
    HakutoimistoService hakutoimistoService;

    @Test
    void testFetchingHakutoimisto() {
        assertThrows(HakutoimistoNotFoundException.class, () ->
                hakutoimistoService.hakutoimisto("1.2.2004.4"));
    }
}