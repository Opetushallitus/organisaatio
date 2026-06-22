package fi.vm.sade.rekisterointi.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static fi.vm.sade.rekisterointi.util.Constants.SESSION_ATTRIBUTE_NAME_BUSINESS_ID;
import static fi.vm.sade.rekisterointi.util.Constants.SESSION_ATTRIBUTE_NAME_ORGANISATION_NAME;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = WebSecurityConfigurationTest.TestApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WebSecurityConfigurationTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void hakijaRequestsRedirectUnauthenticatedUsersToCasLogin() throws Exception {
    mockMvc.perform(get("/hakija/protected"))
        .andExpect(status().isFound())
        .andExpect(header().string("Location", startsWith("https://untuvaopintopolku.fi/cas-oppija/login")));
  }

  @Test
  @WithMockUser(roles = "APP_REKISTEROINTI_HAKIJA")
  void authenticatedHakijaRequestsWithoutValtuudetSessionAttributesRedirectToValtuudet() throws Exception {
    mockMvc.perform(get("/hakija/protected"))
        .andExpect(status().isTemporaryRedirect())
        .andExpect(header().string("Location", "/hakija/valtuudet/redirect"));
  }

  @Test
  @WithMockUser(roles = "APP_REKISTEROINTI_HAKIJA")
  void hakijaRequestsWithRoleAndValtuudetSessionAttributesAreAllowed() throws Exception {
    mockMvc.perform(get("/hakija/protected")
        .sessionAttr(SESSION_ATTRIBUTE_NAME_BUSINESS_ID, "1234567-8")
        .sessionAttr(SESSION_ATTRIBUTE_NAME_ORGANISATION_NAME, "Testiorganisaatio"))
        .andExpect(status().isOk())
        .andExpect(content().string("hakija"));
  }

  @Test
  void nonHakijaRequestsBypassHakijaSecurityChain() throws Exception {
    mockMvc.perform(get("/api/public"))
        .andExpect(status().isOk())
        .andExpect(content().string("public"));
  }

  @SpringBootConfiguration
  @EnableAutoConfiguration
  @Import(WebSecurityConfiguration.class)
  static class TestApplication {
    @Bean
    TestController testController() {
      return new TestController();
    }
  }

  @RestController
  static class TestController {
    @GetMapping("/hakija/protected")
    String hakija() {
      return "hakija";
    }

    @GetMapping("/api/public")
    String publicApi() {
      return "public";
    }
  }
}
