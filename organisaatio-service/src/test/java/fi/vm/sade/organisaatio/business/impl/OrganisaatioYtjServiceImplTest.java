package fi.vm.sade.organisaatio.business.impl;

import fi.vm.sade.organisaatio.SecurityAwareTestBase;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.business.OrganisaatioBusinessService;
import fi.vm.sade.organisaatio.business.OrganisaatioYtjService;
import fi.vm.sade.organisaatio.dao.OrganisaatioDAO;
import fi.vm.sade.organisaatio.dto.mapping.SearchCriteriaModelMapper;
import fi.vm.sade.organisaatio.model.*;
import fi.vm.sade.organisaatio.resource.IndexerResource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@ContextConfiguration(locations = {"classpath:spring/test-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles({"embedded-solr"})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class OrganisaatioYtjServiceImplTest extends SecurityAwareTestBase {

    @Autowired
    private OrganisaatioDAO organisaatioDAO;
    @Autowired
    private OrganisaatioYtjService service;
    @Autowired
    private IndexerResource indexer;

    @Before
    public void setUp() {
        executeSqlScript("data/basic_organisaatio_data.sql", false);
        indexer.reBuildIndex(true);
    }

    @After
    public void tearDown() {
        executeSqlScript("data/truncate_tables.sql", false);
    }

    @Test
    public void updateYTJDataTest() {
        service.updateYTJData(false);
        // verify that the database is updated properly
        List<String> oidList = new ArrayList<>();
        List<Organisaatio> organisaatioList;
        oidList.addAll(organisaatioDAO.findOidsBy(true, 10000, 0, OrganisaatioTyyppi.KOULUTUSTOIMIJA));
        oidList.addAll(organisaatioDAO.findOidsBy(true, 10000, 0, OrganisaatioTyyppi.TYOELAMAJARJESTO));
        oidList.addAll(organisaatioDAO.findOidsBy(true, 10000, 0, OrganisaatioTyyppi.MUU_ORGANISAATIO));
        organisaatioList = organisaatioDAO.findByOidList(oidList, 10000);
        Assert.assertEquals(3, organisaatioList.size());

        int id = 0;
        List<OrganisaatioNimi> orgSortedNimet = new ArrayList<>();
        sortOrganisaatioNimet(organisaatioList, id, orgSortedNimet);
        List<Yhteystieto> orgSortedYhteystiedot = new ArrayList<>();
        sortOrganisaatioYhteystiedot(organisaatioList, id, orgSortedYhteystiedot);
        // Case: Has sv name; gets new fi name from YTJ, no puhelin, www, alkupvm updated
        Assert.assertEquals(1, organisaatioList.get(id).getNimet().size());
        Assert.assertEquals("Helsingin yliopistomuseon säätiö", orgSortedNimet.get(0).getNimi().getString("fi"));
        Assert.assertEquals("node231 foo bar", orgSortedNimet.get(0).getNimi().getString("sv"));
        Assert.assertEquals(923864400000L, organisaatioList.get(id).getNimet().get(0).getAlkuPvm().getTime());
        Assert.assertEquals("Mannerheimintie 2", ((Osoite)orgSortedYhteystiedot.get(0)).getOsoite());
        Assert.assertEquals("Tie 1", ((Osoite)orgSortedYhteystiedot.get(1)).getOsoite());
        Assert.assertEquals("posti_00100", ((Osoite)orgSortedYhteystiedot.get(1)).getPostinumero());
        Assert.assertEquals("posti_00100", ((Osoite)orgSortedYhteystiedot.get(0)).getPostinumero());
        Assert.assertEquals("oppilaitoksenopetuskieli_1#1", organisaatioList.get(id).getKielet().get(0));
        Assert.assertEquals(915141600000L, organisaatioList.get(id).getAlkuPvm().getTime());

        id = 1;
        orgSortedNimet = new ArrayList<>();
        sortOrganisaatioNimet(organisaatioList, id, orgSortedNimet);
        orgSortedYhteystiedot = new ArrayList<>();
        sortOrganisaatioYhteystiedot(organisaatioList, id, orgSortedYhteystiedot);
        // Case: Has fi and sv name, puhelin, www, alkupvm; gets fi name updated from YTJ
        Assert.assertEquals(2, organisaatioList.get(id).getNimet().size());
        Assert.assertEquals("root test koulutustoimija", orgSortedNimet.get(0).getNimi().getString("fi"));
        Assert.assertEquals("Katva Consulting", orgSortedNimet.get(1).getNimi().getString("fi"));
        Assert.assertEquals("root test utbildningsoperator", orgSortedNimet.get(1).getNimi().getString("sv"));
        Assert.assertEquals(-7200000L, organisaatioList.get(id).getNimet().get(0).getAlkuPvm().getTime());
        Assert.assertEquals(918597600000L, organisaatioList.get(id).getNimet().get(1).getAlkuPvm().getTime());
        Assert.assertEquals("Ygankuja 1", ((Osoite)orgSortedYhteystiedot.get(0)).getOsoite());
        Assert.assertEquals("posti_00100", ((Osoite)orgSortedYhteystiedot.get(0)).getPostinumero());
        Assert.assertEquals("oppilaitoksenopetuskieli_1#1", organisaatioList.get(id).getKielet().get(0));
        Assert.assertEquals("12345", organisaatioList.get(id).getPuhelin(Puhelinnumero.TYYPPI_PUHELIN).getPuhelinnumero());
        Assert.assertEquals("http://www.oph.fi", ((Www)orgSortedYhteystiedot.get(4)).getWwwOsoite());
        Assert.assertNotEquals(organisaatioList.get(id).getNimet().get(0).getNimi(), organisaatioList.get(1).getNimet().get(1).getNimi());
        Assert.assertEquals(1091912400000L, organisaatioList.get(id).getAlkuPvm().getTime());

        id = 2;
        orgSortedNimet = new ArrayList<>();
        sortOrganisaatioNimet(organisaatioList, id, orgSortedNimet);
        orgSortedYhteystiedot = new ArrayList<>();
        sortOrganisaatioYhteystiedot(organisaatioList, id, orgSortedYhteystiedot);
        // Case: Has fi name, puhelin, www, alkupvm; gets new sv name and updated puhelin, www from YTJ;
        // alkupvm not updated since ytj invalid data
        Assert.assertEquals(1, organisaatioList.get(id).getNimet().size());
        Assert.assertEquals("Ruotsalainen & koulutustoimija", orgSortedNimet.get(0).getNimi().getString("sv"));
        Assert.assertEquals("root2 test2 koulutustoimija2", orgSortedNimet.get(0).getNimi().getString("fi"));
        Assert.assertEquals(921103200000L, organisaatioList.get(id).getNimet().get(0).getAlkuPvm().getTime());
        Assert.assertEquals("Svenska gatan 1", ((Osoite)orgSortedYhteystiedot.get(0)).getOsoite());
        Assert.assertEquals("posti_00100", ((Osoite)orgSortedYhteystiedot.get(0)).getPostinumero());
        Assert.assertEquals("oppilaitoksenopetuskieli_2#1", organisaatioList.get(id).getKielet().get(0));
        Assert.assertEquals("0100000210", organisaatioList.get(id).getPuhelin(Puhelinnumero.TYYPPI_PUHELIN).getPuhelinnumero());
        Assert.assertEquals("http://www.ytj.sv", ((Www)orgSortedYhteystiedot.get(4)).getWwwOsoite());
        Assert.assertEquals(2, organisaatioList.get(id).getNimet().get(0).getNimi().getValues().size());
        Assert.assertEquals(1151528400000L, organisaatioList.get(id).getAlkuPvm().getTime());
   }

    private void sortOrganisaatioYhteystiedot(List<Organisaatio> organisaatioList, int id, List<Yhteystieto> orgSortedYhteystiedot) {
        for(Yhteystieto yhteystieto : organisaatioList.get(id).getYhteystiedot()) {
            orgSortedYhteystiedot.add(yhteystieto);
        }
        Collections.sort(orgSortedYhteystiedot, new Comparator<Yhteystieto>() {
            @Override
            public int compare(Yhteystieto o1, Yhteystieto o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });
    }

    private void sortOrganisaatioNimet(List<Organisaatio> organisaatioList, int id, List<OrganisaatioNimi> orgNimet) {
        for(OrganisaatioNimi orgNimi : organisaatioList.get(id).getNimet()) {
            orgNimet.add(orgNimi);
        }
        Collections.sort(orgNimet, new Comparator<OrganisaatioNimi>() {
            @Override
            public int compare(OrganisaatioNimi o1, OrganisaatioNimi o2) {
                return o1.getAlkuPvm().compareTo(o2.getAlkuPvm());
            }
        });
    }


}
