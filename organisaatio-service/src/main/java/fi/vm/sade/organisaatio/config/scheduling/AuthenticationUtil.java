package fi.vm.sade.organisaatio.config.scheduling;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public final class AuthenticationUtil {
    @Value("${root.organisaatio.oid}")
    private String rootOid;
    @Value("${organisaatio.serviceuser.oid}")
    private String serviceUser;

    public void configureAuthentication(String role) {
        Collection<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList(role + rootOid);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                serviceUser,
                null,
                authorities
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public String getCurrentUserOid() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
