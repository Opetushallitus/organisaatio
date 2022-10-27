package fi.vm.sade.organisaatio.business.impl;

import fi.vm.sade.organisaatio.SecurityAwareTestBase;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.business.OrganisaatioYtjService;
import fi.vm.sade.organisaatio.repository.OrganisaatioRepository;
import fi.vm.sade.organisaatio.model.*;
import fi.vm.sade.organisaatio.ytj.api.YTJDTOBuilder;
import fi.vm.sade.organisaatio.ytj.api.YTJKieli;
import fi.vm.sade.organisaatio.ytj.api.YTJService;
import fi.vm.sade.organisaatio.ytj.api.exception.YtjConnectionException;
import fi.vm.sade.organisaatio.ytj.mock.YTJServiceMock;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static fi.vm.sade.organisaatio.business.impl.OrganisaatioYtjServiceImpl.KIELI_KOODI_FI;
import static fi.vm.sade.organisaatio.business.impl.OrganisaatioYtjServiceImpl.KIELI_KOODI_SV;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@Transactional
@SpringBootTest
@AutoConfigureTestDatabase
public class OrganisaatioYtjServiceImplTest extends SecurityAwareTestBase {

    @TestConfiguration
    static class OrganisaatioYtjServiceImplTestConfiguration {
        @Bean
        @Primary
        public YTJService ytjService() {
            return spy(new YTJServiceMock());
        }
    }

    @Autowired
    private OrganisaatioRepository organisaatioRepository;
    @Autowired
    private OrganisaatioYtjService service;
    @Autowired
    private YTJService ytjService;

    @BeforeEach
    public void setUp() throws YtjConnectionException {
        executeSqlScript("classpath:data/basic_organisaatio_data.sql", false);
        when(ytjService.findByYTunnus(eq("1234569-5"), any(YTJKieli.class))).thenAnswer(invocation ->
                new YTJDTOBuilder(invocation.getArgument(0, String.class))
                        .kieli(invocation.getArgument(1, YTJKieli.class))
                        .build());
    }

    @AfterEach
    public void tearDown() {
        executeSqlScript("classpath:data/truncate_tables.sql", false);
    }

    @Test
    public void updateYTJDataTest() {
        service.updateYTJData(false);
        // verify that the database is updated properly
        List<String> oidList = new ArrayList<>();
        oidList.addAll(organisaatioRepository.findOidsBy(true, OrganisaatioYtjServiceImpl.SEARCH_LIMIT, 0, OrganisaatioTyyppi.KOULUTUSTOIMIJA));
        oidList.addAll(organisaatioRepository.findOidsBy(true, OrganisaatioYtjServiceImpl.SEARCH_LIMIT, 0, OrganisaatioTyyppi.TYOELAMAJARJESTO));
        oidList.addAll(organisaatioRepository.findOidsBy(true, OrganisaatioYtjServiceImpl.SEARCH_LIMIT, 0, OrganisaatioTyyppi.MUU_ORGANISAATIO));
        oidList.addAll(organisaatioRepository.findOidsBy(true, OrganisaatioYtjServiceImpl.SEARCH_LIMIT, 0, OrganisaatioTyyppi.VARHAISKASVATUKSEN_JARJESTAJA));
        List<Organisaatio> organisaatioList = organisaatioRepository.findByOidList(oidList, OrganisaatioYtjServiceImpl.SEARCH_LIMIT);

        assertEquals(4, organisaatioList.size());

        Organisaatio org = organisaatioRepository.findFirstByOid("1.2.2005.5");
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
                .returns("Säätiö", Organisaatio::getYritysmuoto)
                .extracting(organisaatio -> organisaatio.getNimi().getString("fi"),
                        organisaatio -> organisaatio.getNimi().getString("sv"),
                        organisaatio -> organisaatio.getPostiosoiteByKieli(KIELI_KOODI_SV).getOsoite(),
                        organisaatio -> organisaatio.getPostiosoiteByKieli(KIELI_KOODI_FI).getOsoite(),
                        organisaatio -> organisaatio.getEmail(KIELI_KOODI_FI).getEmail(),
                        organisaatio -> organisaatio.getWww(KIELI_KOODI_FI).getWwwOsoite(),
                        organisaatio -> organisaatio.getPuhelin(Puhelinnumero.TYYPPI_PUHELIN, KIELI_KOODI_FI).getPuhelinnumero(),
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
                        KIELI_KOODI_FI
                        );

        org = organisaatioRepository.findFirstByOid("1.2.2004.1");
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
                .returns("Yksityinen elinkeinonharjoittaja", Organisaatio::getYritysmuoto)
                .extracting(organisaatio -> organisaatio.getNimi().getString("fi"),
                        organisaatio -> organisaatio.getNimi().getString("sv"),
                        organisaatio -> organisaatio.getPostiosoiteByKieli(KIELI_KOODI_SV),
                        organisaatio -> organisaatio.getPostiosoiteByKieli(KIELI_KOODI_FI).getOsoite(),
                        organisaatio -> organisaatio.getEmail(KIELI_KOODI_FI).getEmail(),
                        organisaatio -> organisaatio.getWww(KIELI_KOODI_FI).getWwwOsoite(),
                        organisaatio -> organisaatio.getPuhelin(Puhelinnumero.TYYPPI_PUHELIN, KIELI_KOODI_FI).getPuhelinnumero(),
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
                        KIELI_KOODI_FI
                );

        org = organisaatioRepository.findFirstByOid("1.2.2004.5");
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
                .returns("Yksityinen elinkeinonharjoittaja", Organisaatio::getYritysmuoto)
                .extracting(organisaatio -> organisaatio.getNimi().getString("fi"),
                        organisaatio -> organisaatio.getNimi().getString("sv"),
                        organisaatio -> organisaatio.getPostiosoiteByKieli(KIELI_KOODI_SV).getOsoite(),
                        organisaatio -> organisaatio.getPostiosoiteByKieli(KIELI_KOODI_FI).getOsoite(),
                        organisaatio -> organisaatio.getEmail(KIELI_KOODI_SV).getEmail(),
                        organisaatio -> organisaatio.getWww(KIELI_KOODI_SV).getWwwOsoite(),
                        organisaatio -> organisaatio.getPuhelin(Puhelinnumero.TYYPPI_PUHELIN, KIELI_KOODI_SV).getPuhelinnumero(),
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
                        KIELI_KOODI_SV
                );
    }

    private void initTestData(Organisaatio org) {
        List<OrganisaatioNimi> orgSortedNimet = new ArrayList<>();
        sortOrganisaatioNimet(org, orgSortedNimet);
        List<Yhteystieto> orgSortedYhteystiedot = new ArrayList<>();
        sortOrganisaatioYhteystiedot(org, orgSortedYhteystiedot);
    }

    private void sortOrganisaatioYhteystiedot(Organisaatio org, List<Yhteystieto> orgSortedYhteystiedot) {
        orgSortedYhteystiedot.addAll(org.getYhteystiedot());
        orgSortedYhteystiedot.sort(Comparator.comparing(BaseEntity::getId));
    }

    private void sortOrganisaatioNimet(Organisaatio org, List<OrganisaatioNimi> orgNimet) {
        orgNimet.addAll(org.getNimet());
        orgNimet.sort(Comparator.comparing(OrganisaatioNimi::getAlkuPvm));
    }

}
