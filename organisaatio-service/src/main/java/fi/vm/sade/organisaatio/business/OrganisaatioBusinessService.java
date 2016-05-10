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
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.OrganisaatioNimi;
import fi.vm.sade.organisaatio.model.OrganisaatioResult;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;

import javax.validation.ValidationException;
import java.util.Date;
import java.util.List;

/**
 * @author simok
 */
public interface OrganisaatioBusinessService {

    /**
     * @param model
     * @param updating
     * @param skipParentDateValidation
     * @return
     * @throws ValidationException
     */
    public OrganisaatioResult save(OrganisaatioRDTO model, boolean updating, boolean skipParentDateValidation) throws ValidationException;
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
     */
    public void updateCurrentOrganisaatioNimet();

    /**
     * @param tiedot
     * @return
     */
    public OrganisaatioMuokkausTulosListaDTO bulkUpdatePvm(List<OrganisaatioMuokkausTiedotDTO> tiedot);

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
     */
    public void changeOrganisaatioParent(Organisaatio organisaatio, Organisaatio newParent, Date date);

    /**
     * Yhdistää kaksi organisaatiota
     * @param self Siirrettävän organisaatio
     * @param newParent Kohde organisaatio
     * @param date Siirto pvm
     */
    public void mergeOrganisaatio(Organisaatio self, Organisaatio newParent, Date date);

    /*
    * Päivittää datan YTJ:stä koulutustoimijoille, työelämäjärjestöille ja muu organisaatioille
    * */
    public int updateYTJData(final boolean forceUpdate);

}
