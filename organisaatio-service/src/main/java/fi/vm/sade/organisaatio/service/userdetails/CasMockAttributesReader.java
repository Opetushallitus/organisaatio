package fi.vm.sade.organisaatio.service.userdetails;

import com.google.gson.stream.JsonReader;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CasMockAttributesReader {

    public Map<String, UserDetails> readUserAttributes(File attributesFile) {
        Map<String, UserDetails> usernamesToDetails = new HashMap<>();
        try (JsonReader attributesReader = new JsonReader(new BufferedReader(new FileReader(attributesFile)))) {
            attributesReader.beginObject();
            while (attributesReader.hasNext()) {
                String username = attributesReader.nextName();
                attributesReader.beginObject();
                while (attributesReader.hasNext()) {
                    if (attributesReader.nextName().equals("attributes")) {
                        UserDetails userDetails = readUserDetails(attributesReader);
                        usernamesToDetails.put(username, userDetails);
                    } else {
                        attributesReader.skipValue();
                    }
                }
                attributesReader.endObject();
            }
            attributesReader.endObject();
        } catch (IOException e) {
            throw new IllegalStateException("Reading attributes file failed", e);
        }
        return usernamesToDetails;
    }

    private static UserDetails readUserDetails(JsonReader reader) throws IOException {
        String oid = null;
        List<String> authorities = new ArrayList<>();
        reader.beginObject();
        while (reader.hasNext()) {
            String fieldName = reader.nextName();
            if (fieldName.equals("oid")) {
                oid = reader.nextString();
            } else if (fieldName.equals("authorities")) {
                reader.beginArray();
                while (reader.hasNext()) {
                    authorities.add(reader.nextString());
                }
                reader.endArray();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        assert(oid != null);
        return new CasMockUserDetails(oid, authorities);
    }

    private static class CasMockUserDetails implements UserDetails {

        private final String oid;
        private final Collection<? extends GrantedAuthority> authorities;

        private CasMockUserDetails(String oid, List<String> authorities) {
            this.oid = oid;
            this.authorities = authorities.stream()
                    .map(CasMockAuthority::new).collect(Collectors.toSet());
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return authorities;
        }

        @Override
        public String getPassword() {
            return null;
        }

        @Override
        public String getUsername() {
            return oid;
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }

    private static class CasMockAuthority implements GrantedAuthority {

        private final String authority;

        private CasMockAuthority(String authority) {
            this.authority = authority;
        }

        @Override
        public String getAuthority() {
            return authority;
        }
    }

}
