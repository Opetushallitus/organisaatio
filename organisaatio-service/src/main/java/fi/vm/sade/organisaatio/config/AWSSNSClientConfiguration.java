package fi.vm.sade.organisaatio.config;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
@Getter
@Setter
@ConfigurationProperties(prefix = "aws.sns")
public class AWSSNSClientConfiguration {
    private String topicArn;
    private boolean enabled;

    @Bean
    AWSCredentialsProvider getAwsCredentialsProvider() {
        return DefaultAWSCredentialsProviderChain.getInstance();
    }

    @Bean
    public AmazonSNSClient amazonSNSClient(AWSCredentialsProvider credentialsProvider) {
        return (AmazonSNSClient) AmazonSNSClientBuilder.standard().withRegion(Regions.EU_WEST_1)
                .withCredentials(credentialsProvider)
                .build();
    }


}
