package fi.vm.sade.organisaatio.business.impl;

import fi.vm.sade.organisaatio.SecurityAwareTestBase;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.business.OrganisaatioYtjService;
import fi.vm.sade.organisaatio.dao.OrganisaatioDAO;
import fi.vm.sade.organisaatio.model.*;
import org.assertj.core.groups.Tuple;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(locations = {"classpath:spring/test-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class OrganisaatioYtjServiceImplTest extends SecurityAwareTestBase {

    @Autowired
    private OrganisaatioDAO organisaatioDAO;
    @Autowired
    private OrganisaatioYtjService service;

    private List<OrganisaatioNimi> orgSortedNimet;
    private List<Yhteystieto> orgSortedYhteystiedot;
    private List<Organisaatio> organisaatioList;

    @Before
    public void setUp() {
        executeSqlScript("data/basic_organisaatio_data.sql", false);
    }

    @After
    public void tearDown() {
        executeSqlScript("data/truncate_tables.sql", false);
    }

    @Test
    public void updateYTJDataTest() {
        service.updateYTJData(false);
        // verify that the database is updated properly
        List<String> oidList = new ArrayList<>();
        oidList.addAll(organisaatioDAO.findOidsBy(true, OrganisaatioYtjServiceImpl.SEARCH_LIMIT, 0, OrganisaatioTyyppi.KOULUTUSTOIMIJA));
        oidList.addAll(organisaatioDAO.findOidsBy(true, OrganisaatioYtjServiceImpl.SEARCH_LIMIT, 0, OrganisaatioTyyppi.TYOELAMAJARJESTO));
        oidList.addAll(organisaatioDAO.findOidsBy(true, OrganisaatioYtjServiceImpl.SEARCH_LIMIT, 0, OrganisaatioTyyppi.MUU_ORGANISAATIO));
        organisaatioList = organisaatioDAO.findByOidList(oidList, OrganisaatioYtjServiceImpl.SEARCH_LIMIT);

        Assert.assertEquals(3, organisaatioList.size());

        Organisaatio org = organisaatioDAO.findByOid("1.2.2005.5");
        initTestData(org);
        // Case: Has sv name; gets new fi name from YTJ, no puhelin, www, alkupvm updated
        // name history not updated
        assertThat(org.getNimet())
                .extracting(nimi -> nimi.getNimi().getString("fi"), nimi -> nimi.getNimi().getString("sv"), nimi -> nimi.getAlkuPvm().getTime())
                .containsExactly(Tuple.tuple("Helsingin yliopistomuseon säätiö", "node231 foo bar", 923864400000L));
        assertThat(org.getYhteystiedot().stream().filter(Osoite.class::isInstance).map(Osoite.class::cast))
                .extracting(Osoite::getPostinumero)
                .containsExactlyInAnyOrder("posti_00100", "posti_00100");
        assertThat(org)
                .extracting(organisaatio -> organisaatio.getNimi().getString("fi"),
                        organisaatio -> organisaatio.getNimi().getString("sv"),
                        organisaatio -> organisaatio.getPostiosoiteByKieli(OrganisaatioYtjServiceImpl.KIELI_KOODI_SV).getOsoite(),
                        organisaatio -> organisaatio.getPostiosoiteByKieli(OrganisaatioYtjServiceImpl.KIELI_KOODI_FI).getOsoite(),
                        organisaatio -> organisaatio.getEmail().getEmail(),
                        organisaatio -> organisaatio.getWww().getWwwOsoite(),
                        organisaatio -> organisaatio.getPuhelin(Puhelinnumero.TYYPPI_PUHELIN).getPuhelinnumero(),
                        Organisaatio::getKielet,
                        organisaatio -> organisaatio.getAlkuPvm().getTime(),
                        Organisaatio::getYtjKieli
                        )
                .containsExactly("Helsingin yliopistomuseon säätiö",
                        "node231 foo bar",
                        "Mannerheimintie 2",
                        "Tie 1",
                        "example@example.com",
                        "http://www.ytj.fi",
                        "0100000211",
                        Collections.singleton(OrganisaatioYtjServiceImpl.ORG_KIELI_KOODI_FI),
                        915141600000L,// original 2006-06-29, from YTJ 01.01.1999
                        OrganisaatioYtjServiceImpl.KIELI_KOODI_FI
                        );

        org = organisaatioDAO.findByOid("1.2.2004.1");
        initTestData(org);
        // Case: Has fi and sv name, puhelin, www, alkupvm; gets fi name updated from YTJ
        // new entry to name history
        assertThat(org.getNimet())
                .extracting(nimi -> nimi.getNimi().getString("fi"), nimi -> nimi.getNimi().getString("sv"), nimi -> nimi.getAlkuPvm().getTime())
                .containsExactly(Tuple.tuple("root test koulutustoimija", "root test utbildningsoperator", -7200000L),
                        Tuple.tuple("Katva Consulting", "root test utbildningsoperator", 918597600000L));
        assertThat(org.getYhteystiedot().stream().filter(Osoite.class::isInstance).map(Osoite.class::cast))
                .extracting(Osoite::getPostinumero)
                .containsExactlyInAnyOrder("posti_00100", "posti_00100");
        assertThat(org)
                .extracting(organisaatio -> organisaatio.getNimi().getString("fi"),
                        organisaatio -> organisaatio.getNimi().getString("sv"),
                        organisaatio -> organisaatio.getPostiosoiteByKieli(OrganisaatioYtjServiceImpl.KIELI_KOODI_SV),
                        organisaatio -> organisaatio.getPostiosoiteByKieli(OrganisaatioYtjServiceImpl.KIELI_KOODI_FI).getOsoite(),
                        organisaatio -> organisaatio.getEmail().getEmail(),
                        organisaatio -> organisaatio.getWww().getWwwOsoite(),
                        organisaatio -> organisaatio.getPuhelin(Puhelinnumero.TYYPPI_PUHELIN).getPuhelinnumero(),
                        Organisaatio::getKielet,
                        organisaatio -> organisaatio.getAlkuPvm().getTime(),
                        Organisaatio::getYtjKieli
                )
                .containsExactly("Katva Consulting",
                        "root test utbildningsoperator",
                        null,
                        "Ygankuja 1",
                        "example@example.com",
                        "http://www.oph.fi",
                        "12345",
                        Collections.singleton(OrganisaatioYtjServiceImpl.ORG_KIELI_KOODI_FI),
                        1298844000000L,// original 2004-08-08, from YTJ 2011-02-28
                        OrganisaatioYtjServiceImpl.KIELI_KOODI_FI
                );

        org = organisaatioDAO.findByOid("1.2.2004.5");
        initTestData(org);
        // Case: Has fi name, puhelin, www, alkupvm; gets new sv name and updated puhelin, www from YTJ;
        // alkupvm not updated since ytj invalid data
        // new sv address added
        assertThat(org.getNimet())
                .extracting(nimi -> nimi.getNimi().getString("fi"), nimi -> nimi.getNimi().getString("sv"), nimi -> nimi.getAlkuPvm().getTime())
                .containsExactly(Tuple.tuple("root2 test2 koulutustoimija2", "Ruotsalainen & koulutustoimija", 921103200000L));
        assertThat(org.getYhteystiedot().stream().filter(Osoite.class::isInstance).map(Osoite.class::cast))
                .extracting(Osoite::getPostinumero)
                .containsExactlyInAnyOrder("posti_00100", "posti_00100", "posti_00100");
        assertThat(org)
                .extracting(organisaatio -> organisaatio.getNimi().getString("fi"),
                        organisaatio -> organisaatio.getNimi().getString("sv"),
                        organisaatio -> organisaatio.getPostiosoiteByKieli(OrganisaatioYtjServiceImpl.KIELI_KOODI_SV).getOsoite(),
                        organisaatio -> organisaatio.getPostiosoiteByKieli(OrganisaatioYtjServiceImpl.KIELI_KOODI_FI).getOsoite(),
                        organisaatio -> organisaatio.getEmail().getEmail(),
                        organisaatio -> organisaatio.getWww().getWwwOsoite(),
                        organisaatio -> organisaatio.getPuhelin(Puhelinnumero.TYYPPI_PUHELIN).getPuhelinnumero(),
                        Organisaatio::getKielet,
                        organisaatio -> organisaatio.getAlkuPvm().getTime(),
                        Organisaatio::getYtjKieli
                )
                .containsExactly("root2 test2 koulutustoimija2",
                        "Ruotsalainen & koulutustoimija",
                        "Svenska gatan 1",
                        "Mannerheimintie 1",
                        "example@example.com",
                        "http://www.ytj.sv",
                        "0100000210",
                        Collections.singleton(OrganisaatioYtjServiceImpl.ORG_KIELI_KOODI_SV),
                        1298844000000L,// original 2006-06-29, from YTJ 2011-02-28
                        OrganisaatioYtjServiceImpl.KIELI_KOODI_SV
                );
    }

    private void initTestData(Organisaatio org) {
        orgSortedNimet = new ArrayList<>();
        sortOrganisaatioNimet(org, orgSortedNimet);
        orgSortedYhteystiedot = new ArrayList<>();
        sortOrganisaatioYhteystiedot(org, orgSortedYhteystiedot);
    }

    private void sortOrganisaatioYhteystiedot(Organisaatio org, List<Yhteystieto> orgSortedYhteystiedot) {
        for(Yhteystieto yhteystieto : org.getYhteystiedot()) {
            orgSortedYhteystiedot.add(yhteystieto);
        }
        Collections.sort(orgSortedYhteystiedot, new Comparator<Yhteystieto>() {
            @Override
            public int compare(Yhteystieto o1, Yhteystieto o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });
    }

    private void sortOrganisaatioNimet(Organisaatio org, List<OrganisaatioNimi> orgNimet) {
        for(OrganisaatioNimi orgNimi : org.getNimet()) {
            orgNimet.add(orgNimi);
        }
        Collections.sort(orgNimet, new Comparator<OrganisaatioNimi>() {
            @Override
            public int compare(OrganisaatioNimi o1, OrganisaatioNimi o2) {
                return o1.getAlkuPvm().compareTo(o2.getAlkuPvm());
            }
        });
    }

}
