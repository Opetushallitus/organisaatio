/*
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
package fi.vm.sade.organisaatio.ui;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;
import fi.vm.sade.organisaatio.auth.OrganisaatioPermissionServiceImpl;
import fi.vm.sade.organisaatio.helper.OrganisaatioDisplayHelper;
import fi.vm.sade.organisaatio.service.search.OrganisaatioSearchService;

public class UserContext implements Serializable {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final long serialVersionUID = 1L;
    private boolean useRestriction = false;
    private OrganisaatioPermissionServiceImpl permissionService;
    private Set<String> userOrganisations;
    private final String orgTitle;

    public UserContext(OrganisaatioSearchService organisaatioSearchService, OrganisaatioPermissionServiceImpl permissionService) {
        this.permissionService = permissionService;
        userOrganisations=parseUserOrganisations(SecurityContextHolder.getContext().getAuthentication());
        List<String> nameParts = Lists.newArrayList();

        Preconditions.checkNotNull(organisaatioSearchService);
        List<OrganisaatioPerustieto> org = organisaatioSearchService.findByOidSet(userOrganisations);

        for (OrganisaatioPerustieto organisaatioPerus : org) {
            if(org!=null) {
                nameParts.add(I18N.getLocale() != null ? OrganisaatioDisplayHelper.getClosestBasic(I18N.getLocale(), organisaatioPerus) : OrganisaatioDisplayHelper.getAvailableNameBasic(organisaatioPerus));
            }
        }
        orgTitle = Joiner.on(" ").join(nameParts);
        
        this.useRestriction = isShowRestrictionComponent();
    }

    private Set<String> parseUserOrganisations(Authentication authentication) {
        if(authentication==null) {
            logger.warn("user is not authenticated!");
            return Sets.newHashSet();
        }
        Set<String> orgs = Sets.newHashSet();
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (authority.getAuthority().contains(
                    OrganisaatioPermissionServiceImpl.ORGANISAATIOHALLINTA)) {
                String authorityString = authority.getAuthority();
                String orgOid = authorityString.substring(authorityString
                        .lastIndexOf("_") + 1);
                if (orgOid.startsWith("1")) {
                    // XXX this is hack, use userservice?
                    orgs.add(orgOid);
                }
            }

        }
        return orgs;
    }

    public String getOphOid() {
        return permissionService.getRootOrgOid();
    }

    public void setUseRestriction(boolean useRestriction) {
        this.useRestriction = useRestriction;
    }

    /**
     * Use restriction in search?
     * 
     * @return
     */
    public boolean isUseRestriction() {
        return useRestriction;
    }

    /**
     * Is current user part of "OPH"
     * 
     * @return
     */
    public boolean isOPHUser() {
        return userOrganisations.contains(permissionService.getRootOrgOid());
    }

    /**
     * Returns true if auto search is executed on search page.
     * 
     * If user is from OPH or has multiple organizations no auto search is executed. 
     * For all others automatic search is done.
     * 
     * @return
     */
    public boolean isDoAutoSearch() {
        return !isOPHUser();
    }

    /**
     * Returns true if restriction components should be displayed. Restriction
     * component is displayed if user has 1 organisation that is not "oph".
     * 
     * @return
     */
    public boolean isShowRestrictionComponent() {
        return userOrganisations!=null && !isOPHUser();
    }


    public Collection<String> getUserOrganisaatios() {
        return userOrganisations;
    }
    
    /**
     * Return display name for restriction component.
     */
    public String getOrgTitle(){
        return orgTitle;
    }
}
