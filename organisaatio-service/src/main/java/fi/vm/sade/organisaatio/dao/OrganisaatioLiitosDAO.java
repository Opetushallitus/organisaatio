/*
* Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
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
*/

package fi.vm.sade.organisaatio.dao;

import fi.vm.sade.generic.dao.JpaDAO;
import fi.vm.sade.organisaatio.model.OrganisaatioLiitos;

import java.util.Date;
import java.util.List;

/**
 * @author simok
 */
public interface OrganisaatioLiitosDAO extends JpaDAO<OrganisaatioLiitos, Long>  {

    /**
     * Lisätään organisaatioliitos.
     *
     * @param organisaatioId Yhdistyvä organisaatio
     * @param kohdeId Organisaatio, johon yhdistytään
     * @param startingFrom null == now
     * @return Luotu liitos
     */
    OrganisaatioLiitos addLiitos(Long organisaatioId, Long kohdeId, Date startingFrom);

    /**
     * Etsitään annettuun organisaation liittyneet organisaatiot (liitoksina).
     *
     * @param kohdeId Organisaatio, johon yhdistyneitä organisaatioita haetaan.
     * @return Annettuun kohteeseen tehdyt liitokset.
     */
    List<OrganisaatioLiitos> findLiitokset(Long kohdeId);

    /**
     * Etsitään mihin organisaatioihin (todellisuudessa luultavasti 0 / 1),
     * annettu organisaatio on liittynyt.
     *
     * @param organisaatioId Organisaatio, jonka liittymisiä toiseen / toisiin haetaan.
     * @return Liitokset, joissa annettu organisaatio ollut liittyvänä.
     */
    List<OrganisaatioLiitos> findLiittynyt(Long organisaatioId);

}
