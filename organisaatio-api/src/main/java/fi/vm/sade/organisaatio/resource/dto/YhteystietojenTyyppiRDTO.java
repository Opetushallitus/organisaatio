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

package fi.vm.sade.organisaatio.resource.dto;

import com.wordnik.swagger.annotations.ApiModel;
import java.util.List;
import java.util.Map;

/**
 *
 */
@ApiModel(value = "Yhteystiedon tyyppi")
public class YhteystietojenTyyppiRDTO {

    private Map<String, String> nimi;
    private List<String> sovellettavatOrganisaatioTyyppis;
    private List<String> sovellettavatOppilaitosTyyppis;
    private List<Map<String, String>> lisatietos;

    public Map<String, String> getNimi() {
        return nimi;
    }

    public void setNimi(Map<String, String> nimi) {
        this.nimi = nimi;
    }

    public List<String> getSovellettavatOrganisaatioTyyppis() {
        return sovellettavatOrganisaatioTyyppis;
    }

    public void setSovellettavatOrganisaatioTyyppis(List<String> sovellettavatOrganisaatioTyyppis) {
        this.sovellettavatOrganisaatioTyyppis = sovellettavatOrganisaatioTyyppis;
    }

    public List<String> getSovellettavatOppilaitosTyyppis() {
        return sovellettavatOppilaitosTyyppis;
    }

    public void setSovellettavatOppilaitosTyyppis(List<String> sovellettavatOppilaitosTyyppis) {
        this.sovellettavatOppilaitosTyyppis = sovellettavatOppilaitosTyyppis;
    }

    public List<Map<String, String>> getLisatietos() {
        return lisatietos;
    }

    public void setLisatietos(List<Map<String, String>> lisatietos) {
        this.lisatietos = lisatietos;
    }


}
