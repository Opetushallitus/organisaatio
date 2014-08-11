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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@XmlRootElement
@ApiModel(value = "Organisaation suppeat perustiedot")
public class OrganisaatioPerustietoSuppea implements Serializable {

    private final static long serialVersionUID = 100L;

    @ApiModelProperty(value = "Organisaation oid", required = true)
    private String oid;

    @ApiModelProperty(value = "Organisaation nimi", required = true)
    private Map<String, String> nimi = new HashMap<String, String>();

    private List<OrganisaatioPerustietoSuppea> children = new ArrayList<OrganisaatioPerustietoSuppea>();

    public List<OrganisaatioPerustietoSuppea> getChildren() {
        return children;
    }

    public void setChildren(List<OrganisaatioPerustietoSuppea> children) {
        this.children = children;
    }

    public Map<String, String> getNimi() {
        return nimi;
    }

    public void setNimi(Map<String, String> nimi) {
        this.nimi = nimi;
    }

    public OrganisaatioPerustietoSuppea() {
        super();
    }

    /**
     * Gets the value of the oid property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getOid() {
        return oid;
    }

    /**
     * Sets the value of the oid property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setOid(String value) {
        this.oid = value;
    }

    /**
     * Aseta organisaation nimi
     * 
     * @param targetLanguage
     *            "fi","en" tai "sv"
     * @param nimi
     */
    public void setNimi(String targetLanguage, String nimi) {
        this.nimi.put(targetLanguage, nimi);
    }

    /**
     * Palauta organisaation nimi, tai null jos ko kielellä ei löydy.
     * 
     * @param language
     *            "fi","en" tai "sv"
     * @return
     */
    public String getNimi(String language) {
        return this.nimi.get(language);
    }

}
