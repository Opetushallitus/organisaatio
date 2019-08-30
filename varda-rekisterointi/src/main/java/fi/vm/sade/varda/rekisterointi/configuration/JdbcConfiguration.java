package fi.vm.sade.varda.rekisterointi.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.varda.rekisterointi.converter.ArrayNodeConverter;
import fi.vm.sade.varda.rekisterointi.converter.JsonNodeWriter;
import fi.vm.sade.varda.rekisterointi.converter.ObjectNodeReader;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;

import static java.util.Arrays.asList;

@Configuration
public class JdbcConfiguration extends AbstractJdbcConfiguration {

    private final ObjectMapper objectMapper;

    public JdbcConfiguration(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public JdbcCustomConversions jdbcCustomConversions() {
        return new JdbcCustomConversions(asList(
                new JsonNodeWriter(objectMapper.writer()),
                new ObjectNodeReader(objectMapper.reader()),
                new ArrayNodeConverter(objectMapper.reader())));
    }

}
