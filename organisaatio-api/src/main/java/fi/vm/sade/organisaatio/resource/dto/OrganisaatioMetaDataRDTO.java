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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author mlyly
 */
public class OrganisaatioMetaDataRDTO implements Serializable {

    private Map<String, Map<String, String>> _data = new HashMap<String, Map<String, String>>();
    private Map<String, String> _hakutoimistonNimi;
    private Map<String, String> _nimi;
    private String _hakutoimistoEctsEmail;
    private String _hakutoimistoEctsNimi;
    private String _hakutoimistoEctsPuhelin;
    private String _hakutoimistoEctsTehtavanimike;
    private String _koodi;
    private String _kuvaEncoded;
    private Date _luontiPvm;
    private Date _muokkausPvm;
    private List<Map<String, String>> _yhteystiedot;

    public void addByKey(String key, Map<String, String> map) {
        _data.put(key, map);
    }

    public Map<String, Map<String, String>> getData() {
        return _data;
    }

    public void setData(Map<String, Map<String, String>> _data) {
        this._data = _data;
    }

    public String getHakutoimistoEctsEmail() {
        return _hakutoimistoEctsEmail;
    }

    public void setHakutoimistoEctsEmail(String _hakutoimistoEctsEmail) {
        this._hakutoimistoEctsEmail = _hakutoimistoEctsEmail;
    }

    public String getHakutoimistoEctsNimi() {
        return _hakutoimistoEctsNimi;
    }

    public void setHakutoimistoEctsNimi(String _hakutoimistoEctsNimi) {
        this._hakutoimistoEctsNimi = _hakutoimistoEctsNimi;
    }

    public String getHakutoimistoEctsPuhelin() {
        return _hakutoimistoEctsPuhelin;
    }

    public void setHakutoimistoEctsPuhelin(String _hakutoimistoEctsPuhelin) {
        this._hakutoimistoEctsPuhelin = _hakutoimistoEctsPuhelin;
    }

    public String getHakutoimistoEctsTehtavanimike() {
        return _hakutoimistoEctsTehtavanimike;
    }

    public void setHakutoimistoEctsTehtavanimike(String _hakutoimistoEctsTehtavanimike) {
        this._hakutoimistoEctsTehtavanimike = _hakutoimistoEctsTehtavanimike;
    }

    public Map<String, String> getHakutoimistonNimi() {
        return _hakutoimistonNimi;
    }

    public void setHakutoimistonNimi(Map<String, String> _hakutoimistonNimi) {
        this._hakutoimistonNimi = _hakutoimistonNimi;
    }

    public String getKoodi() {
        return _koodi;
    }

    public void setKoodi(String _koodi) {
        this._koodi = _koodi;
    }

    public String getKuvaEncoded() {
        return _kuvaEncoded;
    }

    public void setKuvaEncoded(String _kuvaEncoded) {
        this._kuvaEncoded = _kuvaEncoded;
    }

    public Date getLuontiPvm() {
        return _luontiPvm;
    }

    public void setLuontiPvm(Date _luontiPvm) {
        this._luontiPvm = _luontiPvm;
    }

    public Date getMuokkausPvm() {
        return _muokkausPvm;
    }

    public void setMuokkausPvm(Date _muokkausPvm) {
        this._muokkausPvm = _muokkausPvm;
    }

    public Map<String, String> getNimi() {
        return _nimi;
    }

    public void setNimi(Map<String, String> _nimi) {
        this._nimi = _nimi;
    }

    public List<Map<String, String>> getYhteystiedot() {
        if (_yhteystiedot == null) {
            _yhteystiedot = new ArrayList<Map<String, String>>();
        }
        return _yhteystiedot;
    }

    public void setYhteystiedot(List<Map<String, String>> _yhteystiedot) {
        this._yhteystiedot = _yhteystiedot;
    }
}
