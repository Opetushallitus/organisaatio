package fi.vm.sade.organisaatio.model;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioStatus;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.model.listeners.ProtectedDataListener;
import fi.vm.sade.organisaatio.repository.impl.OrganisaatioRepositoryImpl;
import fi.vm.sade.organisaatio.service.util.KoodistoUtil;
import fi.vm.sade.organisaatio.service.util.OrganisaatioUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.time.DateUtils;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.*;

import static java.util.stream.Collectors.toSet;


@EntityListeners(ProtectedDataListener.class)
@Entity
@Table(
        name = "organisaatio",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"oid"}),
                @UniqueConstraint(columnNames = {"ytunnus", "organisaatioPoistettu"})}
)
@Comment("Sisältää kaikki organisaatiot.")

@SqlResultSetMapping(
        name = "Organisaatio.findAllDescendants.jalkelaisetRivi",
        classes = @ConstructorResult(
                targetClass = OrganisaatioRepositoryImpl.JalkelaisetRivi.class,
                columns = {
                        @ColumnResult(name = "oid"),
                        @ColumnResult(name = "alkuPvm", type = Date.class),
                        @ColumnResult(name = "lakkautusPvm", type = Date.class),
                        @ColumnResult(name = "parentOid"),
                        @ColumnResult(name = "ytunnus"),
                        @ColumnResult(name = "virastotunnus"),
                        @ColumnResult(name = "oppilaitoskoodi"),
                        @ColumnResult(name = "oppilaitostyyppi"),
                        @ColumnResult(name = "toimipistekoodi"),
                        @ColumnResult(name = "kotipaikka"),
                        @ColumnResult(name = "organisaatiotyyppi"),
                        @ColumnResult(name = "nimiKieli"),
                        @ColumnResult(name = "nimiArvo"),
                        @ColumnResult(name = "kieli"),
                        @ColumnResult(name = "taso", type = Integer.class),
                        @ColumnResult(name = "yritysmuoto"),
                        @ColumnResult(name = "piilotettu")
                }
        )
)


@NamedNativeQuery(
        name = "Organisaatio.findAllDescendants",
        query = "SELECT o.oid, o.alkuPvm, o.lakkautusPvm, p.parent_oid AS parentOid, o.ytunnus, " +
                "o.virastotunnus, o.oppilaitoskoodi, o.oppilaitostyyppi, o.toimipistekoodi, o.kotipaikka, " +
                "t.tyypit AS organisaatiotyyppi, nv.key AS nimiKieli, nv.value AS nimiArvo, " +
                "k.kielet AS kieli, r.parent_position AS taso, o.yritysmuoto, o.piilotettu FROM organisaatio o " +
                "JOIN organisaatio_parent_oids p ON (p.organisaatio_id = o.id) " +
                "JOIN organisaatio_parent_oids r ON (r.organisaatio_id = o.id AND r.parent_oid = :root) " +
                "JOIN organisaatio_tyypit t ON (t.organisaatio_id = o.id AND t.tyypit <> 'Ryhma') " +
                "LEFT JOIN monikielinenteksti n ON (n.id = o.nimi_mkt) " +
                "JOIN monikielinenteksti_values nv ON (nv.id = n.id) " +
                "LEFT JOIN organisaatio_kielet k ON (k.organisaatio_id = o.id) " +
                "WHERE o.organisaatiopoistettu <> TRUE AND o.piilotettu <> TRUE AND o.id IN (" +
                "SELECT organisaatio_id FROM organisaatio_parent_oids WHERE parent_oid = :root) " +
                "ORDER BY taso, o.oid, p.parent_position",
        resultSetMapping = "Organisaatio.findAllDescendants.jalkelaisetRivi"
)
@NamedNativeQuery(
        name = "Organisaatio.findAllDescendantsInclHidden",
        query = "SELECT o.oid, o.alkuPvm, o.lakkautusPvm, p.parent_oid AS parentOid, o.ytunnus, " +
                "o.virastotunnus, o.oppilaitoskoodi, o.oppilaitostyyppi, o.toimipistekoodi, o.kotipaikka, " +
                "t.tyypit AS organisaatiotyyppi, nv.key AS nimiKieli, nv.value AS nimiArvo, " +
                "k.kielet AS kieli, r.parent_position AS taso, o.yritysmuoto, o.piilotettu FROM organisaatio o " +
                "JOIN organisaatio_parent_oids p ON (p.organisaatio_id = o.id) " +
                "JOIN organisaatio_parent_oids r ON (r.organisaatio_id = o.id AND r.parent_oid = :root) " +
                "JOIN organisaatio_tyypit t ON (t.organisaatio_id = o.id AND t.tyypit <> 'Ryhma') " +
                "LEFT JOIN monikielinenteksti n ON (n.id = o.nimi_mkt) " +
                "JOIN monikielinenteksti_values nv ON (nv.id = n.id) " +
                "LEFT JOIN organisaatio_kielet k ON (k.organisaatio_id = o.id) " +
                "WHERE o.organisaatiopoistettu <> TRUE AND o.id IN (" +
                "SELECT organisaatio_id FROM organisaatio_parent_oids WHERE parent_oid = :root) " +
                "ORDER BY taso, o.oid, p.parent_position",
        resultSetMapping = "Organisaatio.findAllDescendants.jalkelaisetRivi"
)

