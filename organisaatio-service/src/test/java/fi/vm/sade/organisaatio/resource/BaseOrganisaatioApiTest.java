package fi.vm.sade.organisaatio.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
@Sql("/data/truncate_tables.sql")
@Sql("/data/basic_organisaatio_data.sql")
abstract public class BaseOrganisaatioApiTest {
    @Autowired
    protected MockMvc mvc;
    @Autowired
    protected ObjectMapper objectMapper;


    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @WithMockUser(value = "1.2.3.4.5", roles = {"APP_ORGANISAATIOHALLINTA", "APP_ORGANISAATIOHALLINTA_READ", "APP_ORGANISAATIOHALLINTA_READ_1.2.246.562.10.90008375488"})
    @interface LimitedUser {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @WithAnonymousUser
    @interface AnonymousUser {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @WithMockUser(roles = {"APP_ORGANISAATIOHALLINTA", "APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.24.00000000001"})
    @interface OPHUser {
    }
}
