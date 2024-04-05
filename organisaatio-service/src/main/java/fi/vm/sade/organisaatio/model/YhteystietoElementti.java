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

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author Antti Salonen
 */
@Entity
// uniqueConstraint koska tietyn niminen kenttä voi olla vain kerran yhteystietotyypillä
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"yhteystietojenTyyppi_id", "nimi"})})
public class YhteystietoElementti extends OrganisaatioBaseEntity {

	private static final long serialVersionUID = 1L;

    @ManyToOne(optional = false)
    @JoinColumn(name="yhteystietojenTyyppi_id")
    private YhteystietojenTyyppi yhteystietojenTyyppi;
    private boolean pakollinen;

    @NotNull
    @Size(min = 3, max = 100)
    private String nimi;  // TODO XSS filtteri
    private String nimiSv;  // TODO XSS filtteri
    private String nimiEn; // TODO XSS filtteri


    @NotNull
    private String oid;

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    @NotNull
    private String tyyppi;
    private boolean kaytossa = true;

    /*@NotNull
    private String kenttaOid;

    public String getKenttaOid() {
        return kenttaOid;
    }

    public void setKenttaOid(String kenttaOid) {
        this.kenttaOid = kenttaOid;
    }*/

    public String getNimi() {
        return nimi;
    }

    public void setNimi(String nimi) {
        this.nimi = nimi;
    }

    public String getTyyppi() {
        return tyyppi;
    }

    public void setTyyppi(String tyyppi) {
        this.tyyppi = tyyppi;
    }

    public boolean isPakollinen() {
        return pakollinen;
    }

    public void setPakollinen(boolean pakollinen) {
        this.pakollinen = pakollinen;
    }

    public YhteystietojenTyyppi getYhteystietojenTyyppi() {
        return yhteystietojenTyyppi;
    }

    public void setYhteystietojenTyyppi(YhteystietojenTyyppi yhteystietojenTyyppi) {
        this.yhteystietojenTyyppi = yhteystietojenTyyppi;
    }

    public boolean isKaytossa() {
        return kaytossa;
    }

    public void setKaytossa(boolean kaytossa) {
        this.kaytossa = kaytossa;
    }


    public String getNimiSv() {
        return nimiSv;
    }

    public void setNimiSv(String nimiSv) {
        this.nimiSv = nimiSv;
    }

    public String getNimiEn() {
        return nimiEn;
    }

    public void setNimiEn(String nimiEn) {
        this.nimiEn = nimiEn;
    }

}
