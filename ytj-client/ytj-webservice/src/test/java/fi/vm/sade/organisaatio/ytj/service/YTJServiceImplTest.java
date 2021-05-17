package fi.vm.sade.organisaatio.ytj.service;

import fi.vm.sade.organisaatio.ytj.api.YTJDTO;
import fi.vm.sade.organisaatio.ytj.api.YTJKieli;
import fi.vm.sade.organisaatio.ytj.api.exception.YtjConnectionException;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

public class YTJServiceImplTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(YTJServiceImplTest.class);

    @Test
    public void testHashHex() {
        String str = "Str to be hashed";
        YTJServiceImpl instance = new YTJServiceImpl();
        String hex = instance.createHashHex(str);
        assertNotNull(hex);
    }
    /**
     * Test of findByYNimi method, of class YTJServiceImpl.
     * 
     * Both of these tests must fail because no client id or secret key has
     * been given
     */
    @Test(expected = YtjConnectionException.class)
    public void testFindByYNimiWithoutCredentialsFails() throws Exception {
        String nimi = "Helsingin";
        boolean naytaPassiiviset = false;
        YTJKieli kieli = YTJKieli.FI;
        YTJServiceImpl instance = new YTJServiceImpl();
        instance.findByYNimi(nimi, naytaPassiiviset, kieli);
    }

    /*Ignore this test so that YTJ is not called every time when project is compiled
     * This test call YTJ-service with test credentials, note that
     * credentials have maximum daily call limit so test is ignored by default
     */
    @Ignore
    @Test
    public void testFindByNimiSuccess() {
        String nimi = "Katva";
        boolean naytaPassiiviset = false;
        YTJKieli kieli = YTJKieli.FI;
        YTJServiceImpl instance = new YTJServiceImpl("XX", "XX");
        try {
            List<YTJDTO> results = instance.findByYNimi(nimi,naytaPassiiviset,kieli);
            LOGGER.info("Got {} results", results.size());
            assertTrue(results.size() > 0);
        } catch (Exception exp) {
            LOGGER.info("Exception in findByNimiSuccess : {}",exp.toString());
            fail();
        }
    }

    @Test(expected = YtjConnectionException.class)
    public void testFindByYTunnusBatchWithoutCredentialsFails() throws Exception {
        List<String> ytunnus = new ArrayList<String>(){{add("1111111-1");}};
        YTJKieli kieli = YTJKieli.FI;
        YTJServiceImpl instance = new YTJServiceImpl();
        List<YTJDTO> result = instance.findByYTunnusBatch(ytunnus, kieli);
        assertEquals("Diibadaa", result.get(0).getNimi().trim()); //shouldn't reach this point
    }

    // Test of findByYTunnusBatch method, of class YTJServiceImpl
    @Ignore
    @Test
    public void testFindByYTunnusBatchSuccess() {
        List<String> ytunnus = new ArrayList<String>(){{add("0313471-7");add("0201256-6");add("2189312-7");}};
        YTJKieli kieli = YTJKieli.FI;
        YTJServiceImpl instance = new YTJServiceImpl("XX", "XX");
        try {
            List<YTJDTO> result = instance.findByYTunnusBatch(ytunnus, kieli);
            assertEquals("Helsingin yliopisto".toLowerCase(), result.get(0).getNimi().trim().toLowerCase());
            assertEquals("Helsingin kaupunki".toLowerCase(), result.get(1).getNimi().trim().toLowerCase());
            assertEquals("Mikkelin Ammattikorkeakoulu Oy".toLowerCase(), result.get(2).getNimi().trim().toLowerCase());
        } catch (Exception exp) {
            LOGGER.info("Exception in findByYTunnus : {}",exp.toString());
            fail();
        }
    }


    /**
     * Test of findByYTunnus method, of class YTJServiceImpl.
     */
    @Test(expected = YtjConnectionException.class)
    public void testFindByYTunnusWithoutCredentialsFails() throws Exception {
        String ytunnus = "1111111-1";
        YTJKieli kieli = YTJKieli.FI;
        YTJServiceImpl instance = new YTJServiceImpl();
        YTJDTO result = instance.findByYTunnus(ytunnus, kieli);
        assertEquals("Diibadaa", result.getNimi().trim()); //shouldn't reach this point
    }

    /*Ignore this test so that YTJ is not called every time when project is compiled
     * This test call YTJ-service with test credentials, note that
     * credentials have maximum daily call limit so test is ignored by default
     */
    @Ignore
    @Test
    public void testFindByYTunnusSuccess() {
        String ytunnus = "0313471-7"; // Helsingin yliopisto
        YTJKieli kieli = YTJKieli.FI;
        YTJServiceImpl instance = new YTJServiceImpl("XX", "XX");
        try {
            YTJDTO result = instance.findByYTunnus(ytunnus, kieli);
            assertEquals("Helsingin yliopisto".toLowerCase(), result.getNimi().trim().toLowerCase());
        } catch (Exception exp) {
            LOGGER.info("Exception in findByYTunnus : {}",exp.toString());
            fail();
        }
    }

}
