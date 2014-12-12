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
package fi.vm.sade.organisaatio.business.impl;

import java.util.ArrayList;
import java.util.List;

/**
 * Koodi-luokka, jonka gson serialisoi/unserialisoi REST-kutsujen JSON:sta.
 *
 * Sisältää vain tämän toiminnalisuuden ja rajapintojen käytön kannalta
 * tarpeelliset propertyt.
 *
 */
public class OrganisaatioKoodistoKoodi {

    private String koodiUri;

    private Long version;

    private int versio;

    private String tila;

    private String koodiArvo;

    protected String paivitysPvm;

    protected String voimassaAlkuPvm;

    protected String voimassaLoppuPvm;

    protected List<OrganisaatioKoodistoKoodiMetadata> metadata = new ArrayList<>();
    protected List<OrganisaatioKoodistoKoodiCodeElements> includesCodeElements = new ArrayList<>();
    protected List<OrganisaatioKoodistoKoodiCodeElements> withinCodeElements = new ArrayList<>();
    protected List<OrganisaatioKoodistoKoodiCodeElements> levelsWithCodeElements = new ArrayList<>();

    public String getKoodiUri() {
        return koodiUri;
    }

    public void setKoodiUri(String koodiUri) {
        this.koodiUri = koodiUri;
    }

    public int getVersio() {
        return versio;
    }

    public void setVersio(int versio) {
        this.versio = versio;
    }

    public String getTila() {
        return tila;
    }

    public void setTila(String tila) {
        this.tila = tila;
    }

    public String getKoodiArvo() {
        return koodiArvo;
    }

    public void setKoodiArvo(String koodiArvo) {
        this.koodiArvo = koodiArvo;
    }

    public String getPaivitysPvm() {
        return paivitysPvm;
    }

    public void setPaivitysPvm(String paivitysPvm) {
        this.paivitysPvm = paivitysPvm;
    }

    public String getVoimassaAlkuPvm() {
        return voimassaAlkuPvm;
    }

    public void setVoimassaAlkuPvm(String voimassaAlkuPvm) {
        this.voimassaAlkuPvm = voimassaAlkuPvm;
    }

    public String getVoimassaLoppuPvm() {
        return voimassaLoppuPvm;
    }

    public void setVoimassaLoppuPvm(String voimassaLoppuPvm) {
        this.voimassaLoppuPvm = voimassaLoppuPvm;
    }

    public List<OrganisaatioKoodistoKoodiMetadata> getMetadata() {
        return metadata;
    }

    public void setMetadata(List<OrganisaatioKoodistoKoodiMetadata> metadata) {
        this.metadata = metadata;
    }

    public List<OrganisaatioKoodistoKoodiCodeElements> getIncludesCodeElements() {
        return includesCodeElements;
    }

    public void setIncludesCodeElements(List<OrganisaatioKoodistoKoodiCodeElements> includesCodeElements) {
        this.includesCodeElements = includesCodeElements;
    }

    public List<OrganisaatioKoodistoKoodiCodeElements> getWithinCodeElements() {
        return withinCodeElements;
    }

    public void setWithinCodeElements(List<OrganisaatioKoodistoKoodiCodeElements> withinCodeElements) {
        this.withinCodeElements = withinCodeElements;
    }

    public List<OrganisaatioKoodistoKoodiCodeElements> getLevelsWithCodeElements() {
        return levelsWithCodeElements;
    }

    public void setLevelsWithCodeElements(List<OrganisaatioKoodistoKoodiCodeElements> levelsWithCodeElements) {
        this.levelsWithCodeElements = levelsWithCodeElements;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(final Long version) {
        this.version = version;
    }
}
