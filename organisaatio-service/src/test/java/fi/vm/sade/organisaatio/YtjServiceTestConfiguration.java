package fi.vm.sade.organisaatio;

import fi.vm.sade.organisaatio.ytj.api.YTJService;
import fi.vm.sade.organisaatio.ytj.mock.YTJServiceMock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.mockito.Mockito.spy;

@Configuration
public class YtjServiceTestConfiguration {

    @Bean
    public YTJService ytjService() {
        return spy(new YTJServiceMock());
    }

}
