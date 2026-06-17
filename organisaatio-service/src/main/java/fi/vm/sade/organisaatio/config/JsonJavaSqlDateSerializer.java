package fi.vm.sade.organisaatio.config;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

import org.springframework.stereotype.Component;

import java.sql.Timestamp;

@Component
public class JsonJavaSqlDateSerializer extends StdSerializer<Timestamp> {

    public JsonJavaSqlDateSerializer() {
        super(Timestamp.class);
    }

    @Override
    public void serialize(Timestamp serializable, JsonGenerator jsonGenerator, SerializationContext serializationContext)
            throws JacksonException {
        jsonGenerator.writeNumber(serializable.getTime());
    }
}
