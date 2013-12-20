package fi.vm.sade.koodisto.selenium;

import fi.vm.sade.support.selenium.SeleniumContext;
import fi.vm.sade.support.selenium.SeleniumTestCaseSupport;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import static fi.vm.sade.support.selenium.SeleniumUtils.*;

/**
 * @author Antti Salonen
 */
public class OrganisaatioWSSeleniumSystemTest extends SeleniumTestCaseSupport {

    public OrganisaatioWSSeleniumSystemTest() {
        super();
    }

    @Override
    public void initPageObjects() {
    }

    public void testOrganisaatioService() {
//        waitForPageSourceContains(":8181/cxf/organisaatioService?wsdl", "<xs:complexType name=\"ping\">");
        waitForPageSourceContains(":8181/cxf/organisaatioService?wsdl", "<wsdl:operation name=\"ping\">");
    }

    public void waitForPageSourceContains(final String relativeUrl, final String expectedContains) {
        (new WebDriverWait(getDriver(), TIME_OUT_IN_SECONDS, SLEEP_IN_MILLIS)).until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                String url = openRelative(relativeUrl);
                boolean contains = getDriver().getPageSource().contains(expectedContains);
                //DEBUGSAWAY:log.debug(this.getClass().getSimpleName() + " - url: " + url + ", expectedContains: " + expectedContains + ", contains: " + contains);
//                if (!contains) {
//                    System.out.println(this.getClass().getSimpleName()+" - page source: "+driver.getPageSource());
//                }
                return contains;
            }
        });
    }

    public String openRelative(String relativeUrl) {
        String url = SeleniumContext.getOphServerUrl() + relativeUrl;
        getDriver().get(url);
        return url;
    }


}
