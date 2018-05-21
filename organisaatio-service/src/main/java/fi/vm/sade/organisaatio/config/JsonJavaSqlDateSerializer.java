package fi.vm.sade.organisaatio.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.Timestamp;

@Component
public class JsonJavaSqlDateSerializer extends JsonSerializer<Timestamp> {
    @Override
    public void serialize(Timestamp serializable, JsonGenerator jsonGenerator, SerializerProvider arg2)
            throws IOException, JsonProcessingException {
        jsonGenerator.writeNumber(serializable.getTime());
    }

}