public class Organisaatio extends OrganisaatioBaseEntity {

    private static final long serialVersionUID = 1L;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "organisaatio_tyypit", joinColumns = @JoinColumn(name = "organisaatio_id"))
    @BatchSize(size = 200)
    private Set<String> tyypit = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "organisaatio_vuosiluokat", joinColumns = @JoinColumn(name = "organisaatio_id"))
    @BatchSize(size = 200)
    private Set<String> vuosiluokat = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "organisaatio_ryhmatyypit", joinColumns = @JoinColumn(name = "organisaatio_id"))
    @BatchSize(size = 200)
    private Set<String> ryhmatyypit = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "organisaatio_kayttoryhmat", joinColumns = @JoinColumn(name = "organisaatio_id"))
    @BatchSize(size = 200)
    private Set<String> kayttoryhmat = new HashSet<>();

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "nimi_mkt")
    private MonikielinenTeksti nimi;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "kuvaus_mkt")
    private MonikielinenTeksti kuvaus2;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private OrganisaatioMetaData metadata;

    @Column(length = 256000)
    private String nimihaku;

    @Column
    // TODO regex validointi?
    private String ytunnus;

    @Column
    private String virastoTunnus; // TODO XSS filtteri

    @OneToMany(mappedBy = "organisaatio", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 200)
    private Set<Yhteystieto> yhteystiedot = new HashSet<>();

    @OneToMany(mappedBy = "child", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("alkuPvm")
    private List<OrganisaatioSuhde> parentSuhteet = new ArrayList<>();

    @OneToMany(mappedBy = "parent", cascade = {}, fetch = FetchType.LAZY)
    private Set<OrganisaatioSuhde> childSuhteet = new HashSet<>();

    @OneToMany(mappedBy = "organisaatio", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("alkuPvm")
    @BatchSize(size = 200)
    private List<OrganisaatioNimi> nimet = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "organisaatio", cascade = CascadeType.ALL)
    @BatchSize(size = 200)
    private Set<OrganisaatioLisatietotyyppi> organisaatioLisatietotyypit = new HashSet<>();

    private String yritysmuoto;

    @Temporal(jakarta.persistence.TemporalType.DATE)
    private Date alkuPvm;

    @Temporal(jakarta.persistence.TemporalType.DATE)
    private Date lakkautusPvm;

    private String kotipaikka;

    @ElementCollection
    @CollectionTable(name = "organisaatio_muut_kotipaikat_uris", joinColumns = @JoinColumn(name = "organisaatio_id"))
    @Column(name = "muut_kotipaikat", nullable = false)
    @BatchSize(size = 200)
    private Set<String> muutKotipaikatUris = new HashSet<>();

    private String maa;

    @ElementCollection
    @CollectionTable(name = "organisaatio_kielet", joinColumns = @JoinColumn(name = "organisaatio_id"))
    @Column(name = "kielet", nullable = false)
    @BatchSize(size = 100)
    private Set<String> kielet = new LinkedHashSet<>();

    private String domainNimi;

    @OneToMany(mappedBy = "organisaatio", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 200)
    private Set<YhteystietoArvo> yhteystietoArvos = new HashSet<>();

    @Column(unique = true)
    private String oppilaitosKoodi;

    private String oppilaitosTyyppi;

    @ElementCollection
    @CollectionTable(name = "organisaatio_muut_oppilaitostyypit", joinColumns = @JoinColumn(name = "organisaatio_id"),
            foreignKey = @ForeignKey(name = "organisaatio_muut_oppilaitostyypit_organisaatio_id_fkey"))
    @Column(name = "oppilaitostyyppi", nullable = false)
    @BatchSize(size = 200)
    private Set<String> muutOppilaitosTyyppiUris = new HashSet<>();

    @NotNull
    private String oid;

    private String ytjKieli;

    @Temporal(TemporalType.DATE)
    private Date ytjPaivitysPvm;

    @Temporal(TemporalType.TIMESTAMP)
    private Date tuontiPvm;

    /**
     * false == ei poistettu
     * true == poistettu
     */
    @Column(nullable = true)
    private Boolean organisaatioPoistettu = false;

    private String opetuspisteenJarjNro;
    private String yhteishaunKoulukoodi;

    // OVT-4954
    @Column(length = 32)
    private String toimipisteKoodi;

    // OVT-7684
    @Temporal(TemporalType.TIMESTAMP)
    @UpdateTimestamp
    private Date paivitysPvm;

    // OVT-7684
    @Column(length = 255)
    private String paivittaja;

    @Transient
    @Setter
    @Getter
    private boolean maskingActive = false;

    /**
     * HUOM! parentOidPath -sarakkeelle on lisätty erikseen indeksi (ks. flyway skripti n. V011)
     */
    private String parentIdPath;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "organisaatio_parent_oids",
            joinColumns = @JoinColumn(name = "organisaatio_id"))
    @Column(name = "parent_oid", nullable = false)
    @OrderColumn(name = "parent_position")
    private List<String> parentOids = new ArrayList<>();

    @Temporal(TemporalType.TIMESTAMP)
    private Date tarkastusPvm;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "varhaiskasvatuksen_toimipaikka_tiedot_id")
    private VarhaiskasvatuksenToimipaikkaTiedot varhaiskasvatuksenToimipaikkaTiedot;

    @Column(name = "piilotettu", nullable = false, columnDefinition = "boolean default false")
    private boolean piilotettu;

    /**
     * Utility method to retrieve the current parent of the organisaatio.
     *
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
                    (curSuhde.getLoppuPvm() != null && curSuhde.getLoppuPvm().before(curDate))) {
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

    public Optional<String> getParentOid() {
        if (parentOids == null || parentOids.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(parentOids.get(0));
    }

    /**
     * Utility method to get current status of the organisaatio.
     *
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

    public Set<Yhteystieto> getYhteystiedot() {
        return Collections.unmodifiableSet(yhteystiedot);
    }


    public void setYhteystiedot(Set<Yhteystieto> newYhteystiedot) {
        yhteystiedot = newYhteystiedot;
    }

    public void addYhteystieto(Yhteystieto yhteystieto) {
        this.yhteystiedot.add(yhteystieto);
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

    private static int sortIntFromYhteystieto(Yhteystieto a) {
        String kieli = a.getKieli();
        if (kieli.startsWith("kieli_fi")) return 1;
        if (kieli.startsWith("kieli_sv")) return 2;
        else return 3;
    }

    static int sortYhteysTietoByKieli(Yhteystieto a, Yhteystieto b) {
        return Integer.compare(sortIntFromYhteystieto(a), sortIntFromYhteystieto(b));
    }

    private Osoite getOsoite(String osoiteTyyppi) {
        return yhteystiedot.stream()
                .filter(Osoite.class::isInstance)
                .map(Osoite.class::cast)
                .filter(o -> osoiteTyyppi.equals(o.getOsoiteTyyppi()))
                .min(Organisaatio::sortYhteysTietoByKieli)
                .orElse(null);
    }

    public Osoite getPostiosoiteByKieli(String kielikoodi) {
        for (Yhteystieto yhteystieto : getYhteystiedot()) {
            if (yhteystieto instanceof Osoite && yhteystieto.getKieli().equals(kielikoodi)
                    && ((Osoite) yhteystieto).getOsoiteTyyppi().equals(Osoite.TYYPPI_POSTIOSOITE)) {
                return (Osoite) yhteystieto;
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

    public Set<String> getMuutKotipaikatUris() {
        return muutKotipaikatUris;
    }

    public void setMuutKotipaikatUris(Set<String> muutKotipaikatUris) {
        this.muutKotipaikatUris = muutKotipaikatUris;
    }

    public Date getAlkuPvm() {
        return alkuPvm;
    }

    private static Date filterPvm(Date pvm) {
        return pvm == null ? null : DateUtils.truncate(pvm, Calendar.DATE);
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

    public Set<String> getTyypit() {
        return Collections.unmodifiableSet(tyypit);
    }

    public void setTyypit(Set<String> tyypit) {
        this.tyypit.clear();
        this.tyypit.addAll(tyypit);
    }

    public Collection<String> getKielet() {
        return Collections.unmodifiableSet(kielet);
    }

    public void setKielet(Collection<String> kielet) {
        this.kielet.clear();
        this.kielet.addAll(kielet);
    }

    public String getMaa() {
        return maa;
    }

    public void setMaa(String maa) {
        this.maa = maa;
    }

    public Set<YhteystietoArvo> getYhteystietoArvos() {
        return yhteystietoArvos;
    }

    public void setYhteystietoArvos(Set<YhteystietoArvo> yhteystietoArvos) {
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

    public Set<String> getMuutOppilaitosTyyppiUris() {
        return muutOppilaitosTyyppiUris;
    }

    public void setMuutOppilaitosTyyppiUris(Set<String> muutOppilaitosTyyppiUris) {
        this.muutOppilaitosTyyppiUris = muutOppilaitosTyyppiUris;
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
    public Set<String> getVuosiluokat() {
        return Collections.unmodifiableSet(vuosiluokat);
    }

    /**
     * @param vuosiluokat the vuosiluokat to set
     */
    public void setVuosiluokat(Set<String> vuosiluokat) {
        this.vuosiluokat.clear();
        this.vuosiluokat.addAll(vuosiluokat);
    }

    /**
     * @return ryhmatyypit
     */
    public Set<String> getRyhmatyypit() {
        return Collections.unmodifiableSet(ryhmatyypit);
    }

    /**
     * @param ryhmatyypit ryhmatyypit to set
     */
    public void setRyhmatyypit(Set<String> ryhmatyypit) {
        this.ryhmatyypit.clear();
        this.ryhmatyypit.addAll(ryhmatyypit);
    }

    /**
     * Palauttaa ryhmätyypit V1-rajapinnan muodossa.
     *
     * @return ryhmatyypit
     */
    public Set<String> getRyhmatyypitV1() {
        return getRyhmatyypit().stream()
                .map(KoodistoUtil::getRyhmatyyppiV1)
                .filter(Objects::nonNull)
                .collect(toSet());
    }

    /**
     * Asettaa ryhmätyypit V1-rajapinnan muodosta.
     *
     * @param ryhmatyypit ryhmatyypit to set
     */
    public void setRyhmatyypitV1(Set<String> ryhmatyypit) {
        this.ryhmatyypit.clear();
        this.ryhmatyypit.addAll(ryhmatyypit.stream().map(KoodistoUtil::getRyhmatyyppiV3).collect(toSet()));
    }

    /**
     * @return kayttoryhmat
     */
    public Set<String> getKayttoryhmat() {
        return Collections.unmodifiableSet(kayttoryhmat);
    }

    /**
     * @param kayttoryhmat kayttoryhmat to set
     */
    public void setKayttoryhmat(Set<String> kayttoryhmat) {
        this.kayttoryhmat.clear();
        this.kayttoryhmat.addAll(kayttoryhmat);
    }

    /**
     * Palauttaa käyttöryhmät V1-rajapinnan muodossa.
     *
     * @return kayttoryhmat
     */
    public Set<String> getKayttoryhmatV1() {
        return getKayttoryhmat().stream()
                .map(KoodistoUtil::getKayttoryhmaV1)
                .filter(Objects::nonNull)
                .collect(toSet());
    }

    /**
     * Asettaa käyttöryhmät V1-rajapinnan muodosta.
     *
     * @param kayttoryhmat kayttoryhmat to set
     */
    public void setKayttoryhmatV1(Set<String> kayttoryhmat) {
        this.kayttoryhmat.clear();
        this.kayttoryhmat.addAll(kayttoryhmat.stream().map(KoodistoUtil::getKayttoryhmaV3).collect(toSet()));
    }

    /**
     * @return the organisaatioPoistettu
     */
    public boolean isOrganisaatioPoistettu() {
        return organisaatioPoistettu;
    }

    /**
     * @param organisaatioPoistettu the organisaatioPoistettu to set
     */
    public void setOrganisaatioPoistettu(boolean organisaatioPoistettu) {
        this.organisaatioPoistettu = organisaatioPoistettu;
    }

    public String getYtjKieli() {
        return ytjKieli;
    }

    public void setYtjKieli(String ytjKieli) {
        this.ytjKieli = ytjKieli;
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
        Organisaatio parent = this.getParent();
        if (parent != null && this.tyypit.contains(OrganisaatioTyyppi.TOIMIPISTE.koodiValue())) {
            Map<String, String> values = new HashMap<>();
            MonikielinenTeksti parentNimi = parent.getNimi();
            this.nimi.getValues().keySet().forEach(a -> {
                String currentNimiString = this.nimi.getString(a);
                String parentNimiString = parentNimi.getString(a);
                values.put(a, currentNimiString.equals(parentNimiString) ? currentNimiString : String.format("%s, %s", parentNimiString, currentNimiString));
            });
            MonikielinenTeksti modified = new MonikielinenTeksti();
            modified.setValues(values);
            return modified;
        }
        return this.nimi;
    }

    public MonikielinenTeksti getActualNimi() {
        return this.nimi;
    }

    /**
     * Set nimi (name) as multilingual text.
     *
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
     *
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
     *
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

        for (OrganisaatioSuhde os : parentSuhteet) {
            if (os.getSuhdeTyyppi() == tyyppi) {
                result.add(os);
            }
        }
        return result;
    }

    public Set<OrganisaatioSuhde> getChildSuhteet() {
        return childSuhteet;
    }

    public Set<OrganisaatioSuhde> getChildSuhteet(OrganisaatioSuhde.OrganisaatioSuhdeTyyppi tyyppi) {
        Set<OrganisaatioSuhde> result = new HashSet<>();

        for (OrganisaatioSuhde os : childSuhteet) {
            if (os.getSuhdeTyyppi() == tyyppi) {
                result.add(os);
            }
        }
        return result;
    }

    public Set<Organisaatio> getChildren(boolean aktiiviset, boolean suunnitellut, boolean lakkautetut) {
        Set<Organisaatio> result = new HashSet<>();

        Date now = new Date();
        for (OrganisaatioSuhde os : childSuhteet) {
            // Ei huomioida liitoksia
            if (os.getSuhdeTyyppi() == OrganisaatioSuhde.OrganisaatioSuhdeTyyppi.LIITOS) {
                continue;
            }

            // Organisaatiosuhde ei ole lakannut, eikä lasta ole poistettu
            if ((os.getLoppuPvm() == null || os.getLoppuPvm().after(now))
                    && !os.getChild().isOrganisaatioPoistettu()) {
                if (aktiiviset && OrganisaatioUtil.isAktiivinen(os.getChild())
                        || suunnitellut && OrganisaatioUtil.isSuunniteltu(os.getChild())
                        || lakkautetut && OrganisaatioUtil.isPassive(os.getChild())) {
                    result.add(os.getChild());
                }
            }
        }
        return result;
    }

    public Set<Organisaatio> getChildren(boolean includeLakkautetut) {
        return getChildren(true, true, includeLakkautetut);
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

            if ((now == null || os.getLoppuPvm() == null || os.getLoppuPvm().after(now))
                    && !os.getChild().isOrganisaatioPoistettu()
                    && (now == null || os.getChild().getLakkautusPvm() == null || os.getChild().getLakkautusPvm().after(now))) {
                ret++;
            }
        }
        return ret;
    }

    public void setParentSuhteet(List<OrganisaatioSuhde> parentSuhteet) {
        this.parentSuhteet = parentSuhteet;
    }

    public String getParentIdPath() {
        return parentIdPath;
    }

    public String getParentOidPath() {
        return OrganisaatioUtil.parentOidPath(parentOids);
    }

    public void setParentIdPath(String parentIdPath) {
        this.parentIdPath = parentIdPath;
    }

    public List<String> getParentOids() {
        return parentOids;
    }

    public void setParentOids(List<String> parentOids) {
        this.parentOids = parentOids;
    }

    /**
     * Gets the running number of the opetuspiste.
     *
     * @return the running number of the opetuspiste.
     */
    public String getOpetuspisteenJarjNro() {
        return opetuspisteenJarjNro;
    }

    /**
     * Sets the running number of the opetuspiste organization.
     *
     * @param opetuspisteenJarjNro - the running number to set.
     */
    public void setOpetuspisteenJarjNro(String opetuspisteenJarjNro) {
        this.opetuspisteenJarjNro = opetuspisteenJarjNro;
    }

    /**
     * Returns the joint application system school code for the organization.
     *
     * @return the joint application system school code.
     */
    public String getYhteishaunKoulukoodi() {
        return yhteishaunKoulukoodi;
    }

    /**
     * Sets the joint application system school code for the organization.
     *
     * @param yhteishaunKoulukoodi the joint application system school code to set.
     */
    public void setYhteishaunKoulukoodi(String yhteishaunKoulukoodi) {
        this.yhteishaunKoulukoodi = yhteishaunKoulukoodi;
    }

    public Date getTarkastusPvm() {
        return tarkastusPvm;
    }

    public void setTarkastusPvm(Date tarkastusPvm) {
        this.tarkastusPvm = tarkastusPvm;
    }

    public Date getTuontiPvm() {
        return tuontiPvm;
    }

    public void setTuontiPvm(Date tuontiPvm) {
        this.tuontiPvm = tuontiPvm;
    }

    public Puhelinnumero getPuhelin(String tyyppi, String kieliKoodi) {
        if (tyyppi != null && kieliKoodi != null) {
            for (Yhteystieto yhteystieto : getYhteystiedot()) {
                if (yhteystieto instanceof Puhelinnumero) {
                    if (tyyppi.equals(((Puhelinnumero) yhteystieto).getTyyppi()) && kieliKoodi.equals(yhteystieto.getKieli())) {
                        return (Puhelinnumero) yhteystieto;
                    }
                }
            }
        }
        return null;
    }

    public Www getWww(String kieliKoodi) {
        if (kieliKoodi != null) {
            for (Yhteystieto yhteystieto : getYhteystiedot()) {
                if (yhteystieto instanceof Www && kieliKoodi.equals(yhteystieto.getKieli())) {
                    return (Www) yhteystieto;
                }
            }
        }
        return null;
    }

    public Email getEmail(String kieliKoodi) {
        if (kieliKoodi != null) {
            for (Yhteystieto yhteystieto : getYhteystiedot()) {
                if (yhteystieto instanceof Email && kieliKoodi.equals(yhteystieto.getKieli())) {
                    return (Email) yhteystieto;
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

    public void addNimi(OrganisaatioNimi organisaatioNimi) {
        this.nimet.add(organisaatioNimi);
    }

    public OrganisaatioNimi getCurrentNimi() {
        OrganisaatioNimi currentOrgNimi = null;
        for (OrganisaatioNimi orgNimi : getNimet()) {
            if (orgNimi.getNimi().equals(getNimi())) {
                currentOrgNimi = orgNimi;
            }
        }
        return currentOrgNimi;
    }

    public boolean isPiilotettu() {
        return piilotettu;
    }

    public void setPiilotettu(boolean piilotettu) {
        this.piilotettu = piilotettu;
    }

    public Set<OrganisaatioLisatietotyyppi> getOrganisaatioLisatietotyypit() {
        return organisaatioLisatietotyypit;
    }

    public void setOrganisaatioLisatietotyypit(Set<OrganisaatioLisatietotyyppi> organisaatioLisatietotyypit) {
        this.organisaatioLisatietotyypit = organisaatioLisatietotyypit;
    }

    public VarhaiskasvatuksenToimipaikkaTiedot getVarhaiskasvatuksenToimipaikkaTiedot() {
        return varhaiskasvatuksenToimipaikkaTiedot;
    }

    public void setVarhaiskasvatuksenToimipaikkaTiedot(VarhaiskasvatuksenToimipaikkaTiedot varhaiskasvatuksenToimipaikkaTiedot) {
        this.varhaiskasvatuksenToimipaikkaTiedot = varhaiskasvatuksenToimipaikkaTiedot;
    }
}
