package fi.vm.sade.organisaatio.repository;

import fi.vm.sade.generic.dao.JpaDAO;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.dto.ChildOidsCriteria;
import fi.vm.sade.organisaatio.dto.mapping.RyhmaCriteriaDto;
import fi.vm.sade.organisaatio.dto.v3.OrganisaatioRDTOV3;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.service.search.SearchCriteria;

import javax.persistence.EntityManager;
import javax.persistence.OptimisticLockException;
import java.time.LocalDate;
import java.util.*;


public interface OrganisaatioRepositoryCustom {

    /**
     * Palauttaa hakukriteerien mukaiset organisaatiot (ei ryhmiä).
     *
     * @param criteria hakukriteerit
     * @param now nykyhetki
     * @return organisaatiot
     */
    Collection<Organisaatio> findBy(SearchCriteria criteria, Date now);

    /**
     * Palauttaa organisaatioiden aktiivisten aliorganisaatioiden lukumäärät.
     *
     * @param now nykyhetki
     * @return aliorganisaatioiden lukumäärät
     */
    Map<String, Long> countActiveChildrenByOid(Date now);

    /**
     * Haetaan organisaatioita annetuilla hakukriteereillä
     *
     * @param kieliList kielirajaus kielivalikoima-koodiston koodiUreja: ["kielivalikoima_en", "kielivalikoima_sv"]
     * @param kuntaList kuntarajaus kunta-koodiston koodiUreja: ["kunta_905", "kunta_401"]
     * @param oppilaitostyyppiList oppilaitostyyppirajaus oppilaitostyyppi-koodiston koodiUreja: ["oppilaitostyyppi_19", "oppilaitostyyppi_91"]
     * @param vuosiluokkaList vuosiluokkarajaus vuosiluokat-koodiston koodiUreja: ["vuosiluokat_1","vuosiluokat_2"]
     * @param ytunnusList y-tunnusrajaus: ["0147510-4", "0203797-4"]
     * @param oidList
     * @param limit hakutuloksen määrän rajoite
     * @return
     */
    Set<Organisaatio> findBySearchCriteria(
            Set<String> kieliList,
            Set<String> kuntaList,
            Set<String> oppilaitostyyppiList,
            Set<String> vuosiluokkaList,
            Set<String> ytunnusList,
            Set<String> oidList,
            int limit);

    /**
     * Haetaan organisaatiota oidin perusteella.
     *
     * @param oid
     * @return
     */
    Organisaatio customFindByOid(String oid);

    List<OrganisaatioRDTOV3> findByOids(Collection<String> oids);

    /**
     * Haetaan organisaatioita oidlistan perusteella.
     *
     * @param oidList
     * @param maxResults
     * @return
     */
    List<Organisaatio> findByOidList(List<String> oidList, int maxResults);

    /**
     * Return parent org oids to org, optimized for the auth use.
     *
     * Parents are returned in "root first" order.
     * <pre>
     * Example: a (b c (f g)) (d e)
     * findParentsTo(g) -> a c
     * </pre>
     *
     * @param oid
     * @return
     */
    List<Organisaatio> findParentsTo(String oid);

    /**
     * Haetaan annetun parent organisaation lapset.
     *
     * @param parentId
     * @return
     */
    List<Organisaatio> findChildren(Long parentId);

    /**
     * Palautetaan oph organisaation alla olevat Ryhmä tyyppiset organisaatiot
     *
     * @param criteria hakukriteerit
     * @return
     */
    List<Organisaatio> findGroups(RyhmaCriteriaDto criteria);

    /**
     * Tarkistetaan onko annetulle ytunnukselle olemassa jo aktiivinen organisaatio.
     *
     * @param ytunnus
     * @return
     */
    boolean isYtunnusAvailable(String ytunnus);

    /**
     * Merkitään organisaatio poistetuksi ja palautetaan organisaation parent.
     * Organisaation voi poistaa vain, jos sillä ei ole lapsiorganisaatioita.
     *
     * @param oid Poistettavan organisaation oid
     * @return Poistettavan organisaation parent
     */
    Organisaatio markRemoved(String oid);

    /**
     * Finds list of oids with given query params.
     *
     * @param requireYtunnus
     * @param count
     * @param startIndex
     * @param type
     * @return
     */
    List<String> findOidsBy(Boolean requireYtunnus, int count, int startIndex, OrganisaatioTyyppi type);

