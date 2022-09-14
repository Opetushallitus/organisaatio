package fi.vm.sade.organisaatio.config;

import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.AmazonSNSException;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class AWSSNSLakkautusTopic {
    private final AmazonSNSClient amazonSNSClient;
    private final String topicArn;
    private final boolean enabled;
    private final ObjectMapper objectMapper;

    public AWSSNSLakkautusTopic(AWSSNSClientConfiguration configuration, AmazonSNSClient amazonSNSClient, ObjectMapper objectMapper) {
        this.topicArn = configuration.getLakkautusTopicArn();
        this.enabled = configuration.isEnabled();
        this.amazonSNSClient = amazonSNSClient;
        this.objectMapper = objectMapper;
        log.info("Initialized AWSSNSLakkautusTopic {} {}", enabled, topicArn);
    }

    public void pubTopic(Map message) throws JsonProcessingException {
        if (enabled) {
            try {
                PublishResult result = amazonSNSClient.publish(new PublishRequest()
                        .withTopicArn(topicArn)
                        .withMessage(objectMapper.writeValueAsString(message)));
                log.debug("{} Message sent. Status is {}",
                        result.getMessageId(),
                        result.getSdkHttpMetadata().getHttpStatusCode());
            } catch (AmazonSNSException e) {
                log.error(e.getErrorMessage());
            }
        } else {
            log.info("SNS Lakkautus aspect disabled {}", message);
        }
    }
}
