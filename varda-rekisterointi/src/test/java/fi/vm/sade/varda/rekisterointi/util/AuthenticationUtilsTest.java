package fi.vm.sade.varda.rekisterointi.util;

import org.junit.Test;

import static fi.vm.sade.varda.rekisterointi.util.Constants.VIRKAILIJA_ROLE;

import static org.junit.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.util.List;
import java.util.Set;

public class AuthenticationUtilsTest {
  
  @Test
  public void mapToRoleMapsBasicAuthority() {
    String role = AuthenticationUtils.mapToRole("ROLE_" + VIRKAILIJA_ROLE);
    assertEquals("OPH", role);
  }

  @Test
  public void mapToRoleMapsAuthorityWithOid() {
    String role = AuthenticationUtils.mapToRole("ROLE_" + VIRKAILIJA_ROLE + "_1.2.246.562.10.00000000001");
    assertEquals("OPH", role);
  }

  @Test
  public void mapRolesToRegistrationTypesMapsAVardaRole() {
    List<String> ophRole = List.of("VARDA");
    Set<String> registrationTypes = AuthenticationUtils.mapRolesToRegistrationTypes(ophRole);
    assertThat(registrationTypes, hasItems("varda"));
  }

  @Test
  public void mapRolesToRegistrationTypesMapsAnOPHRole() {
    List<String> ophRole = List.of("OPH");
    Set<String> registrationTypes = AuthenticationUtils.mapRolesToRegistrationTypes(ophRole);
    assertThat(registrationTypes, hasItems("jotpa", "varda"));
  }

  @Test
  public void mapRolesToRegistrationTypesMapsOPHAndOtherRoles() {
    List<String> ophRole = List.of("VARDA", "OPH");
    Set<String> registrationTypes = AuthenticationUtils.mapRolesToRegistrationTypes(ophRole);
    assertThat(registrationTypes, hasItems("jotpa", "varda"));
  }

  @Test
  public void mapRolesToRegistrationTypesMapsSeveralDistinctRoles() {
    List<String> ophRole = List.of("VARDA", "JOTPA");
    Set<String> registrationTypes = AuthenticationUtils.mapRolesToRegistrationTypes(ophRole);
    assertThat(registrationTypes, hasItems("jotpa", "varda"));
  }
}
