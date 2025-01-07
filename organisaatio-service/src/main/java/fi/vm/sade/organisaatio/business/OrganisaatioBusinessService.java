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

package fi.vm.sade.organisaatio.business;

import fi.vm.sade.organisaatio.dto.OrganisaatioNimiDTO;
import fi.vm.sade.organisaatio.dto.OrganisaatioNimiUpdateDTO;
import fi.vm.sade.organisaatio.dto.v2.OrganisaatioMuokkausTiedotDTO;
import fi.vm.sade.organisaatio.dto.v4.OrganisaatioRDTOV4;
import fi.vm.sade.organisaatio.dto.v4.ResultRDTOV4;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.OrganisaatioNimi;
import fi.vm.sade.organisaatio.model.OrganisaatioResult;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;

import jakarta.validation.ValidationException;
import java.sql.Timestamp;
import java.util.*;

public interface OrganisaatioBusinessService {

    /**
     * @param model
     * @return
     * @throws ValidationException
     */
    public OrganisaatioResult saveOrUpdate(OrganisaatioRDTO model) throws ValidationException;


    /**
     * Organisaatio api v4 wrapperi organisaation luomiseen ja tallennukseen.
     * @param model Organisaatio v4 rajapinnan mukainen dto organisaatiosta
     * @return Organisaatio v4 rajapinnan tulos dto
     * @throws ValidationException Validointivirhe jos organisaation tiedot ovat virheellisiä
     */
    public ResultRDTOV4 saveOrUpdate(OrganisaatioRDTOV4 model) throws ValidationException;

    public OrganisaatioResult saveDatantuontiOrganisaatio(OrganisaatioRDTOV4 model);

    /**
     * @param oid
     * @return
     */
    public List<OrganisaatioNimi> getOrganisaatioNimet(String oid);

    /**
     * @param oid
     * @param nimidto
     * @return
     */
    public OrganisaatioNimi newOrganisaatioNimi(String oid, OrganisaatioNimiDTO nimidto);

    /**
     * @param oid
     * @param nimidto
     * @return
     */
    public OrganisaatioNimi updateOrganisaatioNimi(String oid, OrganisaatioNimiUpdateDTO nimidto);

    /**
     * @param oid
     * @param nimi
     */
    public void deleteOrganisaatioNimi(String oid, OrganisaatioNimiDTO nimi);

    /**
     * Päivitetään organisaatioiden nimet niiltä organisaatioilta, joilla ajastettu nimenmuutos.
     */
    public void updateCurrentOrganisaatioNimet();


    /**
     * Checks all new organisation relations and updates necessarry changes to tree hierarchy.
     *
     * @return List of changed organisations.
     */
    public Set<Organisaatio> processNewOrganisaatioSuhdeChanges();

    /**
     * Siirtää organisaatiota puussa toisen parentin alle
     * @param organisaatio Siirrettävä organisaatio
     * @param newParent Kohde organisaatio
     * @param date Siirto pvm
     */
    public void changeOrganisaatioParent(Organisaatio organisaatio, Organisaatio newParent, Date date);

    /**
     * Yhdistää kaksi organisaatiota
     * @param self Siirrettävän organisaatio
     * @param newParent Kohde organisaatio
     * @param date Siirto pvm
     */
    public void mergeOrganisaatio(Organisaatio self, Organisaatio newParent, Date date);
    public void mergeOrganisaatio(String self, String newParent, Optional<Date> date, boolean merge);

    /**
     *
     * @param givenData Pvm data wanted to be changed
     * @param organisaatioMap The organisations that the data is used to update
     */
    void batchValidatePvm(HashMap<String, OrganisaatioMuokkausTiedotDTO> givenData, HashMap<String, Organisaatio> organisaatioMap);


    /*
     * @Param oldParentNimiMap language-name mappings for the old parent name
     * @Param currentNimiMap language-name mappings for the curremt child name
     * @Param newParentNimiMap language-name mappings for the new parent name
     */
    void updateNimiValues(Map<String, String> oldParentNimiMap, Map<String, String> currentNimiMap, Map<String, String> newParentNimiMap);

    /**
     * Update the tarkastusPvm of organisaatio
     * @param oid Oid of organisaatio that has been checked
     * @return Timestamp when organisaatio was checked
     */
    Timestamp updateTarkastusPvm(String oid);
}
