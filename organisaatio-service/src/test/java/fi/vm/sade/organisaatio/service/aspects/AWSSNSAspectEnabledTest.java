package fi.vm.sade.organisaatio.service.aspects;

import com.amazonaws.http.HttpResponse;
import com.amazonaws.http.SdkHttpMetadata;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishResult;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.repository.OrganisaatioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
@ExtendWith(SpringExtension.class)
@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest
@ActiveProfiles("sns")
class AWSSNSAspectEnabledTest {
    @MockBean
    AmazonSNSClient amazonSNSClient;
    @Autowired
    OrganisaatioRepository organisaatioRepository;

    @Test
    void testEnabledSNS(CapturedOutput capturedOutput) {
        assertTrue(capturedOutput.getOut().contains("Initialized AWSSNSLakkautusTopic true foo"));
        Organisaatio entity = new Organisaatio();
        entity.setOid("1.2.3.4.5");
        Organisaatio entity2 = organisaatioRepository.saveAndFlush(entity);
        entity2.setLakkautusPvm(Date.from(Instant.now()));
        PublishResult result = new PublishResult();
        HttpResponse http = mock(HttpResponse.class);
        when(http.getStatusCode()).thenReturn(200);
        when(http.getHeaders()).thenReturn(Map.of());
        when(http.getAllHeaders()).thenReturn(Map.of());
        result.setSdkHttpMetadata(SdkHttpMetadata.from(http));
        result.setMessageId("testmessage");
        when(amazonSNSClient.publish(any())).thenReturn(result);
        organisaatioRepository.saveAndFlush(entity2);
        assertTrue(capturedOutput.getOut().contains("testmessage message sent with status 200"));
    }

}