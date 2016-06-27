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

    private Organisaatio org;
    private List<OrganisaatioNimi> orgSortedNimet;
    private List<Yhteystieto> orgSortedYhteystiedot;
    private List<Organisaatio> organisaatioList;

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
        oidList.addAll(organisaatioDAO.findOidsBy(true, OrganisaatioYtjServiceImpl.SEARCH_LIMIT, 0, OrganisaatioTyyppi.KOULUTUSTOIMIJA));
        oidList.addAll(organisaatioDAO.findOidsBy(true, OrganisaatioYtjServiceImpl.SEARCH_LIMIT, 0, OrganisaatioTyyppi.TYOELAMAJARJESTO));
        oidList.addAll(organisaatioDAO.findOidsBy(true, OrganisaatioYtjServiceImpl.SEARCH_LIMIT, 0, OrganisaatioTyyppi.MUU_ORGANISAATIO));
        organisaatioList = organisaatioDAO.findByOidList(oidList, OrganisaatioYtjServiceImpl.SEARCH_LIMIT);

        Assert.assertEquals(3, organisaatioList.size());

        initTestData(0);
        // Case: Has sv name; gets new fi name from YTJ, no puhelin, www, alkupvm updated
        // name history not updated
        Assert.assertEquals(1, org.getNimet().size());
        // MonikielinenTeksti added for new lang
        Assert.assertEquals(2, org.getNimi().getValues().size());
        Assert.assertEquals("Helsingin yliopistomuseon säätiö", orgSortedNimet.get(0).getNimi().getString("fi"));
        Assert.assertEquals("node231 foo bar", orgSortedNimet.get(0).getNimi().getString("sv"));
        Assert.assertEquals(923864400000L, org.getNimet().get(0).getAlkuPvm().getTime());
        Assert.assertEquals("Mannerheimintie 2", org.getPostiosoiteByKieli(OrganisaatioYtjServiceImpl.KIELI_KOODI_SV).getOsoite());
        Assert.assertEquals("Tie 1", org.getPostiosoiteByKieli(OrganisaatioYtjServiceImpl.KIELI_KOODI_FI).getOsoite());
        Assert.assertEquals("posti_00100", ((Osoite) orgSortedYhteystiedot.get(1)).getPostinumero());
        Assert.assertEquals("posti_00100", ((Osoite) orgSortedYhteystiedot.get(0)).getPostinumero());
        Assert.assertEquals("http://www.ytj.fi", org.getWww().getWwwOsoite());
        Assert.assertEquals("0100000211", org.getPuhelin(Puhelinnumero.TYYPPI_PUHELIN).getPuhelinnumero());
        Assert.assertEquals(OrganisaatioYtjServiceImpl.ORG_KIELI_KOODI_FI, org.getKielet().get(0));
        Assert.assertEquals(915141600000L, org.getAlkuPvm().getTime());
        Assert.assertEquals(OrganisaatioYtjServiceImpl.KIELI_KOODI_FI, org.getYtjKieli());

        initTestData(1);
        // Case: Has fi and sv name, puhelin, www, alkupvm; gets fi name updated from YTJ
        // new entry to name history
        Assert.assertEquals(2, org.getNimet().size());
        Assert.assertEquals("root test koulutustoimija", orgSortedNimet.get(0).getNimi().getString("fi"));
        Assert.assertEquals("Katva Consulting", orgSortedNimet.get(1).getNimi().getString("fi"));
        Assert.assertEquals("root test utbildningsoperator", orgSortedNimet.get(1).getNimi().getString("sv"));
        Assert.assertEquals(-7200000L, org.getNimet().get(0).getAlkuPvm().getTime());
        Assert.assertEquals(918597600000L, org.getNimet().get(1).getAlkuPvm().getTime());
        Assert.assertEquals("Ygankuja 1", org.getPostiosoiteByKieli(OrganisaatioYtjServiceImpl.KIELI_KOODI_FI).getOsoite());
        Assert.assertEquals("posti_00100", ((Osoite) orgSortedYhteystiedot.get(0)).getPostinumero());
        Assert.assertEquals(OrganisaatioYtjServiceImpl.ORG_KIELI_KOODI_FI, org.getKielet().get(0));
        Assert.assertEquals("12345", org.getPuhelin(Puhelinnumero.TYYPPI_PUHELIN).getPuhelinnumero());
        Assert.assertEquals("http://www.oph.fi", org.getWww().getWwwOsoite());
        Assert.assertNotEquals(org.getNimet().get(0).getNimi(), organisaatioList.get(1).getNimet().get(1).getNimi());
        Assert.assertEquals(1091912400000L, org.getAlkuPvm().getTime());
        Assert.assertEquals(OrganisaatioYtjServiceImpl.KIELI_KOODI_FI, org.getYtjKieli());

        initTestData(2);
        // Case: Has fi name, puhelin, www, alkupvm; gets new sv name and updated puhelin, www from YTJ;
        // alkupvm not updated since ytj invalid data
        Assert.assertEquals(1, org.getNimet().size());
        Assert.assertEquals("Ruotsalainen & koulutustoimija", orgSortedNimet.get(0).getNimi().getString("sv"));
        Assert.assertEquals("root2 test2 koulutustoimija2", orgSortedNimet.get(0).getNimi().getString("fi"));
        Assert.assertEquals(921103200000L, org.getNimet().get(0).getAlkuPvm().getTime());
        // TODO with old implementation this assert fails, there is a bug (updates fi address for sv org if sv address doesn't exist)!
        //Assert.assertEquals("Svenska gatan 1", org.getPostiosoiteByKieli(OrganisaatioYtjServiceImpl.KIELI_KOODI_SV).getOsoite());
        Assert.assertEquals("posti_00100", ((Osoite) orgSortedYhteystiedot.get(0)).getPostinumero());
        Assert.assertEquals(OrganisaatioYtjServiceImpl.ORG_KIELI_KOODI_SV, org.getKielet().get(0));
        Assert.assertEquals("0100000210", org.getPuhelin(Puhelinnumero.TYYPPI_PUHELIN).getPuhelinnumero());
        Assert.assertEquals("http://www.ytj.sv", org.getWww().getWwwOsoite());
        Assert.assertEquals(2, org.getNimet().get(0).getNimi().getValues().size());
        Assert.assertEquals(1151528400000L, org.getAlkuPvm().getTime());
        Assert.assertEquals(OrganisaatioYtjServiceImpl.KIELI_KOODI_SV, org.getYtjKieli());
    }

    private void initTestData(int index) {
        org = organisaatioList.get(index);
        orgSortedNimet = new ArrayList<>();
        sortOrganisaatioNimet(index, orgSortedNimet);
        orgSortedYhteystiedot = new ArrayList<>();
        sortOrganisaatioYhteystiedot(index, orgSortedYhteystiedot);
    }

    private void sortOrganisaatioYhteystiedot(int id, List<Yhteystieto> orgSortedYhteystiedot) {
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

    private void sortOrganisaatioNimet(int id, List<OrganisaatioNimi> orgNimet) {
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
