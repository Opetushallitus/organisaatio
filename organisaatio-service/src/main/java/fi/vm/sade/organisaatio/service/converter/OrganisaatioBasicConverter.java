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

package fi.vm.sade.organisaatio.service.converter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import fi.vm.sade.organisaatio.api.model.types.MonikielinenTekstiTyyppi;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioDTO;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioPerustietoType;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.model.MonikielinenTeksti;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.dto.OrgPerustieto;

/**
 *
 * @author Tuomas Katva
 */
public class OrganisaatioBasicConverter {

    public static OrganisaatioPerustietoType convertOrganisaatioToBasicType(Organisaatio org) {
        OrganisaatioPerustietoType basic = new OrganisaatioPerustietoType();
        
        if (org.getParent() != null && org.getParent().getOid() != null) {
            basic.setParentOid(org.getParent().getOid());
        }
        basic.setOid(org.getOid());
        basic.setAlkuPvm(org.getAlkuPvm());
        basic.setLakkautusPvm(org.getLakkautusPvm());
        basic.setNimiFi(convertNimiFromEntity(org, "fi"));
        basic.setNimiEn(convertNimiFromEntity(org, "en"));
        basic.setNimiSv(convertNimiFromEntity(org, "sv"));
        basic.setYtunnus(org.getYtunnus());
        basic.setVirastoTunnus(org.getVirastoTunnus());
        basic.setOppilaitosKoodi(org.getOppilaitosKoodi());
        if (org.getTyypit() != null) {
            for (String tyyppi : org.getTyypit()) {
                basic.getTyypit().add(OrganisaatioTyyppi.fromValue(tyyppi));
            }
        }
        basic.setAliOrganisaatioMaara(org.getChildCount(null, new Date()));
        basic.setParentOidPath(org.getParentOidPath());
        return basic;
    }
    
    public static OrganisaatioPerustietoType convertOrganisaatioToBasicType(OrgPerustieto org) {
        OrganisaatioPerustietoType basic = new OrganisaatioPerustietoType();
        
        if (org.getParentOidPath() != null && !org.getParentOidPath().isEmpty()) {
        	String[] ancestors = org.getParentOidPath().split("\\|");
        	if (ancestors.length > 0) {
        		String parentOid = ancestors[ancestors.length - 1];
        		basic.setParentOid(parentOid);
        	}
        } 
        
        basic.setOid(org.getOid());
        basic.setAlkuPvm(org.getAlkuPvm());
        basic.setLakkautusPvm(org.getLakkautusPvm());
        basic.setNimiFi(convertNimiFromMonikieli(org.getNimi(), "fi"));
        basic.setNimiEn(convertNimiFromMonikieli(org.getNimi(), "en"));
        basic.setNimiSv(convertNimiFromMonikieli(org.getNimi(), "sv"));
        basic.setYtunnus(org.getYtunnus());
        basic.setOppilaitosKoodi(org.getOppilaitosKoodi());
        if (org.getTyypitStr() != null && !org.getTyypitStr().isEmpty()) {
        	for (String curTyyppi : org.getTyypitStr().split("\\|")) {
        		if (curTyyppi != null) {
        			basic.getTyypit().add(OrganisaatioTyyppi.fromValue(curTyyppi));
        		}
        	}
        }
        basic.setParentOidPath(org.getParentOidPath());
        return basic;
    }
    
    
    
    public static OrganisaatioDTO convertOrganisaatioToDTO(OrgPerustieto org) {
        OrganisaatioDTO dto = new OrganisaatioDTO();
        
        if (org.getParentOidPath() != null && !org.getParentOidPath().isEmpty()) {
            dto.setParentOid(org.getParentOidPath().split("\\|")[0]);
        }
        
        dto.setOid(org.getOid());
        dto.setAlkuPvm(org.getAlkuPvm());
        dto.setLakkautusPvm(org.getLakkautusPvm());
        dto.setNimi(convertMonikielinenFromEntity(org.getNimi()));
        dto.setYtunnus(org.getYtunnus());
        dto.setOppilaitosKoodi(org.getOppilaitosKoodi());
        if (org.getTyypitStr() != null && !org.getTyypitStr().isEmpty()) {
        	for (String curTyyppi : org.getTyypitStr().split("\\|")) {
        		if (curTyyppi != null) {
        			dto.getTyypit().add(OrganisaatioTyyppi.fromValue(curTyyppi));
        		}
        	}
        }
        dto.setParentOidPath(org.getParentOidPath());
        return dto;
    }
    

    private static String convertNimiFromEntity(Organisaatio org, String lang) {
    	if (org.getNimi() == null) {
    		return null;
    	}
    	for (Entry<String, String> e : org.getNimi().getValues().entrySet()) {
    		if (e.getKey().equals(lang)) {
    			return e.getValue();
    		}
    	}
    	return null;
    }
    
    private static MonikielinenTekstiTyyppi convertMonikielinenFromEntity(MonikielinenTeksti nimi) {
    	if (nimi == null) {
    		return null;
    	}
    	MonikielinenTekstiTyyppi dto = new MonikielinenTekstiTyyppi();
    	for (Entry<String, String> e : nimi.getValues().entrySet()) {
    		MonikielinenTekstiTyyppi.Teksti newTeksti = new MonikielinenTekstiTyyppi.Teksti();
    		newTeksti.setKieliKoodi(e.getKey());
    		newTeksti.setValue(e.getValue());
    		dto.getTeksti().add(newTeksti);
    		
    	}
    	return dto;
    }
    
    private static String convertNimiFromMonikieli(MonikielinenTeksti nimi, String lang) {
    	if (nimi == null) {
    		return null;
    	}	
    	for (Entry<String, String> e : nimi.getValues().entrySet()) {
    		if (e.getKey().equals(lang)) {
    			return e.getValue();
    		}
    	}
    	return null;
    }
    
    public static List<OrganisaatioPerustietoType> convertToPerustietos(List<Organisaatio> orgs) {
        List<OrganisaatioPerustietoType> op = new ArrayList<OrganisaatioPerustietoType>();
        for (Organisaatio org : orgs) {
            op.add(convertOrganisaatioToBasicType(org));
        }
        return op;
    }
    
    public static List<OrganisaatioPerustietoType> convertSmallToPerustietos(List<OrgPerustieto> orgs) {
        List<OrganisaatioPerustietoType> op = new ArrayList<OrganisaatioPerustietoType>();
        for (OrgPerustieto org : orgs) {
            op.add(convertOrganisaatioToBasicType(org));
        }
        return op;
    }
    
    public static List<OrganisaatioDTO> convertSmallToDTOs(List<OrgPerustieto> orgs) {
        List<OrganisaatioDTO> op = new ArrayList<OrganisaatioDTO>();
        for (OrgPerustieto org : orgs) {
            op.add(convertOrganisaatioToDTO(org));
        }
        return op;
    }
    
}
