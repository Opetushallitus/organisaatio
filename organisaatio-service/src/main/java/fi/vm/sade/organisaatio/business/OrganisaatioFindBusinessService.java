package fi.vm.sade.organisaatio.business;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;
import fi.vm.sade.organisaatio.dto.ChildOidsCriteria;
import fi.vm.sade.organisaatio.dto.v3.OrganisaatioRDTOV3;
import fi.vm.sade.organisaatio.dto.v4.OrganisaatioHakutulosV4;
import fi.vm.sade.organisaatio.dto.v4.OrganisaatioRDTOV4;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.OrganisaatioSuhde;
import fi.vm.sade.organisaatio.resource.dto.RyhmaCriteriaDtoV3;
import fi.vm.sade.organisaatio.service.search.SearchConfig;
import fi.vm.sade.organisaatio.service.search.SearchCriteria;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

public interface OrganisaatioFindBusinessService {

    /**
     * Yleiskäyttöinen hakutoiminto.
     *
     * @param criteria hakukriteerit
     * @param config haun asetukset
     * @return organisaatioiden perustiedot
     */
    List<OrganisaatioPerustieto> findBy(SearchCriteria criteria, SearchConfig config);

    /**
     * Hakee v4 rajapinnan mukaiset organisaatiotiedot oidien perusteella
     * @param oids Organisaatioden oidit
     * @return V4 rajapinnan käyttämä organisaatiolista
     */
    List<OrganisaatioRDTOV4> findByOidsV4(Collection<String> oids);

    /**
     * Wrappaa findById(String id) mutta palauttaa v4 apin tuloksen
     * @param id findById(String id) vastaava hakuavain
     * @param includeImage Haetaanko kuvat
     * @return Organisaatio dto muodossa OrganisaatioRDTOV4
     */
    OrganisaatioRDTOV4 findByIdV4(String id, boolean includeImage);

    /**
     * Hakee organisaation ID:n perusteella ja palauttaa tämän lapsiorganisaatiot.
     * @param id Käyttää findById(String id)
     * @param includeImage Haetaanko kuvat
     * @return Organisaation aliorganisaatiot muodossa OrganisaatioRDTOV4
     */
    List<OrganisaatioRDTOV4> findChildrenById(String id, boolean includeImage);

    /**
     * Idllä haku (Id voi olla oid, y-tunnus, virastotunnus, oppilaitoskoodi, toimipistekoodi)
     *
     * Search order
     *  1. OID
     *  2. Y-TUNNUS
     *  3. VIRASTOTUNNUS
     *  4. OPPILAITOSKOODI
     *  5. TOIMIPISTEKOODI
     *
     * @param id Id, joka voi olla: oid, y-tunnus, virastotunnus, oppilaitoskoodi tai toimipistekoodi
     * @return Organisaatio
     */
    public Organisaatio findById(String id);

    public List<OrganisaatioRDTOV3> findByOids(Collection<String> oids);

    /**
     * @param kieliList kielirajaus kielivalikoima-koodiston koodiUreja: ["kielivalikoima_en", "kielivalikoima_sv"]
     * @param kuntaList kuntarajaus kunta-koodiston koodiUreja: ["kunta_407", "kunta_604"]
     * @param oppilaitostyyppiList oppilaitostyyppirajaus oppilaitostyyppi-koodiston koodiUreja: ["oppilaitostyyppi_19", "oppilaitostyyppi_91"]
     * @param vuosiluokkaList vuosiluokkarajaus vuosiluokat-koodiston koodiUreja: ["vuosiluokat_1","vuosiluokat_2"]
     * @param ytunnusList y-tunnusrajaus: ["0147510-4", "0203797-4"]
     * @param oidList
     * @param limit
     * @return
     */
    public Set<Organisaatio> findBySearchCriteria(
            Set<String> kieliList,
            Set<String> kuntaList,
            Set<String> oppilaitostyyppiList,
            Set<String> vuosiluokkaList,
            Set<String> ytunnusList,
            Set<String> oidList,
            int limit);

    /**
     * Haetaan kannasta kaikki ryhmät.
     * @param criteria hakukriteerit
     * @return Ryhmät
     */
    public List<Organisaatio> findGroups(RyhmaCriteriaDtoV3 criteria);

    /**
     * Haetaan kannasta organiasaatioiden oidit listana.
     *
     * @param count       Lukumäärä
     * @param startIndex  Aloitusindeksi
     * @param type        Haettava organisaatiotyyppi
     * @return Organisaatioiden oid:t
     */
    public List<String> findOidsBy(int count, int startIndex, OrganisaatioTyyppi type);

    /**
     * Haetaan kannasta kaikki organisaatioliitokset.
     *
     * @param date
     * @return Organisaatioiden liitokset.
     */
    public List<OrganisaatioSuhde> findLiitokset(Date date);

    Collection<String> findChildOidsRecursive(ChildOidsCriteria criteria);

    /**
     * Hakee muuttuneet organisaatiot
     * @param lastModifiedSince Päivämäärä jonka jälkeen muuttuneita haetaan
     * @param organizationTypes Halutut organisaatiotyypit (tyhjä/null ei rajaa tyypillä)
     * @param excludeDiscontinued Rajataanko lakkautetut pois tuloksista
     * @return Muuttuneet organisaatiot muodossa OrganisaatioRDTOV4
     */
    List<OrganisaatioRDTOV4> haeMuutetut(
            LocalDateTime lastModifiedSince,
            List<OrganisaatioTyyppi> organizationTypes,
            boolean excludeDiscontinued);

    OrganisaatioHakutulosV4  findDescendants(String oid);
}
