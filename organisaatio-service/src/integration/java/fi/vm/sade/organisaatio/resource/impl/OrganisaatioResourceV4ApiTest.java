package fi.vm.sade.organisaatio.resource.impl;

import fi.vm.sade.organisaatio.dto.v4.OrganisaatioRDTOV4;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class OrganisaatioResourceV4ApiTest {

    private static final String EXISTING_OID = "1.2.8000.1";
    private static final String NONEXISTENT_OID = "1.1.2.345.67890";

    @LocalServerPort
    private int port;

    @Test
    @Sql(
            scripts = "classpath:data/basic_organisaatio_data.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:data/truncate_tables.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    public void getOrganisaatioByOIDPalauttaaOrganisaation() {
        OrganisaatioRDTOV4 organisaatio = RestAssured
                .given()
                    .port(port)
                    .pathParam("oid", EXISTING_OID)
                .when()
                    .get("/organisaatio/v4/{oid}")
                .then()
                    .statusCode(HttpStatus.OK.value())
                .and()
                .extract().as(OrganisaatioRDTOV4.class);
        assertEquals(EXISTING_OID, organisaatio.getOid());
    }

    @Test
    @Sql(
            scripts = "classpath:data/basic_organisaatio_data.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:data/truncate_tables.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    public void getOrganisaatioByOIDPalauttaaNotFound() {
        RestAssured
                .given()
                    .port(port)
                    .pathParam("oid", NONEXISTENT_OID)
                .when()
                    .get("/organisaatio/v4/{oid}")
                .then()
                    .statusCode(HttpStatus.NOT_FOUND.value());
    }
}