    Organisaatio findByVirastoTunnus(String oid);

    Organisaatio findByYTunnus(String oid);

    Organisaatio findByOppilaitoskoodi(String oid);

    Organisaatio findByToimipistekoodi(String oid);

    /**
     * Find childers for given Organisation with OID.
     *
     * @param parentOid
     * @param myosPoistetut if true return also "removed" orgs
     * @param myosLakkautetut
     * @return
     */
    List<Organisaatio> findChildren(String parentOid, boolean myosPoistetut, boolean myosLakkautetut);

    /**
     * Useiden organisaatioiden tietojen hakeminen yhdellä kyselyllä. Ei palauta piilotettuja organisaatioita.
     * Hibernaten odotetaan lataavan laiskasti batcheissa puuttuvat tiedot.
     * @param oids Organisaatioiden oidit
     * @param excludePoistettu Jätetäänkö poistetut organisaatiot pois hausta
     * @return Oideja vastaavat organisaatiot
     * @see #findByOids(Collection, boolean, boolean)
     */
    List<Organisaatio> findByOids(Collection<String> oids, boolean excludePoistettu);

    /**
     * Useiden organisaatioiden tietojen hakeminen yhdellä kyselyllä. <b>Huom!</b> oikeus nähdä piilotetut
     * organisaatiot tarkistettava, jos piilotetut sisällytetään tuloksiin - tämä on <i>kutsujan vastuulla</i>.
     * 
     * @param oids organisatioiden OID:t
     * @param excludePoistettu jätetäänkö poistetut pois tuloksista
     * @param excludePiilotettu jätetäänkö piilotetut pois tuloksista
     * @return OID:ja vastaavat organisaatiot
     * @see #findByOids(Collection, boolean)
     */
    List<Organisaatio> findByOids(Collection<String> oids, boolean excludePoistettu, boolean excludePiilotettu);

    /***
     * Palauttaa annetun päivän jälkeen muuttuneet organisaatiot. Ei rajaa organisaatiotyypillä, sisällyttää myös
     * lakkautetut organisaatiot.
     *
     * @param lastModifiedSince päivämäärä
     * @param excludePiilotettu jätetäänkö piilotetut organisaatiot pois tuloksista
     * @return annetun päivämäärän jälkeen muuttuneet organisaatiot
     * @see #findModifiedSince(boolean, Date, List, boolean)
     */
    List<Organisaatio> findModifiedSince(
            boolean excludePiilotettu,
            Date lastModifiedSince);

    /***
     * Palauttaa annetun päivän jälkeen muuttuneet organisaatiot. Hakua voi rajata organisaatiotyypeillä tai jättää
     * (hakuhetkellä) lakkautetut organisaatiot pois tuloksista.
     *
     * @param lastModifiedSince päivämäärä
     * @param excludePiilotettu jätetäänkö piilotetut organisaatiot pois tuloksista
     * @param organizationTypes halutut organisaatiotyypit (tyhjä/null palauttaa kaikki)
     * @param excludeDiscontinued jätetäänkö (hakuhetkellä) lakkautetut pois tuloksista
     * @return annetun päivämäärän jälkeen muuttuneet organisaatiot
     */
    List<Organisaatio> findModifiedSince(
            boolean excludePiilotettu,
            Date lastModifiedSince,
            List<OrganisaatioTyyppi> organizationTypes,
            boolean excludeDiscontinued);

    /**
     * Palauttaa aktiiviset organisaatiot joille ei ole tehty tietojen tarkastusta annetulla päivämäärällä.
     *
     * @param tarkastusPvm aikaleima jonka jälkeen tarkastus tulee olla tehtynä
     * @param voimassaPvm aikaleima jolla organisaatiot halutaan olevan aktiivisia
     * @param oids organisaatiot joita haetaan
     * @param limit palautettavien rivien maksimimäärä
     * @return tarkastamattomat organisaatiot
     */
    Collection<Organisaatio> findByTarkastusPvm(Date tarkastusPvm, LocalDate voimassaPvm, Collection<String> oids, long limit);

    /**
     * Implementation of merge without flush, let hibernate decide when
     * @param org
     * @throws OptimisticLockException
     */
    void updateOrg(Organisaatio org) throws OptimisticLockException;

    void flush();

    EntityManager getJpaEntityManager();

    Collection<String> findChildOidsRecursive(ChildOidsCriteria criteria);

    List<String> findParentOidsTo(String oid);
}