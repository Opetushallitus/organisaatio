package fi.vm.sade.varda.rekisterointi.controller.virkailija;

import org.springframework.security.test.context.support.WithMockUser;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithMockUser(value = WithMockVirkailijaUser.MOCK_VIRKAILIJA_OID, roles = "APP_ORGANISAATIOIDEN_REKISTEROITYMINEN_OPH")
public @interface WithMockVirkailijaUser {
    String MOCK_VIRKAILIJA_OID = "1.234.56789";
}
