package fi.vm.sade.varda.rekisterointi.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import org.postgresql.util.PGobject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.io.IOException;

@ReadingConverter
public abstract class AbstractJsonNodeReader<T extends JsonNode> implements Converter<PGobject, T> {

    private final ObjectReader objectReader;

    public AbstractJsonNodeReader(ObjectReader objectReader) {
        this.objectReader = objectReader;
    }

    @Override
    public final T convert(PGobject source) {
        if (!"jsonb".equals(source.getType())) {
            throw new IllegalArgumentException("Column type is not jsonb, is: " + source.getType());
        }
        try {
            return cast(objectReader.readTree(source.getValue()));
        } catch (ClassCastException | IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    protected abstract T cast(JsonNode jsonNode);

}
