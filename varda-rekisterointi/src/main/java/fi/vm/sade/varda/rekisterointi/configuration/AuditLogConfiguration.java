package fi.vm.sade.varda.rekisterointi.configuration;

import fi.vm.sade.auditlog.ApplicationType;
import fi.vm.sade.auditlog.Audit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuditLogConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditLogConfiguration.class);

    @Bean
    public Audit audit() {
        return new Audit(LOGGER::info, "varda-rekisterointi", ApplicationType.VIRKAILIJA);
    }

}
