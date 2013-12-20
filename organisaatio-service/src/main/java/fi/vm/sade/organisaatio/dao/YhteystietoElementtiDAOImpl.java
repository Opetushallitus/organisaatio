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

package fi.vm.sade.organisaatio.dao;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import fi.vm.sade.generic.dao.AbstractJpaDAOImpl;
import fi.vm.sade.organisaatio.model.YhteystietoElementti;

/**
 * @author Antti Salonen
 * @author mlyly
 */
@Repository
public class YhteystietoElementtiDAOImpl extends AbstractJpaDAOImpl<YhteystietoElementti, Long> {
    
    @Autowired
    private YhteystietojenTyyppiDAOImpl yttDao;

    public List<YhteystietoElementti> findAllKaytossa() {
//        Query query = getEntityManager().createQuery("SELECT x FROM YhteystietoElementti x where x.kaytossa = true");
//        return query.getResultList();
        
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<YhteystietoElementti> query = cb.createQuery(YhteystietoElementti.class);
        
        Root<YhteystietoElementti> root = query.from(YhteystietoElementti.class);
        query.select(root);
        
        Predicate whereClause = cb.equal(root.get("kaytossa"), true);
        query.where(whereClause);

        return getEntityManager().createQuery(query).getResultList();
    }

    public List<YhteystietoElementti> findByLisatietoIdAndKentanNimi(String yhteystietojenTyyppiOid, String kentanNimi) {
        
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<YhteystietoElementti> query = cb.createQuery(YhteystietoElementti.class);
        
        Root<YhteystietoElementti> root = query.from(YhteystietoElementti.class);
        query.select(root);
        
        Predicate tyyppiEquals = cb.equal(root.get("yhteystietojenTyyppi").get("oid"), yhteystietojenTyyppiOid);
        Predicate nameEquals = cb.equal(root.get("nimi"), kentanNimi);
        
        Predicate whereClause = cb.and(tyyppiEquals, nameEquals);
        query.where(whereClause);
        
        return getEntityManager().createQuery(query).getResultList();
        
//        YhteystietojenTyyppi ytT =  yttDao.findBy("oid", yhteystietojenTyyppiOid).get(0);
//        Query query = getEntityManager().createQuery(
//                "SELECT ltk FROM YhteystietoElementti ltk " +
//                "WHERE ltk.yhteystietojenTyyppi.id = :yhteystietojenTyyppiId " +
//                "AND ltk.nimi = :kentanNimi"
//                );
//        return query
//                .setParameter("yhteystietojenTyyppiId", ytT.getId())
//                .setParameter("kentanNimi", kentanNimi)
//                .getResultList();
    }
}
