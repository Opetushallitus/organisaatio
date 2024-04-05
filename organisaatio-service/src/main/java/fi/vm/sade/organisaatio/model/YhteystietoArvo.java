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

import fi.vm.sade.organisaatio.api.model.types.YhteystietoElementtiTyyppi;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

import static fi.vm.sade.organisaatio.ValidationConstants.GENERIC_MAX;
import static fi.vm.sade.organisaatio.ValidationConstants.GENERIC_MIN;

/**
 * @author Antti Salonen
 * @see fi.vm.sade.organisaatio.api.model.types.YhteystietoArvoDTO
 */
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"kentta_id", "organisaatio_id", "kieli"})})
public class YhteystietoArvo extends OrganisaatioBaseEntity {
    public static final String KRIISIVIESTINNAN_SAHKOPOSTIOSOITE_TYYPPI_OID = "1.2.246.562.5.31532764098";

    private static final long serialVersionUID = 1L;

    @ManyToOne(optional = false)
    private YhteystietoElementti kentta;
    @ManyToOne(optional = false)
    private Organisaatio organisaatio;
    @OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
    private Yhteystieto arvoYhteystieto;
    @Size(min = GENERIC_MIN, max = GENERIC_MAX)
    private String arvoText; // TODO XSS filtteri
    private String kieli;  // TODO XSS filtteri
 
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
