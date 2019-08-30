package fi.vm.sade.varda.rekisterointi.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.postgresql.util.PGobject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import java.sql.SQLException;

@WritingConverter
public class JsonNodeWriter implements Converter<JsonNode, PGobject> {

    private final ObjectWriter objectWriter;

    public JsonNodeWriter(ObjectWriter objectWriter) {
        this.objectWriter = objectWriter;
    }

    @Override
    public PGobject convert(JsonNode source) {
        PGobject destination = new PGobject();
        destination.setType("jsonb");
        try {
            destination.setValue(objectWriter.writeValueAsString(source));
        } catch (SQLException | JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
        return destination;
    }
}
