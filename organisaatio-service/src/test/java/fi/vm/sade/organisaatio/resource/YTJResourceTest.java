package fi.vm.sade.organisaatio.resource;

import fi.vm.sade.organisaatio.SecurityAwareTestBase;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.business.OrganisaatioBusinessService;
import fi.vm.sade.organisaatio.dto.VarhaiskasvatuksenToimipaikkaTiedotDto;
import fi.vm.sade.organisaatio.dto.v4.OrganisaatioRDTOV4;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(locations = {"classpath:spring/test-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class YTJResourceTest extends SecurityAwareTestBase {

    @Autowired
    private YTJResource ytjResource;

    @Autowired
    private OrganisaatioBusinessService organisaatioBusinessService;

    @Before
    public void setup() {
        executeSqlScript("data/root_organisaatio_data.sql", false);
    }

    @After
    public void cleanup() {
        executeSqlScript("data/truncate_tables.sql", false);
    }

    @Test
    public void findByYTunnusV4() {
        OrganisaatioRDTOV4 varhaiskasvatuksenJarjestaja = ytjResource.findByYTunnusV4("2255802-1");
        // lisätään pakolliset tiedot ennen tallennusta
        varhaiskasvatuksenJarjestaja.setTyypit(singleton(OrganisaatioTyyppi.VARHAISKASVATUKSEN_JARJESTAJA.koodiValue()));

        varhaiskasvatuksenJarjestaja = organisaatioBusinessService.save(varhaiskasvatuksenJarjestaja, false).getOrganisaatio();

        assertThat(varhaiskasvatuksenJarjestaja).returns("1.2.246.562.24.00000000001", OrganisaatioRDTOV4::getParentOid);

        OrganisaatioRDTOV4 varhaiskasvatuksenToimipaikka = ytjResource.findByYTunnusV4("2255802-1");
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

        varhaiskasvatuksenToimipaikka = organisaatioBusinessService.save(varhaiskasvatuksenToimipaikka, false).getOrganisaatio();

        assertThat(varhaiskasvatuksenToimipaikka).returns(varhaiskasvatuksenJarjestaja.getOid(), OrganisaatioRDTOV4::getParentOid);
    }

}
