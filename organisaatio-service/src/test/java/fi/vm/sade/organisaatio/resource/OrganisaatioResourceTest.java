package fi.vm.sade.organisaatio.resource;

import com.google.common.base.Joiner;
import fi.vm.sade.organisaatio.SecurityAwareTestBase;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.api.search.OrganisaatioHakutulos;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;
import fi.vm.sade.organisaatio.api.search.OrganisaatioSearchCriteria;
import fi.vm.sade.organisaatio.resource.dto.HakutoimistoDTO;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;
import fi.vm.sade.organisaatio.resource.dto.ResultRDTO;
import fi.vm.sade.organisaatio.resource.v2.OrganisaatioResourceV2;
import junit.framework.Assert;
import org.apache.commons.lang.StringUtils;
import org.junit.After;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@ContextConfiguration(locations = {"classpath:spring/test-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("embedded-solr")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class OrganisaatioResourceTest extends SecurityAwareTestBase {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    @Autowired
    private OrganisaatioResource res;

    @Autowired
    private OrganisaatioResourceV2 res2;

    @Autowired
    private IndexerResource solrIndexer;

    @Value("${root.organisaatio.oid}")
    private String rootOrganisaatioOid;

    @Override
    @Before
    public void before() {
        super.before();
        Locale.setDefault(Locale.US); // because of validaton messages
        executeSqlScript("data/basic_organisaatio_data.sql", false);
        solrIndexer.reBuildIndex(true); //rebuild index
    }

    @Override
    @After
    public void after() {
        executeSqlScript("data/truncate_tables.sql", false);
    }

    @Test
    public void testParentOids() throws Exception {
        String reference = Joiner.on("/").join(
                new String[]{rootOrganisaatioOid, "1.2.2004.1", "1.2.2004.3", "1.2.2005.4"});

        String s = res.parentoids("1.2.2005.4");
        Assert.assertEquals(reference, s);
    }

    @Test
    public void testChangeParentOid() throws Exception {
        String oldParentOid = "1.2.2004.1";
        String parentOid = "1.2.2004.5";

        assertChildCountFromIndex(oldParentOid, 2);
        assertChildCountFromIndex(parentOid, 0);

        // Change parent from root -> root2
        OrganisaatioRDTO node2foo = res.getOrganisaatioByOID("1.2.2004.3", false);
        node2foo.setParentOid(parentOid);
        ResultRDTO updated = res.updateOrganisaatio(node2foo.getOid(), node2foo);
        Assert.assertEquals("Parent oid should match!", parentOid, updated.getOrganisaatio().getParentOid());
        LOG.info("Path: {}", updated.getOrganisaatio().getParentOidPath());

        List<OrganisaatioRDTO> children = res.children(updated.getOrganisaatio().getOid(), false);
        Assert.assertEquals("Children count should match!", 2, children.size());
        for (OrganisaatioRDTO child : children) {
            LOG.info("Child oid path: {}, id path: {}", child.getParentOidPath());
            Assert.assertEquals("Child parent oid path should match!",
                    updated.getOrganisaatio().getParentOidPath() + child.getParentOid() + "|", child.getParentOidPath());
        }

        assertChildCountFromIndex(oldParentOid, 1);
        assertChildCountFromIndex(parentOid, 1);
    }

    @Test
    public void testSearchOrganisaatios() throws Exception {
        //Finding all koulutustoimijat
        OrganisaatioSearchCriteria searchCriteria = createOrgSearchCriteria(OrganisaatioTyyppi.KOULUTUSTOIMIJA.value(), null, null, true, null);
        OrganisaatioHakutulos result = res.searchHierarchy(searchCriteria);
        assertEquals(6, result.getNumHits());

        //List roots
        ArrayList<String> oidList = new ArrayList<String>();
        oidList.add("1.2.2004.1");
        oidList.add("1.2.2004.5");
        searchCriteria = createOrgSearchCriteria(null, null, null, true, oidList);
        result = res.searchHierarchy(searchCriteria);
        for (OrganisaatioPerustieto org : result.getOrganisaatiot()) {
            LOG.debug("ORG: {}", org.getOid());
        }
        assertEquals(7, result.getNumHits());

        //Finding all organisaatios with bar in name
        searchCriteria = createOrgSearchCriteria(null, null, "bar", true, null);
        result = res.searchHierarchy(searchCriteria);
        assertEquals(5, result.getNumHits());

        //Finding only organisaatios that are of oppilaitostyyppi Ammattikorkeakoulut
        searchCriteria = createOrgSearchCriteria(null, "oppilaitostyyppi_41#1", null, true, null);
        result = res.searchHierarchy(searchCriteria);
        assertEquals(2, result.getNumHits());
    }

    @Test
    public void testFetchingHakutoimisto() throws Exception {
        HakutoimistoDTO hakutoimisto = res2.hakutoimisto("1.2.2004.4");
        assertEquals(new HakutoimistoDTO("1.2.2004.4", "Hassuttimenkatu 2", "10000", "Äyhtävä"), hakutoimisto);
    }

    @Test(expected = fi.vm.sade.organisaatio.business.exception.OrganisaatioNotFoundException.class)
    public void testFetchingMissingHakutoimisto() throws Exception {
        res2.hakutoimisto("non.existing.oid");
    }

    private OrganisaatioSearchCriteria createOrgSearchCriteria(String organisaatioTyyppi, String oppilaitosTyyppi, String searchStr,
                                                               boolean suunnitellut, List<String> oids) {
        OrganisaatioSearchCriteria sc = new OrganisaatioSearchCriteria();
        sc.setOrganisaatioTyyppi(organisaatioTyyppi);//organisaatioTyyppi = organisaatioTyyppi;
        Set<String> tyypit = new HashSet<>();
        if (!StringUtils.isEmpty(oppilaitosTyyppi)) {
            tyypit.add(oppilaitosTyyppi);
        }
        sc.setOppilaitosTyyppi(tyypit);
        sc.setSearchStr(searchStr);
        sc.setSuunnitellut(suunnitellut);
        if (oids != null) {
            sc.setOidRestrictionList(oids);
        }
        return sc;

    }
}
