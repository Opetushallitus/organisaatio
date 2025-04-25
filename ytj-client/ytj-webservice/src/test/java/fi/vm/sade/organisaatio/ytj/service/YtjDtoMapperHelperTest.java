/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.vm.sade.organisaatio.ytj.service;

import fi.vm.sade.organisaatio.ytj.api.YTJDTO;
import fi.ytj.ToiminimiDTO;
import fi.ytj.YTunnusDTO;
import fi.ytj.YritysHakuDTO;
import fi.ytj.YritysTiedotV2DTO;
import java.util.List;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Tuomas
 */
public class YtjDtoMapperHelperTest {

    public YtjDtoMapperHelperTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of mapYritysTiedotV2DTOtoYTJDTO method, of class YtjDtoMapperHelper.
     *
     * Check that mapper return object even when almost every field is missing
     */
    @Test
    public void testMapYritysTiedotV2DTOtoYTJDTO() {
        System.out.println("mapYritysTiedotV2DTOtoYTJDTO");
        YritysTiedotV2DTO vastaus = new YritysTiedotV2DTO();
        YTunnusDTO yTunnus = new YTunnusDTO();
        yTunnus.setYTunnus("11111-1");
        vastaus.setYritysTunnus(yTunnus);
        ToiminimiDTO nimi = new ToiminimiDTO();
        nimi.setToiminimi("Diibadaa");
        vastaus.setToiminimi(nimi);
        YtjDtoMapperHelper instance = new YtjDtoMapperHelper();

        YTJDTO result = instance.mapYritysTiedotV2DTOtoYTJDTO(vastaus);
        assertEquals("Diibadaa", result.getNimi());

    }

    /**
     * Test of mapYritysHakuDTOListToDtoList method, of class YtjDtoMapperHelper.
     */
    @Test
    public void testMapYritysHakuDTOListToDtoList() {
        System.out.println("mapYritysHakuDTOListToDtoList");
        List<YritysHakuDTO> vastaukset = null;
        YtjDtoMapperHelper instance = new YtjDtoMapperHelper();
        var result = instance.mapYritysHakuDTOListToDtoList(vastaukset);
        assertEquals(null, result);

    }

    /**
     * Test of mapYritysHakuDTOToDto method, of class YtjDtoMapperHelper.
     */
    @Test
    public void testMapYritysHakuDTOToDto() {
        System.out.println("mapYritysHakuDTOToDto");
        YritysHakuDTO ytjParam = new YritysHakuDTO();
        ytjParam.setYritysnimi("Diiba");
        YtjDtoMapperHelper instance = new YtjDtoMapperHelper();
        YTJDTO result = instance.mapYritysHakuDTOToDto(ytjParam);
        assertEquals("Diiba", result.getNimi().trim());

    }
}
