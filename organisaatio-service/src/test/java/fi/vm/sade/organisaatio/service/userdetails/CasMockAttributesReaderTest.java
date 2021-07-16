package fi.vm.sade.organisaatio.service.userdetails;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CasMockAttributesReaderTest {

    private final CasMockAttributesReader reader = new CasMockAttributesReader();
    private final File attributesFile;

    public CasMockAttributesReaderTest() {
        try {
            attributesFile = new File(this.getClass().getResource("/json/attributes.json").toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    public void readsSeveralUsers() {
        Map<String, UserDetails> userDetails = reader.readUserAttributes(attributesFile);
        assertEquals(2, userDetails.size());
        assertTrue(userDetails.containsKey("teemup"));
        assertEquals(userDetails.get("teemup").getUsername(), "1");
        assertTrue(userDetails.containsKey("lukash"));
        assertEquals(userDetails.get("lukash").getUsername(), "2");
    }

    @Test
    public void readsAuthorities() {
        Map<String, UserDetails> userDetails = reader.readUserAttributes(attributesFile);
        assertEquals(2, userDetails.size());
        assertTrue(userDetails.get("lukash").getAuthorities().stream().anyMatch(
                grantedAuthority -> grantedAuthority.getAuthority().equals("MALLASOIKEUS")
        ));
    }
}
