/*
 *
 * Copyright (c) 2012 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software:  Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versionsOO
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
package fi.vm.sade.organisaatio.ui;

import com.vaadin.Application;
import com.vaadin.service.ApplicationContext;

import fi.vm.sade.generic.ui.app.AbstractBlackboardSadeApplication;
import fi.vm.sade.organisaatio.api.model.OrganisaatioService;
import fi.vm.sade.organisaatio.auth.OrganisaatioPermissionServiceImpl;
import fi.vm.sade.organisaatio.service.search.OrganisaatioSearchService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public abstract class PortletRole extends AbstractBlackboardSadeApplication implements ApplicationContext.TransactionListener {

    private static ThreadLocal<PortletRole> tl = new ThreadLocal<PortletRole>();
    private static final long serialVersionUID = 2L;

    private UserContext userContext;
    @Autowired(required = true)
    protected OrganisaatioService organisaatioService;
    @Autowired(required = true)
    protected OrganisaatioPermissionServiceImpl permissionServiceImpl;
    @Value("${root.organisaatio.oid}")
    private String ophOid = null;
    @Autowired(required = true)
    protected OrganisaatioSearchService organisaatioSearchService;

    @Override
    public synchronized void init() {
        super.init();
        getContext().addTransactionListener(this);
        this.transactionStart(this, null);
        this.userContext = new UserContext(organisaatioSearchService, permissionServiceImpl);
    }

    @Override
    public void transactionStart(Application application, Object transactionData) {
        super.transactionStart(application, transactionData);
        if (application == this) {
            tl.set(this);
        }
    }

    @Override
    public void transactionEnd(Application application, Object transactionData) {
        if (application == this) {
            tl.remove();
        }
        super.transactionEnd(application, transactionData);
    }

    public static PortletRole getInstance() {
        if (tl.get() == null) {
            throw new RuntimeException("An unknown application error, PortletRole class instance not found from ThreadLocal.");
        }

        return tl.get();
    }

    public OrganisaatioPermissionServiceImpl getPermissionService() {
        return permissionServiceImpl;
    }

    public UserContext getUserContext() {
        return userContext;
    }
    
    @Override
    public void close() {
        super.close();
        getContext().removeTransactionListener(this);
    }

}
