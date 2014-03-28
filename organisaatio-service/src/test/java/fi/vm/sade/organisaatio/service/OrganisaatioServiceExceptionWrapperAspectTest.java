package fi.vm.sade.organisaatio.service;

import fi.vm.sade.organisaatio.service.aspects.OrganisaatioServiceExceptionWrapperAspect;
import junit.framework.Assert;

import org.junit.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import fi.vm.sade.organisaatio.api.model.GenericFault;
import fi.vm.sade.organisaatio.api.model.OrganisaatioService;

public class OrganisaatioServiceExceptionWrapperAspectTest {

    @Test
    public void test() throws GenericFault {

        OrganisaatioServiceImpl target = new OrganisaatioServiceImpl();

        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        OrganisaatioServiceExceptionWrapperAspect aspect = new OrganisaatioServiceExceptionWrapperAspect();
        factory.addAspect(aspect);
        OrganisaatioService proxy = factory.getProxy();

        // test non wrapping method
        try {
            proxy.findBasicParentOrganisaatios(null);
            Assert.fail("should not succeed!");
        } catch (RuntimeException rte) {
            Assert.assertEquals("Unhandled exception : class java.lang.NullPointerException - null", rte.getMessage());
        }

        // test non AbstractOrganisaatioBusinessException
        try {
            proxy.createOrganisaatio(null, false);
            Assert.fail("should not succeed!");
        } catch (GenericFault gf) {
            Assert.assertEquals("java.lang.NullPointerException", gf.getFaultInfo().getErrorCode());
        }

        // test AbstractOrganisaatioBusinessException
        try {
            proxy.ping("!!");
        } catch (GenericFault gf) {
            Assert.assertEquals("organisaatio.exception.learning.institution.exists", gf.getFaultInfo().getErrorCode());
        }

    }

}
