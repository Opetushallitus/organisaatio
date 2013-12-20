/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.vm.sade.organisaatio.ui.widgets.factory;

import fi.vm.sade.organisaatio.api.model.OrganisaatioService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import fi.vm.sade.organisaatio.ui.widgets.OrganisaatioSearchTree;
import fi.vm.sade.organisaatio.ui.widgets.OrganisaatioSearchWidget;
import fi.vm.sade.organisaatio.ui.widgets.OrganisaatioSearchType;
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
public class OrganisaatioWidgetFactory implements ApplicationContextAware {

    private static ApplicationContext staticApplicationContext;
    private static int counter = 0;

    // TODO this is ugly, convert to nonstatic implementation asap
    private static OrganisaatioProxy organisaatioProxy;

    private static String rootOrganizationOid = null;

    public OrganisaatioWidgetFactory() {
        // empty
    }

    @Override
    public void setApplicationContext(ApplicationContext ac) throws BeansException {
        setStaticContext(ac);
    }

    /*
     * Creates instance of OrganisaatioSearchWidget, OrganisaatioSearchType enumeration
     * specifies which kind of OrganisaatioSearchWidget is created. OrganisaatioSearchWidget
     * can be simple popup with OrganisaatioTree and search field or it can contain more limit factors.
     *
     * @param OrganisaatioSearchType enumeration defining which kind of organisaatiowidget is returned
     * @return instantiated OrganisaatioSearchWidget
     */
    public static OrganisaatioSearchWidget createOrganisaatioSearchWidget(OrganisaatioSearchType type) {
        OrganisaatioSearchWidget orgSearch = new OrganisaatioSearchWidget(organisaatioProxy, OrganisaatioWidgetFactory.getOrganisaatioSearchTree(type, false));
        if (type == OrganisaatioSearchType.ALL_FIELDS) {
            orgSearch.setPopupHeight("520px");
        } else if (type == OrganisaatioSearchType.ADVANCED) {
            orgSearch.setPopupHeight("440px");
        }
     //   orgSearch.setRootOid(getRootOrgOid());

        return orgSearch;
    }


    /* Overloaded method createOrganisaatioSearchWidget, this method allows to specify
     * whether to search with Organisaatio's domain name
     * Creates instance of OrganisaatioSearchWidget, OrganisaatioSearchType enumeration
     * specifies which kind of OrganisaatioSearchWidget is created. OrganisaatioSearchWidget
     * can be simple popup with OrganisaatioTree and search field or it can contain more limit factors.
     *
     * @param OrganisaatioSearchType enumeration defining which kind of organisaatiowidget is returned
     * @param boolean searchWithDomainName specifies whether to display Organisaatio's domain name in search tree
     * @return instantiated OrganisaatioSearchWidget
     */
    public static OrganisaatioSearchWidget createOrganisaatioSearchWidget(OrganisaatioSearchType type, boolean searchWithDomainName) {
        OrganisaatioSearchWidget orgSearch = new OrganisaatioSearchWidget(organisaatioProxy, OrganisaatioWidgetFactory.getOrganisaatioSearchTree(type, searchWithDomainName));
        if (type == OrganisaatioSearchType.ALL_FIELDS) {
            orgSearch.setPopupHeight("520px");
        } else if (type == OrganisaatioSearchType.ADVANCED) {
            orgSearch.setPopupHeight("440px");
        }
        orgSearch.setShowDomainName(searchWithDomainName);
      //  orgSearch.setRootOid(getRootOrgOid());
        return orgSearch;
    }

    /*
     * This method takes Spring application context as a parameter and creates
     * OrganisaatioService using it.
     *
     * @param ApplicationContext spring application context
     */
    private static void setStaticContext(ApplicationContext context) {
        OrganisaatioWidgetFactory.staticApplicationContext = context;
        OrganisaatioService organisaatioService = staticApplicationContext.getBean(OrganisaatioService.class);
        organisaatioProxy = new OrganisaatioProxyCachingImpl(organisaatioService);
    }

    public static void setOrganisaatioService(OrganisaatioService orgService) {
        // TODO static... ugly - fixme
        organisaatioProxy = new OrganisaatioProxyCachingImpl(orgService);
    }

    private static OrganisaatioSearchTree getOrganisaatioSearchTree(OrganisaatioSearchType type, boolean showDomainName) {
        OrganisaatioSearchTree orgSearchTree = new OrganisaatioSearchTree("org" + counter + "_", new OrganisaatioTreeAdapter(organisaatioProxy, showDomainName), type);
        counter++;
        return orgSearchTree;
    }

    public static OrganisaatioSearchTree getOrganisaatioSearchTree() {
        OrganisaatioSearchTree orgSearchTree = new OrganisaatioSearchTree("org_", new OrganisaatioTreeAdapter(organisaatioProxy));
        return orgSearchTree;
    }

  //  private static String getRootOrgOid() {
    //   if (rootOrganizationOid == null) {
    //        rootOrganizationOid = "1.2.246.562.10.00000000001";
    //    }
    //    return rootOrganizationOid;
   // }

   // public String getRootOrganizationOid() {
   //     if (rootOrganizationOid == null) {
   //         rootOrganizationOid = "1.2.246.562.10.00000000001";
   //     }
   //     return rootOrganizationOid;
   // }

  //  public void setRootOrganizationOid(String rootOrganizationOid) {
   //     OrganisaatioWidgetFactory.rootOrganizationOid = rootOrganizationOid;
  //  }
}
