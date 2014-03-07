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

package fi.vm.sade.organisaatio.dao.impl;

import java.util.Collections;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import fi.vm.sade.generic.dao.AbstractJpaDAOImpl;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.YhteystietoArvo;
import fi.vm.sade.organisaatio.model.YhteystietojenTyyppi;

/**
 * @author Antti Salonen
 */
@Repository
public class YhteystietoArvoDAOImpl extends AbstractJpaDAOImpl<YhteystietoArvo, Long> {
    
    @Autowired
    private OrganisaatioDAOImpl organisaatioDAO;
    
    @SuppressWarnings("unchecked")
	public List<YhteystietoArvo> findByOrganisaatio(Organisaatio org) {
    	if (org.getId()==null) {
    		return Collections.emptyList();
    	}
    	return getEntityManager().createQuery("FROM "+YhteystietoArvo.class.getName()+" WHERE organisaatio=?")
    		.setParameter(1, org)
    		.getResultList();
    }
    
    public YhteystietoArvo findByOrganisaatioAndNimi(String organisaatioOid, String nimi) {
        
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<YhteystietoArvo> query = cb.createQuery(YhteystietoArvo.class);
        
        Root<YhteystietoArvo> root = query.from(YhteystietoArvo.class);
        query.select(root);
        
        Predicate organisaatioEquals = cb.equal(root.get("organisaatio").get("oid"), organisaatioOid);
        Predicate nameEquals = cb.equal(root.get("kentta").get("nimi"), nimi);
        
        Predicate whereClause = cb.and(organisaatioEquals, nameEquals);
        query.where(whereClause);
        
        return getEntityManager().createQuery(query).getSingleResult();
        
//        Organisaatio org = organisaatioDAO.findBy("oid", organisaatioOid).get(0);
//        Query query = getEntityManager().createQuery("SELECT x FROM YhteystietoArvo x " +
//                "WHERE x.kentta.nimi = :nimi AND x.organisaatio.id = :organisaatioId");
//        query.setParameter("nimi", nimi);
//        query.setParameter("organisaatioId", org.getId());
//        return (YhteystietoArvo) query.getSingleResult();
    }

    /**
     * Returns yhteystietoarvos for a given yhteystietojen tyyppi
     * @param yhteystietojenTyyppi the yhteystietojen tyyppi given
     * @return the yhteystietoarvo objects matching the given yhteystietojen tyyppi
     */
	public List<YhteystietoArvo> findByYhteystietojenTyyppi(
			YhteystietojenTyyppi yhteystietojenTyyppi) {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<YhteystietoArvo> query = cb.createQuery(YhteystietoArvo.class);
        
        Root<YhteystietoArvo> root = query.from(YhteystietoArvo.class);
        query.select(root);
        
        Predicate yhteystietojenTyyppiEquals = cb.equal(root.get("kentta").get("yhteystietojenTyyppi").get("oid"), yhteystietojenTyyppi.getOid());
		query.where(yhteystietojenTyyppiEquals);
		
		return getEntityManager().createQuery(query).getResultList();
	}

}
