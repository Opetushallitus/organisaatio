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
package fi.vm.sade.organisaatio.repository.impl;

import fi.vm.sade.organisaatio.model.YhteystietojenTyyppi;
import fi.vm.sade.organisaatio.repository.YhteystietojenTyyppiRepositoryCustom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Repository
public class YhteystietojenTyyppiRepositoryImpl implements YhteystietojenTyyppiRepositoryCustom {

    @Autowired
    EntityManager em;


    private static final String QUERY1 =
            "SELECT distinct ol FROM YhteystietojenTyyppi ol " + 
            "join fetch ol.sovellettavatOrganisaatioTyyppis as so WHERE so in (:organisaatioTyyppis)";
    private static final String QUERY2 =
            "SELECT distinct ol FROM YhteystietojenTyyppi ol " + 
            "join fetch ol.sovellettavatOppilaitostyyppis as so WHERE so in (:organisaatioTyyppis)";
    
    @Override
    public List<YhteystietojenTyyppi> findLisatietoMetadataForOrganisaatio(Collection<String> organisaatioTyyppis) {
        
        Query query = em.createQuery(QUERY1);
        query.setParameter("organisaatioTyyppis", organisaatioTyyppis);
        List<YhteystietojenTyyppi> matches = query.getResultList();
        query = em.createQuery(QUERY2);
        query.setParameter("organisaatioTyyppis", organisaatioTyyppis);
        matches.addAll(union(matches, query.getResultList()));
        return matches;//query.getResultList();
    }
    
    private List<YhteystietojenTyyppi> union(List<YhteystietojenTyyppi> list1, List<YhteystietojenTyyppi> list2) {
        List<YhteystietojenTyyppi> additions = new ArrayList<YhteystietojenTyyppi>();
        for (YhteystietojenTyyppi curYt : list2) {
            if (!contained(curYt, list1)) {
                additions.add(curYt);
            }
        }
        return additions;
    }
    
    private boolean contained(YhteystietojenTyyppi yt, List<YhteystietojenTyyppi> ytlist) {
        for (YhteystietojenTyyppi curYt : ytlist) {
            if (curYt.getOid().equals(yt.getOid())) {
                return true;
            }
        }
        return false;
    }

}
