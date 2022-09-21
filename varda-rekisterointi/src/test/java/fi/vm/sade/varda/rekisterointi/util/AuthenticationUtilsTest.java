package fi.vm.sade.varda.rekisterointi.util;

import org.junit.Test;

import static fi.vm.sade.varda.rekisterointi.util.Constants.VIRKAILIJA_ROLE;

import static org.junit.Assert.assertEquals;

public class AuthenticationUtilsTest {
  
  @Test
  public void mapToRoleMapsBasicAuthority() {
    String role = AuthenticationUtils.mapToRole("ROLE_" + VIRKAILIJA_ROLE).orElse("");
    assertEquals("OPH", role);
  }

  @Test
  public void mapToRoleMapsAuthorityWithOid() {
    String role = AuthenticationUtils.mapToRole("ROLE_" + VIRKAILIJA_ROLE + "_1.2.246.562.10.00000000001").orElse("");
    assertEquals("OPH", role);
  }
}
