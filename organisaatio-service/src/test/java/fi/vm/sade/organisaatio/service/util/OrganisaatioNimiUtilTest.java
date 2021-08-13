package fi.vm.sade.organisaatio.service.util;

import fi.vm.sade.organisaatio.model.MonikielinenTeksti;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Collections;
import java.util.Map;

public class OrganisaatioNimiUtilTest {

    @Test
    public void createNimihakuHandlesNullArguments() {
        assertNull(OrganisaatioNimiUtil.createNimihaku(null));
    }

    @Test
    public void createNimihakuHandlesMissingValues() {
        assertNull(OrganisaatioNimiUtil.createNimihaku(new MonikielinenTeksti()));
    }

    @Test
    public void createNimihakuHandlesEmptyValues() {
        MonikielinenTeksti nimi = new MonikielinenTeksti();
        nimi.setValues(Collections.emptyMap());

        assertNull(OrganisaatioNimiUtil.createNimihaku(nimi));
    }

    @Test
    public void createNimihakuHandlesValues() {
        MonikielinenTeksti nimi = new MonikielinenTeksti();
        nimi.setValues(Map.of("fi", "nimi", "sv", "namn", "en", "name"));

        String nimihaku = OrganisaatioNimiUtil.createNimihaku(nimi);

        assertNotNull(nimihaku);
        assertTrue(nimihaku.contains("nimi"));
        assertTrue(nimihaku.contains("namn"));
        assertTrue(nimihaku.contains("name"));
        assertTrue(nimihaku.matches("\\w+,\\w+,\\w+"));
    }
}
