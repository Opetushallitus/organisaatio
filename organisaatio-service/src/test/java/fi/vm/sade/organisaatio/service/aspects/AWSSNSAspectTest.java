package fi.vm.sade.organisaatio.service.aspects;

import fi.vm.sade.organisaatio.model.MonikielinenTeksti;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.repository.OrganisaatioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest
@Sql("/data/truncate_tables.sql")
class AWSSNSAspectTest {
    @Autowired
    OrganisaatioRepository organisaatioRepository;

    @Test
    void testDisabledSNS(CapturedOutput capturedOutput) {
        Organisaatio entity = new Organisaatio();
        entity.setOid("1.2.3.4.5");
        entity.setNimi(new MonikielinenTeksti());
        Organisaatio entity2 = organisaatioRepository.saveAndFlush(entity);
        entity2.setLakkautusPvm(Date.from(Instant.now()));
        organisaatioRepository.saveAndFlush(entity2);
        assertTrue(capturedOutput.getOut().contains("SNS Lakkautus aspect disabled {oid=1.2.3.4.5}"));
    }
}