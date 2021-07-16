package fi.vm.sade.organisaatio.service.userdetails;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "organisaatio.dev-userdetails.enabled", havingValue = "true")
public class DevUserDetailsService implements UserDetailsService {

    private final Map<String, UserDetails> usernameToUserDetails;

    public DevUserDetailsService(
            @Value("${organisaatio.dev-userdetails.attributes-path}") Resource attributesResource)
            throws IOException {
        File attributesFile = attributesResource.getFile();
        CasMockAttributesReader reader = new CasMockAttributesReader();
        this.usernameToUserDetails = reader.readUserAttributes(attributesFile);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDetails userDetails = usernameToUserDetails.get(username);
        if (userDetails == null) {
            throw new UsernameNotFoundException("Username not found: " + username);
        }
        return userDetails;
    }

}
