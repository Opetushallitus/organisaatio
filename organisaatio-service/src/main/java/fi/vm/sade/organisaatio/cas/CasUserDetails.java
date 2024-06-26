package fi.vm.sade.organisaatio.cas;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class CasUserDetails implements UserDetails {
    private static final String SUOMI_FI_IDP_ENTITY_ID = "vetuma";
    private final String oidHenkilo;
    private final String idpEntityId;
    private final String kayttajaTyyppi;
    private final List<SimpleGrantedAuthority> authorities;
    @Override public String getUsername() { return oidHenkilo; }
    @Override public Collection<SimpleGrantedAuthority> getAuthorities() { return authorities; }
    public boolean isStrongAuth() { return SUOMI_FI_IDP_ENTITY_ID.equals(idpEntityId); }
    public boolean isPalvelukayttaja() { return "PALVELU".equals(kayttajaTyyppi); }
    @Override public String getPassword() { return null; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}