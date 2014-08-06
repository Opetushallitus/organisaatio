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
package fi.vm.sade.organisaatio.resource.dto;

import java.io.Serializable;

/**
 * Result wrapper for organization CU operations
 * 
 * @author markus
 */
public class ResultRDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    
    public enum ResultStatus {
        OK,
        INFO,
        WARNING,
        VALIDATION,
        ERROR
    };
    
    private ResultStatus status = ResultStatus.OK;

    private String info;

    private OrganisaatioRDTO organisaatio;
    
    public ResultRDTO(OrganisaatioRDTO organisaatio) {
        this.organisaatio = organisaatio;
    }
    
    public ResultRDTO(OrganisaatioRDTO organisaatio, ResultStatus status, String info) {
        this.organisaatio = organisaatio;
        this.status = status;
        this.info = info;
    }
    
    public ResultStatus getStatus() {
        return status;
    }

    public void setStatus(ResultStatus status) {
        this.status = status;
    }
    
    public OrganisaatioRDTO getOrganisaatio() {
        return organisaatio;
    }

    public void setOrganisaatio(OrganisaatioRDTO organisaatio) {
        this.organisaatio = organisaatio;
    }
    
    public String getInfo() {
        return info;
    }
    
    public void setInfo(String info) {
        this.info = info;
    }

}
