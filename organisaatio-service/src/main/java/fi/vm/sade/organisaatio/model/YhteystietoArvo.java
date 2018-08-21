/*
 *
 * Copyright (c) 2012 The Finnish Board of Education - Opetushallitus
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
 * European Union Public Licence for more details.
 */

package fi.vm.sade.organisaatio.model;

import static fi.vm.sade.generic.common.validation.ValidationConstants.GENERIC_MAX;
import static fi.vm.sade.generic.common.validation.ValidationConstants.GENERIC_MIN;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Cascade;

import fi.vm.sade.organisaatio.api.model.types.YhteystietoElementtiTyyppi;
import fi.vm.sade.security.xssfilter.FilterXss;
import fi.vm.sade.security.xssfilter.XssFilterListener;

/**
 * @author Antti Salonen
 * @see fi.vm.sade.organisaatio.api.model.types.YhteystietoArvoDTO
 */
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"kentta_id", "organisaatio_id", "kieli"})})
@EntityListeners(XssFilterListener.class)
public class YhteystietoArvo extends OrganisaatioBaseEntity {

	private static final long serialVersionUID = 1L;

    @ManyToOne(optional = false)
    private YhteystietoElementti kentta;
    @ManyToOne(optional = false)
    private Organisaatio organisaatio;
    @OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
    private Yhteystieto arvoYhteystieto;
    @Size(min = GENERIC_MIN, max = GENERIC_MAX)
    @FilterXss
    private String arvoText;
    @FilterXss
    private String kieli;
 
    @NotNull
    private String yhteystietoArvoOid;

    public YhteystietoArvo() {
    }

    public YhteystietoArvo(String arvoText) {
        this.arvoText = arvoText;
    }

    public YhteystietoElementti getKentta() {
        return kentta;
    }

    public void setKentta(YhteystietoElementti kentta) {
        this.kentta = kentta;
    }

    public Organisaatio getOrganisaatio() {
        return organisaatio;
    }

    public void setOrganisaatio(Organisaatio organisaatio) {
        this.organisaatio = organisaatio;
    }

    public Yhteystieto getArvoYhteystieto() {
        return arvoYhteystieto;
    }

    public void setArvoYhteystieto(Yhteystieto arvoYhteystieto) {
        this.arvoYhteystieto = arvoYhteystieto;
    }

    public String getArvoText() {
        return arvoText;
    }

    public void setArvoText(String arvoText) {
        this.arvoText = arvoText;
    }

    public Serializable getArvo() {
        if (kentta.getTyyppi().equals(YhteystietoElementtiTyyppi.TEKSTI.value())) {
            return arvoText;
        } else if (kentta.getTyyppi().equals(YhteystietoElementtiTyyppi.EMAIL.value())
                || kentta.getTyyppi().equals(YhteystietoElementtiTyyppi.WWW.value())
                || kentta.getTyyppi().equals(YhteystietoElementtiTyyppi.PUHELIN.value())
                || kentta.getTyyppi().equals(YhteystietoElementtiTyyppi.OSOITE.value())
                || kentta.getTyyppi().equals(YhteystietoElementtiTyyppi.OSOITE_ULKOMAA.value())) {
            return arvoYhteystieto;
        } else if (kentta.getTyyppi().equals(YhteystietoElementtiTyyppi.NIMI.value())
                    || kentta.getTyyppi().equals(YhteystietoElementtiTyyppi.NIMIKE.value())) {
            return arvoText;
        } else {
            throw new IllegalArgumentException("cannot get arvo, illegal target class: "+kentta.getTyyppi());
        }
        /*if (YhteystietoDTO.class.isAssignableFrom(kentta.getTyyppi().getTargetClass())) {
            return arvoYhteystieto;
        } else if (String.class.isAssignableFrom(kentta.getTyyppi().getTargetClass())) {
            return arvoText;
        } else {
            throw new IllegalArgumentException("cannot get arvo, illegal target class: "+kentta.getTyyppi().getTargetClass());
        }*/
    }

    public String getYhteystietoArvoOid() {
        return yhteystietoArvoOid;
    }

    public void setYhteystietoArvoOid(String yhteystietoArvoOid) {
        this.yhteystietoArvoOid = yhteystietoArvoOid;
    }

    public String getKieli() {
        return kieli;
    }

    public void setKieli(String kieli) {
        this.kieli = kieli;
    }

}
