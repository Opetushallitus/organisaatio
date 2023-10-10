package fi.vm.sade.organisaatio.resource;

import fi.vm.sade.organisaatio.SecurityAwareTestBase;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.business.OrganisaatioBusinessService;
import fi.vm.sade.organisaatio.dto.VarhaiskasvatuksenToimipaikkaTiedotDto;
import fi.vm.sade.organisaatio.dto.v4.OrganisaatioRDTOV4;
import fi.vm.sade.organisaatio.ytj.api.YTJDTO;
import fi.vm.sade.organisaatio.ytj.api.YTJService;
import fi.vm.sade.organisaatio.ytj.mock.YTJServiceMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;

@Transactional
@SpringBootTest
@Sql("/data/truncate_tables.sql")
@Sql("/data/basic_organisaatio_data.sql")
class YTJResourceTest extends SecurityAwareTestBase {

    @TestConfiguration
    static class TestContextConfiguration {

        @Bean
        @Primary
        public YTJService ytjService() {
            return spy(new YTJServiceMock());
        }

    }

    @Autowired
    private YTJResource ytjResource;

    @Autowired
    private OrganisaatioBusinessService organisaatioBusinessService;

    @Test
    void findByYTunnusV4() {
        OrganisaatioRDTOV4 varhaiskasvatuksenJarjestaja = ytjResource.findByYTunnusV4("8817238-3");
        // lisätään pakolliset tiedot ennen tallennusta
        varhaiskasvatuksenJarjestaja.setTyypit(singleton(OrganisaatioTyyppi.VARHAISKASVATUKSEN_JARJESTAJA.koodiValue()));

        varhaiskasvatuksenJarjestaja = organisaatioBusinessService.saveOrUpdate(varhaiskasvatuksenJarjestaja).getOrganisaatio();

        assertThat(varhaiskasvatuksenJarjestaja).returns("1.2.246.562.24.00000000001", OrganisaatioRDTOV4::getParentOid);

        OrganisaatioRDTOV4 varhaiskasvatuksenToimipaikka = ytjResource.findByYTunnusV4("8817238-3");
        // lisätään pakolliset tiedot ennen tallennusta
        varhaiskasvatuksenToimipaikka.setYTunnus(null);
        varhaiskasvatuksenToimipaikka.setParentOid(varhaiskasvatuksenJarjestaja.getOid());
        varhaiskasvatuksenToimipaikka.setTyypit(singleton(OrganisaatioTyyppi.VARHAISKASVATUKSEN_TOIMIPAIKKA.koodiValue()));
        VarhaiskasvatuksenToimipaikkaTiedotDto tiedot = new VarhaiskasvatuksenToimipaikkaTiedotDto();
        tiedot.setPaikkojenLukumaara(10L);
        tiedot.setToimintamuoto("vardatoimintamuoto_tm02");
        tiedot.setKasvatusopillinenJarjestelma("vardakasvatusopillinenjarjestelma_kj99");
        tiedot.setVarhaiskasvatuksenJarjestamismuodot(singleton("vardajarjestamismuoto_jm03"));
        varhaiskasvatuksenToimipaikka.setVarhaiskasvatuksenToimipaikkaTiedot(tiedot);

        varhaiskasvatuksenToimipaikka = organisaatioBusinessService.saveOrUpdate(varhaiskasvatuksenToimipaikka).getOrganisaatio();

        assertThat(varhaiskasvatuksenToimipaikka).returns(varhaiskasvatuksenJarjestaja.getOid(), OrganisaatioRDTOV4::getParentOid);
    }

    @Test
    void findByYTunnus() {
        OrganisaatioResourceException thrown = Assertions.assertThrows(OrganisaatioResourceException.class, () -> {
            ytjResource.findByYTunnus("");
        });
    }

    @Test
    void findByYNimi1() {
        OrganisaatioResourceException thrown = Assertions.assertThrows(OrganisaatioResourceException.class, () -> {
            ytjResource.findByYNimi("-");
        });
    }

    @Test
    void findByYNimi2() {
        List<YTJDTO> list = ytjResource.findByYNimi("FCG Finnish Consulting Group Oy");
        assertThat(list.size()).isEqualTo(1);
    }

    @Test
    void findByYNimi3() {
        OrganisaatioResourceException thrown = Assertions.assertThrows(OrganisaatioResourceException.class, () -> {
            ytjResource.findByYNimi("x");
        });
    }
}
