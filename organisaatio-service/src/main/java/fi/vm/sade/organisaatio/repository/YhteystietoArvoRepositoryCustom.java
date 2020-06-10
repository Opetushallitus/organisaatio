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

package fi.vm.sade.organisaatio.repository;

import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.YhteystietoArvo;
import fi.vm.sade.organisaatio.model.YhteystietojenTyyppi;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 *
 * @author simok
 */
public interface YhteystietoArvoRepositoryCustom {

    @SuppressWarnings(value = "unchecked")
    List<YhteystietoArvo> findByOrganisaatio(Organisaatio org);

    YhteystietoArvo findByOrganisaatioAndNimi(String organisaatioOid, String nimi);

    /**
     * Returns yhteystietoarvos for a given yhteystietojen tyyppi
     * @param yhteystietojenTyyppi the yhteystietojen tyyppi given
     * @return the yhteystietoarvo objects matching the given yhteystietojen tyyppi
     */
    List<YhteystietoArvo> findByYhteystietojenTyyppi(YhteystietojenTyyppi yhteystietojenTyyppi);
    
}
