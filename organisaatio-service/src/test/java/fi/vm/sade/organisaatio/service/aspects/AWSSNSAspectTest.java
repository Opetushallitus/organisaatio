package fi.vm.sade.organisaatio.service.aspects;

import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.repository.OrganisaatioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest
class AWSSNSAspectTest {
    @Autowired
    OrganisaatioRepository organisaatioRepository;

    @Test
    void testDisabledSNS(CapturedOutput capturedOutput) {
        assertTrue(capturedOutput.getOut().contains("Initialized AWSSNSLakkautusTopic false undefined"));
        Organisaatio entity = new Organisaatio();
        entity.setOid("1.2.3.4.5");
        Organisaatio entity2 = organisaatioRepository.saveAndFlush(entity);
        entity2.setLakkautusPvm(Date.from(Instant.now()));
        organisaatioRepository.saveAndFlush(entity2);
        assertTrue(capturedOutput.getOut().contains("SNS Lakkautus aspect disabled {oid=1.2.3.4.5}"));
    }
}