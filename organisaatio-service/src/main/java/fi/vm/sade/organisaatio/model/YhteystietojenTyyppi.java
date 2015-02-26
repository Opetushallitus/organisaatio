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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

import fi.vm.sade.security.xssfilter.XssFilterListener;

/**
 * @author Antti Salonen
 */
@Entity
@EntityListeners(XssFilterListener.class)
public class YhteystietojenTyyppi extends OrganisaatioBaseEntity {

	private static final long serialVersionUID = 1L;

	@OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "nimi_mkt")
    private MonikielinenTeksti nimi;


    @ElementCollection
    @Column(name = "organisaatio_tyyppi")
    @CollectionTable(name = "yhteystietojenTyyppi_organisaatioTyypit", joinColumns = @JoinColumn(name = "yhteystietojenTyyppi_id"))
    private List<String> sovellettavatOrganisaatioTyyppis = new ArrayList<String>();

    @ElementCollection
    @Column(name = "oppilaitos_tyyppi")
    @CollectionTable(name = "yhteystietojenTyyppi_oppilaitosTyypit", joinColumns = @JoinColumn(name = "yhteystietojenTyyppi_id"))
    private List<String> sovellettavatOppilaitostyyppis = new ArrayList<String>();

    @OneToMany(mappedBy = "yhteystietojenTyyppi", cascade = CascadeType.ALL, orphanRemoval=true)
    // TODO remove @Cascade(value = org.hibernate.annotations.CascadeType.DELETE_ORPHAN) // TODO: kun JPA2: @OneToMany(..., orphanRemoval=true)
    private List<YhteystietoElementti> lisatietos = new ArrayList<YhteystietoElementti>();

    @NotNull
    private String oid;

    public MonikielinenTeksti getNimi() {
        return nimi;
    }

    public void setNimi(MonikielinenTeksti nimi) {
        this.nimi = nimi;
    }

    public List<String> getSovellettavatOrganisaatioTyyppis() {
        return Collections.unmodifiableList(sovellettavatOrganisaatioTyyppis);
    }

    public void setSovellettavatOrganisaatioTyyppis(List<String> sovellettavatOrganisaatioTyyppis) {
        this.sovellettavatOrganisaatioTyyppis.clear();
        this.sovellettavatOrganisaatioTyyppis.addAll(sovellettavatOrganisaatioTyyppis);
    }

    public List<String> getSovellettavatOppilaitostyyppis() {
        return this.sovellettavatOppilaitostyyppis;
    }

    public void setSovellettavatOppilaitostyyppis(
            List<String> sovellettavatOppilaitostyyppis) {
        this.sovellettavatOppilaitostyyppis = sovellettavatOppilaitostyyppis;
    }

    public List<YhteystietoElementti> getLisatietos() {
        return Collections.unmodifiableList(lisatietos);
    }

    public void addLisatieto(YhteystietoElementti lisatieto) {
        lisatieto.setYhteystietojenTyyppi(this);
        lisatietos.add(lisatieto);
    }

    public void setLisatietos(List<YhteystietoElementti> lisatietos) {

        this.lisatietos.clear();

        for (YhteystietoElementti lisatieto : lisatietos) {

            if (lisatieto != null) {

                addLisatieto(lisatieto);
            }


        }
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }
}
