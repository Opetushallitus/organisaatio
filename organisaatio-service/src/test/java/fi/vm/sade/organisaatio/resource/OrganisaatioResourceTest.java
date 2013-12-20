package fi.vm.sade.organisaatio.resource;

import java.util.Random;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Joiner;

import fi.vm.sade.organisaatio.SecurityAwareTestBase;
import fi.vm.sade.organisaatio.api.model.GenericFault;
import fi.vm.sade.organisaatio.api.model.OrganisaatioService;
import fi.vm.sade.organisaatio.api.model.types.MonikielinenTekstiTyyppi;
import fi.vm.sade.organisaatio.api.model.types.MonikielinenTekstiTyyppi.Teksti;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioDTO;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.dao.OrganisaatioDAOImplTest;
import fi.vm.sade.organisaatio.integrationtest.OrganisaatioTstUtils;

@ContextConfiguration(locations = { "classpath:spring/test-context.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ActiveProfiles("embedded-solr")
public class OrganisaatioResourceTest extends SecurityAwareTestBase {

    @Autowired
    OrganisaatioResource res;

    Random r = new Random(0);

    private static final Logger LOG = LoggerFactory
            .getLogger(OrganisaatioDAOImplTest.class);

    @Value("${root.organisaatio.oid}")
    private String rootOrganisaatioOid;

    @Autowired
    OrganisaatioService organisaatioService;

    // @Autowired
    // OrganisaatioDAOImpl organisaatioDAO;
    //
    // @Autowired
    // OrganisaatioSuhdeDAOImpl organisaatioSuhdeDAO;

    @Test
    public void test() throws Exception {
        LOG.info("doTest()...");
        OrganisaatioDTO a = createOrganisaatio("A", null);
        OrganisaatioDTO b = createOrganisaatio("B", a);
        OrganisaatioDTO c = createOrganisaatio("C", b);
        OrganisaatioDTO d = createOrganisaatio("D", c);
        OrganisaatioDTO e = createOrganisaatio("E", d);

        String reference = Joiner.on("/").join(
                new String[] { rootOrganisaatioOid, a.getOid(), b.getOid(),
                        c.getOid(), d.getOid(), e.getOid() });

        String s = res.parentoids(e.getOid());
        Assert.assertEquals(reference, s);

    }

    int c = 0;

    private OrganisaatioDTO createOrganisaatio(String nimi,
            OrganisaatioDTO parent) throws GenericFault {
        LOG.info("createOrganisaatio({})", nimi);

        OrganisaatioDTO o = new OrganisaatioDTO();
        if (parent != null) {
            o.setParentOid(parent.getOid());
        }
        o.setOid(Long.toString(c++));
        o.getTyypit().add(OrganisaatioTyyppi.MUU_ORGANISAATIO);

        o.setNimi(new MonikielinenTekstiTyyppi());
        o.getNimi().getTeksti().add(new Teksti(nimi, "FI"));

        o.getYhteystiedot().add(OrganisaatioTstUtils.DEFAULT_POSTIOSOITE);

        o = organisaatioService.createOrganisaatio(o, true);

        return o;
    }

}
