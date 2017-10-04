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

import fi.vm.sade.organisaatio.dto.v2.OrganisaatioMuokkausTiedotDTO;
import fi.vm.sade.organisaatio.dto.v2.OrganisaatioMuokkausTulosListaDTO;
import fi.vm.sade.organisaatio.dto.v2.OrganisaatioNimiDTOV2;
import fi.vm.sade.organisaatio.dto.v3.OrganisaatioRDTOV3;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.OrganisaatioNimi;
import fi.vm.sade.organisaatio.model.OrganisaatioResult;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;

import javax.validation.ValidationException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author simok
 */
public interface OrganisaatioBusinessService {

    /**
     * @param model
     * @param updating
     * @param csrfCookie
     * @return
     * @throws ValidationException
     */
    public OrganisaatioResult save(OrganisaatioRDTO model, boolean updating, final String csrfCookie) throws ValidationException;

    /**
     * @param model
     * @param updating
     * @param csrfCookie
     * @return
     * @throws ValidationException
     */
    public OrganisaatioResult save(OrganisaatioRDTOV3 model, boolean updating, final String csrfCookie) throws ValidationException;

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
    public OrganisaatioNimi newOrganisaatioNimi(String oid, OrganisaatioNimiDTOV2 nimidto);

    /**
     * @param oid
     * @param date
     * @param nimidto
     * @return
     */
    public OrganisaatioNimi updateOrganisaatioNimi(String oid, Date date, OrganisaatioNimiDTOV2 nimidto);

    /**
     * @param oid
     * @param date
     */
    public void deleteOrganisaatioNimi(String oid, Date date);

    /**
     * Päivitetään organisaatioiden nimet niiltä organisaatioilta, joilla ajastettu nimenmuutos.
     * @param csrfCookie CSRF-keksin arvo
     */
    public void updateCurrentOrganisaatioNimet(final String csrfCookie);

    /**
     * @param tiedot
     * @param csrfCookie
     * @return
     */
    public OrganisaatioMuokkausTulosListaDTO bulkUpdatePvm(List<OrganisaatioMuokkausTiedotDTO> tiedot, final String csrfCookie);

    /**
     * Checks all new organisation relations and updates necessarry changes to tree hierarchy.
     *
     * @return List of changed organisations.
     */
    public List<Organisaatio> processNewOrganisaatioSuhdeChanges();

    /**
     * Siirtää organisaatiota puussa toisen parentin alle
     * @param organisaatio Siirrettävä organisaatio
     * @param newParent Kohde organisaatio
     * @param date Siirto pvm
     * @param csrfCookie CSRF-keksin arvo
     */
    public void changeOrganisaatioParent(Organisaatio organisaatio, Organisaatio newParent, Date date, final String csrfCookie);

    /**
     * Yhdistää kaksi organisaatiota
     * @param self Siirrettävän organisaatio
     * @param newParent Kohde organisaatio
     * @param date Siirto pvm
     * @param csrfCookie
     */
    public void mergeOrganisaatio(Organisaatio self, Organisaatio newParent, Date date, final String csrfCookie);

    /**
     *
     * @param givenData Pvm data wanted to be changed
     * @param organisaatioMap The organisations that the data is used to update
     */
    void batchValidatePvm(HashMap<String, OrganisaatioMuokkausTiedotDTO> givenData, HashMap<String, Organisaatio> organisaatioMap);
}
