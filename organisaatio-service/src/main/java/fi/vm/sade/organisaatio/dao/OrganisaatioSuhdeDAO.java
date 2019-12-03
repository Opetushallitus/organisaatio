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
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.OrganisaatioSuhde;

import java.util.Date;
import java.util.List;

/**
 * @author simok
 */
public interface OrganisaatioSuhdeDAO extends JpaDAO<OrganisaatioSuhde, Long>  {

    /**
     * If child has a "current" parent, this actually "moves" child under another parent.
     *
     * @param parentId
     * @param childId
     * @param startingFrom null == now
     * @param opetuspisteenJarjNro
     * @return created relation
     */
    OrganisaatioSuhde addChild(Long parentId, Long childId, Date startingFrom, String opetuspisteenJarjNro);

    /**
     * Lisätään organisaatioliitos.
     *
     * @param organisaatio Yhdistyvä organisaatio
     * @param kohde Organisaatio, johon yhdistytään
     * @param startingFrom null == now
     * @return Luotu liitos
     */
    OrganisaatioSuhde addLiitos(Organisaatio organisaatio, Organisaatio kohde, Date startingFrom);

    /**
     * @param parentId
     * @param atTime   null == now
     * @return list of child relations at given time
     */
    List<OrganisaatioSuhde> findChildrenTo(Long parentId, Date atTime);

    // USE CASES:
    //
    // TIME 1. New child, B for A
    // data:
    // A B 1 - *
    //
    // TIME 2. New child, C for A
    // data:
    // A B 1 -
    // A C 2 - *
    //
    // TIME 3. New child, E for D
    // data:
    // A B 1 -
    // A C 2 -
    // D E 3 - *
    //
    // TIME 4. Remove child E
    // data:
    // A B 1 -
    // A C 2 -
    // D E 3 4 *
    //
    // TIME 5. Move B to be child of C  (remove child B, add child B to C)
    // data:
    // A B 1 5 *
    // A C 2 -
    // D E 3 4
    // C B 5 - *
    //

    /**
     * @param childId
     * @param atTime  null == now
     * @return parent relation at given time - there can be only one.
     */
    OrganisaatioSuhde findParentTo(Long childId, Date atTime);

    /**
     * Updates existing parent-child relation for give parent-child.
     * If parent is null ANY valid relation for child will be dated to be ended.
     *
     * @param parentId
     * @param childId
     * @param removalDate
     */
    void removeChild(Long parentId, Long childId, Date removalDate);

    /**
     * Finds all organisation relation changes that matches given day.
     * Ordering is ascending by starting date.
     * Time part of the given date is ignored.
     *
     * @param day Given date.
     * @return list of {@link fi.vm.sade.organisaatio.model.OrganisaatioSuhde} enties
     */
    List<OrganisaatioSuhde> findForDay(Date day);

    public List<OrganisaatioSuhde> findLiitokset(Boolean piilotettu, Date date);

}
