package fi.vm.sade.organisaatio.ui.widgets.test;


/*
 *
 * Copyright (c) 2012 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software:  Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * European Union Public Licence for more details.
 */
import com.bsb.common.vaadin.embed.EmbedVaadinServer;
import com.bsb.common.vaadin.embed.support.EmbedVaadin;
import com.vaadin.data.util.NestedMethodProperty;
import fi.vm.sade.organisaatio.api.OrganisaatioServiceMock;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioDTO;
import fi.vm.sade.organisaatio.ui.widgets.OrganisaatioSearchType;
import fi.vm.sade.organisaatio.ui.widgets.OrganisaatioSearchWidget;
import fi.vm.sade.organisaatio.ui.widgets.factory.OrganisaatioWidgetFactory;
import fi.vm.sade.support.selenium.SeleniumContext;
import fi.vm.sade.support.selenium.SeleniumTestCaseSupport;
import fi.vm.sade.support.selenium.SeleniumUtils;
import java.util.concurrent.TimeUnit;
import org.junit.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
/**
 *
 * @author Tuomas Katva
 */
@Ignore
@RunWith(org.junit.runners.JUnit4.class)
public class OVT_748_OrganisaatioUIWidgetBasicTest extends SeleniumTestCaseSupport {

    private EmbedVaadinServer server;
    private OrganisaatioSearchWidget orgWidget;
    private int httpPort;
    private OrganisaatioDTO orgDto;
    private final String BASIC_ORG_SEARCH_VAR = "Essecraft";
    private final String ESSE_OID = "1.2.2004.9";
    private final String ADVANCED_ORG_SEARCH = "root test koulutustoimija";
    private final String SEARCH_RESULT_DEBUG_ID = "orgSearchWidget_searchResultLbl";
    private final String SEARCH_BTN_DEGUB_ID = "orgSearchWidget_searchBtn";

    @Before
    public void setUp() throws Exception {
        FirefoxProfile profile = new FirefoxProfile();
        driver = new FirefoxDriver(profile);
         SeleniumContext.setDriver(driver);
        OrganisaatioServiceMock orgServiceMock = new OrganisaatioServiceMock();
        OrganisaatioWidgetFactory.setOrganisaatioService(orgServiceMock);

    }

    private void startServerForComponent(OrganisaatioSearchWidget orgSearchWidget) {
        orgWidget = orgSearchWidget;
        if (server != null) {
            server.stop();
        }
        server = EmbedVaadin.forComponent(orgWidget).wait(false).start();
        httpPort = server.getConfig().getPort();
        driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void TestBasicWidget() {
        orgWidget = OrganisaatioWidgetFactory.createOrganisaatioSearchWidget(OrganisaatioSearchType.BASIC);
        orgDto = new OrganisaatioDTO();
        orgWidget.setPropertyDataSource(new NestedMethodProperty(orgDto, "oid"));
        startServerForComponent(orgWidget);
        if(!(driver instanceof FirefoxDriver)) {
            return;
        }
        driver.get("http://localhost:" + httpPort);
        driver.findElement(By.id(orgWidget.getSearchButton().getDebugId())).click();
        WebElement item = SeleniumUtils.waitForElement(By.xpath("//span[contains(.,'" + BASIC_ORG_SEARCH_VAR + "')]"));
        item.click();
        orgWidget.commit();
        assertEquals(orgDto.getOid(), ESSE_OID);


    }

     @SuppressWarnings("unchecked")
    @Test
    public void testAdvancedOrganisaatioSearch() {
        orgWidget = OrganisaatioWidgetFactory.createOrganisaatioSearchWidget(OrganisaatioSearchType.BASIC,true);
        orgDto = new OrganisaatioDTO();
        orgWidget.setPropertyDataSource(new NestedMethodProperty(orgDto, "oid"));
        startServerForComponent(orgWidget);
        if(!(driver instanceof FirefoxDriver)) {
            return;
        }
        driver.get("http://localhost:" + httpPort);
        driver.findElement(By.id(orgWidget.getSearchButton().getDebugId())).click();

        WebElement item = SeleniumUtils.waitForElement(By.xpath("//span[contains(.,'" + ADVANCED_ORG_SEARCH + "')]"));

        item.click();
        SeleniumUtils.waitForElement(By.id(orgWidget.getSearchValueLabel().getDebugId()));
        orgWidget.commit();
        SeleniumUtils.STEP("Label name was : " + orgWidget.getSelectedName());
        assertTrue(orgWidget.getSelectedName().startsWith("www"));

    }

    @Override
    public void initPageObjects() {
    }

    /*
     * Must quit driver because GlobalWebDriverRunner cannot be used,
     * it need initialized Spring Application Context but OrganisaatioWidget
     * gets its ApplicationContext from using application
     */

    @After
    public void tearDown() throws Exception {
        //super.tearDown();
        driver.quit();
        if (server != null) {
            server.stop();
        }

    }
}
