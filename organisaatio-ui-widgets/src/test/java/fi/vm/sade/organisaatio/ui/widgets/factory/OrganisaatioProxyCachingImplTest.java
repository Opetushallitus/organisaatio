package fi.vm.sade.organisaatio.ui.widgets.factory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Collection;

import junit.framework.Assert;

import org.junit.Test;

import com.google.common.collect.Lists;

import fi.vm.sade.organisaatio.api.OrganisaatioServiceMock;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioDTO;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioSearchCriteriaDTO;

public class OrganisaatioProxyCachingImplTest {

    @Test
    public void testBasic() {

        try {
            new OrganisaatioProxyCachingImpl(null);
            fail("should not scceed");
        } catch (RuntimeException rte) {
            // expected
        }

        OrganisaatioServiceMock mock = new OrganisaatioServiceMock();

        OrganisaatioProxyCachingImpl cache = new OrganisaatioProxyCachingImpl(mock);
        Collection<OrganisaatioDTO> result = cache.find(null);
        assertNotNull(result);
        assertEquals(0, result.size());

        // verify we get expected content from cache
        Assert.assertEquals("1.2.2004.1", cache.findByOid("1.2.2004.1").getOid());
        //Assert.assertEquals(1, cache.findByParentOids(Lists.newArrayList("1.2.2004.1")).size());
        OrganisaatioSearchCriteriaDTO criteria = new OrganisaatioSearchCriteriaDTO();
        criteria.setSearchStr("1234567-1");
        Assert.assertEquals(1, cache.find(criteria).size());
    }

}
