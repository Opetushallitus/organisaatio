package fi.vm.sade.organisaatio.business.impl;

import fi.vm.sade.organisaatio.business.OrganisaatioViestinta;
import fi.vm.sade.organisaatio.business.OrganisaatioYtjService;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.YtjPaivitysLoki;
import fi.vm.sade.organisaatio.repository.OrganisaatioRepository;
import fi.vm.sade.organisaatio.repository.YtjPaivitysLokiRepository;
import fi.vm.sade.organisaatio.resource.YTJResource;
import fi.vm.sade.organisaatio.ytj.api.YTJDTO;
import fi.vm.sade.organisaatio.ytj.api.YTJOsoiteDTO;
import fi.ytj.YTunnusDTO;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Sql({"/data/truncate_tables.sql"})
@Sql({"/data/basic_organisaatio_data.sql"})
class OrganisaatioYtjServiceImplPersistanceTest {
    @MockBean
    private OrganisaatioViestinta organisaatioViestinta;

    @MockBean
    private YTJResource ytjResource;
    @Autowired
    private OrganisaatioYtjService organisaatioYtjService;
    @Autowired
    private OrganisaatioRepository organisaatioRepository;
    @Autowired
    private YtjPaivitysLokiRepository ytjPaivitysLokiRepository;

    @Test
    void testPersistingChange() {

        Organisaatio foo = organisaatioRepository.findFirstByOid("1.2.2004.1");
        assertNull(foo.getYtjPaivitysPvm());
        assertEquals("oy", foo.getYritysmuoto());

        when(ytjResource.doYtjMassSearch(any())).thenReturn(List.of(getDto("2255802-1", "Suomi")));
        organisaatioYtjService.updateYTJData(false);
        verify(organisaatioViestinta).sendPaivitysLokiViestintaEmail(any());

        Organisaatio b = organisaatioRepository.findFirstByOid("1.2.2004.1");
        assertNotNull(b.getYtjPaivitysPvm());
        assertEquals("Osakeyhtiö", b.getYritysmuoto());

        List<YtjPaivitysLoki> ytjLoki = ytjPaivitysLokiRepository.findLatest(1000);
        assertEquals(1, ytjLoki.size());
        YtjPaivitysLoki loki = ytjLoki.get(0);
        assertEquals(0, loki.getYtjVirheet().size());
        assertEquals(1, loki.getPaivitetytLkm());
    }

    @Test
    void testPersistingChangeOneFailure() {

        Organisaatio foo = organisaatioRepository.findFirstByOid("1.2.2004.1");
        assertNull(foo.getYtjPaivitysPvm());
        assertEquals("oy", foo.getYritysmuoto());


        when(ytjResource.doYtjMassSearch(any())).thenReturn(List.of(getDto("2255802-1", "Suomi"), getDto("1234569-5", null)));
        organisaatioYtjService.updateYTJData(false);
        verify(organisaatioViestinta).sendPaivitysLokiViestintaEmail(any());

        Organisaatio a = organisaatioRepository.findFirstByOid("1.2.2004.1");
        assertNotNull(a.getYtjPaivitysPvm());
        assertEquals("Osakeyhtiö", a.getYritysmuoto());

        Organisaatio b = organisaatioRepository.findFirstByOid("1.2.8000.1");
        assertNull(b.getYtjPaivitysPvm());
        assertEquals("oy", b.getYritysmuoto());

        List<YtjPaivitysLoki> ytjLoki = ytjPaivitysLokiRepository.findLatest(1000);
        assertEquals(1, ytjLoki.size());
        YtjPaivitysLoki loki = ytjLoki.get(0);
        assertEquals(1, loki.getYtjVirheet().size());
        assertEquals("ilmoitukset.log.virhe.kieli.puuttuu", loki.getYtjVirheet().get(0).getVirheviesti());
        assertEquals(1, loki.getPaivitetytLkm());
    }

    private static YTJDTO getDto(String ytunnus, String kieli) {
        YTunnusDTO ytdto = new YTunnusDTO();
        ytdto.setYritysLopetettu(false);
        YTJOsoiteDTO odto = new YTJOsoiteDTO();
        odto.setPostinumero("99999");
        odto.setKatu("Foo street");
        odto.setToimipaikka("Rauma");
        YTJDTO dto = new YTJDTO();
        dto.setYtunnus(ytunnus);
        dto.setKotiPaikka("Rauma");
        dto.setYritysTunnus(ytdto);
        dto.setYrityksenKieli(kieli);
        dto.setPostiOsoite(odto);
        dto.setYritysmuoto("Osakeyhtiö");
        dto.setNimi("Makkara");
        dto.setSvNimi("Korv");
        dto.setAloitusPvm("01.01.2022");
        return dto;
    }

}
