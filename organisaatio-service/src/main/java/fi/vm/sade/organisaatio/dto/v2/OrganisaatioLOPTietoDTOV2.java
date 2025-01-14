/*
 * Copyright (c) 2014 The Finnish Board of Education - Opetushallitus
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

package fi.vm.sade.organisaatio.dto.v2;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.HashMap;
import java.util.Map;

@Schema(description = "Organisaation hakutulos suppea")
public class OrganisaatioLOPTietoDTOV2 {

    private String _oid;

    private Map<String, String> nimi = new HashMap<String, String>();

    private Map<String, Map<String, String>> _data = new HashMap<String, Map<String, String>>();

    @Schema(description = "Organisaation oid", required = true)
    public String getOid() {
        return _oid;
    }

    public void setOid(String _oid) {
        this._oid = _oid;
    }

    @Schema(description = "Organisaation nimi", required = true)
    public Map<String, String> getNimi() {
        return nimi;
    }

    public void setNimi(Map<String, String> nimi) {
        this.nimi = nimi;
    }

    @Schema(description = "Organisaation LOP tiedot", required = true)
    public Map<String, Map<String, String>> getData() {
        return _data;
    }

    public void setData(Map<String, Map<String, String>> _data) {
        this._data = _data;
    }

    public void addByKey(String key, Map<String, String> map) {
        _data.put(key, map);
    }

}
