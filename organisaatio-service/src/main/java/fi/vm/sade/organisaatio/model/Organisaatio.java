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

import java.util.*;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.time.DateUtils;

import fi.vm.sade.organisaatio.service.util.OrganisaatioUtil;
import fi.vm.sade.security.xssfilter.FilterXss;
import fi.vm.sade.security.xssfilter.XssFilterListener;


/**
 * @author tuomaskatva
 */
@Entity
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = {"oid"}),
    @UniqueConstraint(columnNames = {"ytunnus", "organisaatioPoistettu"})}
)
@org.hibernate.annotations.Table(appliesTo = "Organisaatio", comment = "Sisältää kaikki organisaatiot.")
@EntityListeners(XssFilterListener.class)
public class Organisaatio extends OrganisaatioBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * Organisaation status.
     */
    public enum OrganisaatioStatus {
        AKTIIVINEN,
        SUUNNITELTU,
        PASSIIVINEN,
        POISTETTU
    };

    @ElementCollection(fetch= FetchType.EAGER)
    @CollectionTable(name = "organisaatio_tyypit", joinColumns = @JoinColumn(name = "organisaatio_id"))
    private List<String> tyypit = new ArrayList<String>();

    @ElementCollection
    @CollectionTable(name = "organisaatio_vuosiluokat", joinColumns = @JoinColumn(name = "organisaatio_id"))
    private List<String> vuosiluokat = new ArrayList<String>();

    @ElementCollection
    @CollectionTable(name = "organisaatio_ryhmatyypit", joinColumns = @JoinColumn(name = "organisaatio_id"))
    private List<String> ryhmatyypit = new ArrayList<String>();

    @ElementCollection
    @CollectionTable(name = "organisaatio_kayttoryhmat", joinColumns = @JoinColumn(name = "organisaatio_id"))
    private List<String> kayttoryhmat = new ArrayList<String>();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "nimi_mkt")
    private MonikielinenTeksti nimi;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "kuvaus_mkt")
    private MonikielinenTeksti kuvaus2;

    @OneToOne(cascade = CascadeType.ALL, fetch= FetchType.LAZY)
    private OrganisaatioMetaData metadata;

    @Column(length=256000)
    private String nimihaku;

    @Column
    // TODO regex validointi?
    private String ytunnus;

    @Column
    @FilterXss
    private String virastoTunnus;

    @OneToMany(mappedBy = "organisaatio", cascade = CascadeType.ALL, orphanRemoval=true)
    private List<Yhteystieto> yhteystiedot = new ArrayList<Yhteystieto>();

    @OneToMany(mappedBy = "child", cascade = CascadeType.ALL, fetch=FetchType.LAZY)
    private List<OrganisaatioSuhde> parentSuhteet = new ArrayList<OrganisaatioSuhde>();

    @OneToMany(mappedBy = "parent", cascade = {}, fetch=FetchType.LAZY)
    private List<OrganisaatioSuhde> childSuhteet = new ArrayList<OrganisaatioSuhde>();

    @OneToMany(mappedBy = "organisaatio", cascade = CascadeType.ALL, orphanRemoval=true, fetch=FetchType.LAZY)
    @OrderBy("alkuPvm")
    private List<OrganisaatioNimi> nimet = new ArrayList<OrganisaatioNimi>();


    private String yritysmuoto;

    @Temporal(javax.persistence.TemporalType.DATE)
    private Date alkuPvm;

    @Temporal(javax.persistence.TemporalType.DATE)
    private Date lakkautusPvm;

    private String kotipaikka;
    private String maa;

    // @NotNull
    @ElementCollection
    @CollectionTable(name = "organisaatio_kielet", joinColumns = @JoinColumn(name = "organisaatio_id"))
    private List<String> kielet = new ArrayList<String>();

    private String domainNimi;

    @OneToMany(mappedBy = "organisaatio", cascade = CascadeType.ALL, orphanRemoval=true)
    private List<YhteystietoArvo> yhteystietoArvos = new ArrayList<YhteystietoArvo>();

    @Column(unique = true)
    private String oppilaitosKoodi;

    private String oppilaitosTyyppi;

    private String oid;

    @Temporal(TemporalType.DATE)
    private Date ytjPaivitysPvm;

    @Temporal(TemporalType.TIMESTAMP)
    private Date tuontiPvm;

    /**
     * false == ei poistettu
     * null == poistettu
     *  - huom. true ei sallittu
     */
    @Column(nullable=true)
    private Boolean organisaatioPoistettu = false;

    private String opetuspisteenJarjNro;
    private String yhteishaunKoulukoodi;

    // OVT-4954
    @Column(length = 32)
    private String toimipisteKoodi;

    // OVT-7684
    @Temporal(TemporalType.TIMESTAMP)
    private Date paivitysPvm;

    // OVT-7684
    @Column(length = 255)
    private String paivittaja;

    /**
     * HUOM! parentOidPath -sarakkeelle on lisätty erikseen indeksi (ks. flyway skripti n. V011)
     */
    private String parentOidPath;
    private String parentIdPath;
    private String organisaatiotyypitStr;

    /**
     * Utility method to retrieve the current parent of the organisaatio.
     * @return the parent organisaatio
     */
    public Organisaatio getParent() {
        OrganisaatioSuhde latestSuhde = null;
        Date curDate = new Date();
        for (OrganisaatioSuhde curSuhde : parentSuhteet) {
            // Ei huomioida liitoksia
            if (curSuhde.getSuhdeTyyppi() == OrganisaatioSuhde.OrganisaatioSuhdeTyyppi.LIITOS) {
                continue;
            }

            // Ei oteta huomioon suhteita, jotka tulevaisuudessa tai jotka ovat lakanneet
            if (curSuhde.getAlkuPvm().after(curDate) ||
                    (curSuhde.getLoppuPvm() != null && curSuhde.getLoppuPvm().before(curDate)))
            {
                continue;
            }
            if (latestSuhde == null) {
                // Ensimmäinen löytynyt validi suhde
                latestSuhde = curSuhde;
            } else if (latestSuhde.getAlkuPvm().before(curSuhde.getAlkuPvm())) {
                // Aikaisemmin löydettyä suhdetta uudempi suhde
                latestSuhde = curSuhde;
            }
        }
        return (latestSuhde != null) ? latestSuhde.getParent() : null;
    }

    /**
     * Utility method to get current status of the organisaatio.
     * @return the status
     */
    public OrganisaatioStatus getStatus() {
        if (OrganisaatioUtil.isPassive(this)) {
            return OrganisaatioStatus.PASSIIVINEN;
        }
        if (this.isOrganisaatioPoistettu()) {
            return OrganisaatioStatus.POISTETTU;
        }
        if (OrganisaatioUtil.isSuunniteltu(this)) {
            return OrganisaatioStatus.SUUNNITELTU;
        }
        return OrganisaatioStatus.AKTIIVINEN;
    }

    public OrganisaatioMetaData getMetadata() {
        return metadata;
    }

    public void setMetadata(OrganisaatioMetaData metadata) {
        this.metadata = metadata;
    }

    public List<Yhteystieto> getYhteystiedot() {
        return Collections.unmodifiableList(yhteystiedot);
    }


    public void setYhteystiedot(List<Yhteystieto> newYhteystiedot) {
        yhteystiedot = newYhteystiedot;
    }


    public String getYtunnus() {
        return ytunnus;
    }

    public void setYtunnus(String ytunnus) {
        this.ytunnus = ytunnus;
    }

    public Osoite getPostiosoite() {
        return getOsoite(ModelConstants.TYYPPI_POSTIOSOITE);
    }

    public Osoite getKayntiosoite() {
        return getOsoite(ModelConstants.TYYPPI_KAYNTIOSOITE);
    }

    public Object getPuhelin() {
        for (Yhteystieto yhteystieto : yhteystiedot) {
            if (yhteystieto instanceof Puhelinnumero) {
                return yhteystieto;
            }
        }
        return null;
    }

    private Osoite getOsoite(String osoiteTyyppi) {
        for (Yhteystieto yhteystieto : yhteystiedot) {
            if (yhteystieto instanceof Osoite) {
                Osoite osoite = (Osoite) yhteystieto;
                if (osoiteTyyppi.equals(osoite.getOsoiteTyyppi())) {
                    return osoite;
                }
            }
        }
        return null;
    }

    public String getYritysmuoto() {
        return yritysmuoto;
    }

    public void setYritysmuoto(String yritysmuoto) {
        this.yritysmuoto = yritysmuoto;
    }

    public String getKotipaikka() {
        return kotipaikka;
    }

    public void setKotipaikka(String kotipaikka) {
        this.kotipaikka = kotipaikka;
    }

    public Date getAlkuPvm() {
        return alkuPvm;
    }

    private static Date filterPvm(Date pvm) {
        return pvm==null ? null : DateUtils.truncate(pvm, Calendar.DATE);
    }

    public void setAlkuPvm(Date alkuPvm) {
        this.alkuPvm = filterPvm(alkuPvm);
    }

    public Date getLakkautusPvm() {
        return lakkautusPvm;
    }

    public void setLakkautusPvm(Date lakkautusPvm) {
        this.lakkautusPvm = filterPvm(lakkautusPvm);
    }

    public List<String> getTyypit() {
        return Collections.unmodifiableList(tyypit);
    }

    public void setTyypit(List<String> tyypit) {
        this.tyypit.clear();
        this.tyypit.addAll(tyypit);
    }

    public List<String> getKielet() {
        return Collections.unmodifiableList(kielet);
    }

    public void setKielet(List<String> kielet) {
        this.kielet.clear();
        this.kielet.addAll(kielet);
    }

    public String getMaa() {
        return maa;
    }

    public void setMaa(String maa) {
        this.maa = maa;
    }

    public List<YhteystietoArvo> getYhteystietoArvos() {
        return yhteystietoArvos;
    }

    public void setYhteystietoArvos(List<YhteystietoArvo> yhteystietoArvos) {
        this.yhteystietoArvos.clear();
        if (yhteystietoArvos == null) {
            return;
        }
        for (YhteystietoArvo yhteystietoArvo : yhteystietoArvos) {
            yhteystietoArvo.setOrganisaatio(this);
            this.yhteystietoArvos.add(yhteystietoArvo);
        }
    }

    public String getDomainNimi() {
        return domainNimi;
    }

    public void setDomainNimi(String domainNimi) {
        this.domainNimi = domainNimi;
    }

    public String getOppilaitosKoodi() {
        return oppilaitosKoodi;
    }

    public void setOppilaitosKoodi(String oppilaitosKoodi) {
        this.oppilaitosKoodi = oppilaitosKoodi;
    }

    public String getOppilaitosTyyppi() {
        return oppilaitosTyyppi;
    }

    public void setOppilaitosTyyppi(String oppilaitosTyyppi) {
        this.oppilaitosTyyppi = oppilaitosTyyppi;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }
    /**
     * @return the vuosiluokat
     */
    public List<String> getVuosiluokat() {
        return Collections.unmodifiableList(vuosiluokat);//vuosiluokat;
    }

    /**
     * @param vuosiluokat the vuosiluokat to set
     */
    public void setVuosiluokat(List<String> vuosiluokat) {
        this.vuosiluokat.clear();
        this.vuosiluokat.addAll(vuosiluokat);// = vuosiluokat;
    }

    /**
     * @return ryhmatyypit
     */
    public List<String> getRyhmatyypit() {
        return Collections.unmodifiableList(ryhmatyypit);
    }

    /**
     * @param ryhmatyypit ryhmatyypit to set
     */
    public void setRyhmatyypit(List<String> ryhmatyypit) {
        this.ryhmatyypit.clear();
        this.ryhmatyypit.addAll(ryhmatyypit);
    }

    /**
     * @return kayttoryhmat
     */
    public List<String> getKayttoryhmat() {
        return Collections.unmodifiableList(kayttoryhmat);
    }

    /**
     * @param kayttoryhmat kayttoryhmat to set
     */
    public void setKayttoryhmat(List<String> kayttoryhmat) {
        this.kayttoryhmat.clear();
        this.kayttoryhmat.addAll(kayttoryhmat);
    }

    /**
     * @return the organisaatioPoistettu
     */
    public boolean isOrganisaatioPoistettu() {
        return organisaatioPoistettu==null;
    }

    /**
     * @param organisaatioPoistettu the organisaatioPoistettu to set
     */
    public void setOrganisaatioPoistettu(boolean organisaatioPoistettu) {
        this.organisaatioPoistettu = organisaatioPoistettu ? null : false;
    }

    /**
     * @return the ytjPaivitysPvm
     */
    public Date getYtjPaivitysPvm() {
        return ytjPaivitysPvm;
    }

    /**
     * @param ytjPaivitysPvm the ytjPaivitysPvm to set
     */
    public void setYtjPaivitysPvm(Date ytjPaivitysPvm) {
        this.ytjPaivitysPvm = ytjPaivitysPvm;
    }

    public String getVirastoTunnus() {
        return virastoTunnus;
    }

    public void setVirastoTunnus(String virastoTunnus) {
        this.virastoTunnus = virastoTunnus;
    }

    /**
     * @return multilingual nimi (name)
     */
    public MonikielinenTeksti getNimi() {
        return nimi;
    }

    /**
     * Set nimi (name) as multilingual text.
     * @param nimi
     */
    public void setNimi(MonikielinenTeksti nimi) {
        this.nimi = nimi;
    }

    /**
     * @return multilingual kuvaus (description)
     */
    public MonikielinenTeksti getKuvaus2() {
        return kuvaus2;
    }

    /**
     * Set kuvaus (descriptive text) as multilingual text.
     * @param kuvaus2
     */
    public void setKuvaus2(MonikielinenTeksti kuvaus2) {
        this.kuvaus2 = kuvaus2;
    }

    public String getNimihaku() {
        return nimihaku;
    }


    public void setNimihaku(String nimihaku) {
        this.nimihaku = nimihaku;
    }

    /**
     * Returns the metadata of the parent of the organisation.
     * @return
     */
    public OrganisaatioMetaData getParentMetadata() {
        Organisaatio parent = this.getParent();
        if (parent != null
                && parent.getMetadata() != null) {
            return parent.getMetadata();
        } else if (parent != null) {
            return parent.getParentMetadata();
        }
        return null;
    }

    public List<OrganisaatioSuhde> getParentSuhteet() {
        return parentSuhteet;
    }

    public List<OrganisaatioSuhde> getParentSuhteet(OrganisaatioSuhde.OrganisaatioSuhdeTyyppi tyyppi) {
        List<OrganisaatioSuhde> result = new ArrayList<>();

        Date now = new Date();
        for (OrganisaatioSuhde os : parentSuhteet) {
            if (os.getSuhdeTyyppi() == tyyppi) {
                result.add(os);
            }
        }
        return result;
    }

    public List<OrganisaatioSuhde> getChildSuhteet() {
        return childSuhteet;
    }

    public List<OrganisaatioSuhde> getChildSuhteet(OrganisaatioSuhde.OrganisaatioSuhdeTyyppi tyyppi) {
        List<OrganisaatioSuhde> result = new ArrayList<>();

        Date now = new Date();
        for (OrganisaatioSuhde os : childSuhteet) {
            if (os.getSuhdeTyyppi() == tyyppi) {
                result.add(os);
            }
        }
        return result;
    }

    public List<Organisaatio> getChildren(boolean includeLakkautetut) {
        List<Organisaatio> result = new ArrayList<>();

        Date now = new Date();
        for (OrganisaatioSuhde os : childSuhteet) {
            // Ei huomioida liitoksia
            if (os.getSuhdeTyyppi() == OrganisaatioSuhde.OrganisaatioSuhdeTyyppi.LIITOS) {
                continue;
            }

            // Organisaatiosuhde ei ole lakannut, eikä lasta ole poistettu
            if ((os.getLoppuPvm()==null || os.getLoppuPvm().after(now))
                    && !os.getChild().isOrganisaatioPoistettu()) {

                // Aliorganisaatio on lakkautettu, katsotaan otetaanko se mukaan
                if (OrganisaatioUtil.isPassive(os.getChild())) {
                    if (includeLakkautetut) {
                        result.add(os.getChild());
                    }
                }
                else {
                    result.add(os.getChild());
                }
            }
        }
        return result;
    }


    /**
     * Laskee organisaatiosuhteet.
     *
     * @param now Aikarajaus; jos ei null, lasketaan vain ne organisaatiot joita ei ole lakkautettu tähän päivään mennessä.
     * @return Aliorganisaatioiden lukumäärä.
     */
    public int getChildCount(Date now) {
        int ret = 0;
        for (OrganisaatioSuhde os : childSuhteet) {
            // Ei huomioida liitoksia
            if (os.getSuhdeTyyppi() == OrganisaatioSuhde.OrganisaatioSuhdeTyyppi.LIITOS) {
                continue;
            }

            if ((now==null || os.getLoppuPvm()==null || os.getLoppuPvm().after(now) )
                    && !os.getChild().isOrganisaatioPoistettu()
                    && (now==null || os.getChild().getLakkautusPvm()==null || os.getChild().getLakkautusPvm().after(now)) ) {
                ret++;
            }
        }
        return ret;
    }

    public void setParentSuhteet(List<OrganisaatioSuhde> parentSuhteet) {
        this.parentSuhteet = parentSuhteet;
    }

    public String getParentOidPath() {
        return parentOidPath;
    }

    public void setParentOidPath(String parentOidPath) {
        this.parentOidPath = parentOidPath;
    }

    public String getParentIdPath() {
        return parentIdPath;
    }

    public void setParentIdPath(String parentIdPath) {
        this.parentIdPath = parentIdPath;
    }

    /**
     * Gets the running number of the opetuspiste.
     * @return the running number of the opetuspiste.
     */
    public String getOpetuspisteenJarjNro() {
        return opetuspisteenJarjNro;
    }

    /**
     * Sets the running number of the opetuspiste organization.
     * @param opetuspisteenJarjNro - the running number to set.
     */
    public void setOpetuspisteenJarjNro(String opetuspisteenJarjNro) {
        this.opetuspisteenJarjNro = opetuspisteenJarjNro;
    }

    /**
     * Returns the joint application system school code for the organization.
     * @return the joint application system school code.
     */
    public String getYhteishaunKoulukoodi() {
        return yhteishaunKoulukoodi;
    }

    /**
     * Sets the joint application system school code for the organization.
     * @param yhteishaunKoulukoodi the joint application system school code to set.
     */
    public void setYhteishaunKoulukoodi(String yhteishaunKoulukoodi) {
        this.yhteishaunKoulukoodi = yhteishaunKoulukoodi;
    }

    public String getOrganisaatiotyypitStr() {
        return organisaatiotyypitStr;
    }

    public void setOrganisaatiotyypitStr(String organisaatiotyypitStr) {
        this.organisaatiotyypitStr = organisaatiotyypitStr;
    }

    public Date getTuontiPvm() {
        return tuontiPvm;
    }

    public void setTuontiPvm(Date tuontiPvm) {
        this.tuontiPvm = tuontiPvm;
    }

    public Puhelinnumero getPuhelin(String tyyppi) {
        if (tyyppi == null) {
            return null;
        }
        for (Yhteystieto yhteystieto : getYhteystiedot()) {
            if (yhteystieto instanceof Puhelinnumero) {
                if (tyyppi.equals(((Puhelinnumero) yhteystieto).getTyyppi())) {
                    return (Puhelinnumero) yhteystieto;
                }
            }
        }
        return null;
    }

    public String getToimipisteKoodi() {
        return toimipisteKoodi;
    }

    public void setToimipisteKoodi(String toimipisteKoodi) {
        this.toimipisteKoodi = toimipisteKoodi;
    }

    public Date getPaivitysPvm() {
        return paivitysPvm;
    }

    public void setPaivitysPvm(Date paivitysPvm) {
        this.paivitysPvm = paivitysPvm;
    }

    public String getPaivittaja() {
        return paivittaja;
    }

    public void setPaivittaja(String paivittaja) {
        this.paivittaja = paivittaja;
    }

    /**
     * @return the nimet
     */
    public List<OrganisaatioNimi> getNimet() {
        return Collections.unmodifiableList(nimet);
    }

    /**
     * @param nimet the nimet to set
     */
    public void setNimet(List<OrganisaatioNimi> nimet) {
        this.nimet = nimet;
    }
}
