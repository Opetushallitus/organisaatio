package fi.vm.sade.organisaatio.config;

import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.AmazonSNSException;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AWSSNSLakkautusTopic {
    private final AmazonSNSClient amazonSNSClient;
    private final String topicArn;
    private final boolean enabled;

    public AWSSNSLakkautusTopic(AWSSNSClientConfiguration configuration, AmazonSNSClient amazonSNSClient) {
        this.topicArn = configuration.getTopicArn();
        this.enabled = configuration.isEnabled();
        this.amazonSNSClient = amazonSNSClient;
    }

    public void pubTopic(String message) {
        if (enabled) {
            try {
                PublishResult result = amazonSNSClient.publish(new PublishRequest()
                        .withTopicArn(topicArn)
                        .withMessage(message));
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
