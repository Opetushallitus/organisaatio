/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fi.vm.sade.organisaatio.ui.widgets.test;

import com.bsb.common.vaadin.embed.EmbedVaadinServer;
import com.bsb.common.vaadin.embed.support.EmbedVaadin;
import com.vaadin.data.util.NestedMethodProperty;
import org.junit.Before;
import org.junit.Test;

import fi.vm.sade.organisaatio.api.OrganisaatioServiceMock;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioDTO;
import fi.vm.sade.support.selenium.SeleniumTestCaseSupport;
import fi.vm.sade.organisaatio.ui.widgets.factory.OrganisaatioWidgetFactory;
//import fi.vm.sade.organisaatio.api.OrganisaatioServiceMock;
import fi.vm.sade.organisaatio.ui.widgets.OrganisaatioSearchType;
import fi.vm.sade.organisaatio.ui.widgets.OrganisaatioSearchWidget;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.assertTrue;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import fi.vm.sade.support.selenium.SeleniumUtils;
import org.junit.After;
/**
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

/**
 *
 * @author Tuomas Katva
 */
@Ignore
@RunWith(org.junit.runners.JUnit4.class)
public class OVT_748_OrganisaatioUiWidgetAdvancedTest extends SeleniumTestCaseSupport{

    private EmbedVaadinServer server;
    private OrganisaatioSearchWidget orgWidget;
    private int httpPort;
    private OrganisaatioDTO orgDto;
    private final String ADVANCED_ORG_SEARCH = "root test koulutustoimija";
    private final String SEARCH_RESULT_DEBUG_ID = "orgSearchWidget_searchResultLbl";

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        OrganisaatioServiceMock orgServiceMock = new OrganisaatioServiceMock();
        OrganisaatioWidgetFactory.setOrganisaatioService(orgServiceMock);

    }

    private void startServerForComponent(OrganisaatioSearchWidget orgSearchWidget) {
        orgWidget = orgSearchWidget;
        server = EmbedVaadin.forComponent(orgWidget).wait(false).start();
        httpPort = server.getConfig().getPort();
        driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
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
        driver.findElement(By.id("orgSearchWidget_searchBtn")).click();

        WebElement item = SeleniumUtils.waitForElement(By.xpath("//span[contains(.,'" + ADVANCED_ORG_SEARCH + "')]"));

        item.click();
        SeleniumUtils.waitForElement(By.id(SEARCH_RESULT_DEBUG_ID));
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
        super.tearDown();
        driver.quit();
        driver.close();
        if (server != null) {
        server.stop();
        }

    }
}
