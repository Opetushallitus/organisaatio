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

    /**
     * KoodiMetadata-luokka on osa Koodi:a jonka gson serialisoi/unserialisoi REST-kutsujen JSON:sta.
     */
public class OrganisaatioKoodistoKoodiMetadata {

        private String nimi;

        private String kuvaus;

        private String lyhytNimi;

        private String kayttoohje;

        private String kasite;

        private String sisaltaaMerkityksen;

        private String eiSisallaMerkitysta;

        private String huomioitavaKoodi;

        private String sisaltaaKoodiston;

        private String kieli;

        public String getNimi() {
            return nimi;
        }

        public void setNimi(String nimi) {
            this.nimi = nimi;
        }

        public String getKuvaus() {
            return kuvaus;
        }

        public void setKuvaus(String kuvaus) {
            this.kuvaus = kuvaus;
        }

        public String getLyhytNimi() {
            return lyhytNimi;
        }

        public void setLyhytNimi(String lyhytNimi) {
            this.lyhytNimi = lyhytNimi;
        }

        public String getKayttoohje() {
            return kayttoohje;
        }

        public void setKayttoohje(String kayttoohje) {
            this.kayttoohje = kayttoohje;
        }

        public String getKasite() {
            return kasite;
        }

        public void setKasite(String kasite) {
            this.kasite = kasite;
        }

        public String getSisaltaaMerkityksen() {
            return sisaltaaMerkityksen;
        }

        public void setSisaltaaMerkityksen(String sisaltaaMerkityksen) {
            this.sisaltaaMerkityksen = sisaltaaMerkityksen;
        }

        public String getEiSisallaMerkitysta() {
            return eiSisallaMerkitysta;
        }

        public void setEiSisallaMerkitysta(String eiSisallaMerkitysta) {
            this.eiSisallaMerkitysta = eiSisallaMerkitysta;
        }

        public String getHuomioitavaKoodi() {
            return huomioitavaKoodi;
        }

        public void setHuomioitavaKoodi(String huomioitavaKoodi) {
            this.huomioitavaKoodi = huomioitavaKoodi;
        }

        public String getSisaltaaKoodiston() {
            return sisaltaaKoodiston;
        }

        public void setSisaltaaKoodiston(String sisaltaaKoodiston) {
            this.sisaltaaKoodiston = sisaltaaKoodiston;
        }

        public String getKieli() {
            return kieli;
        }

        public void setKieli(String kieli) {
            this.kieli = kieli;
        }

    }

