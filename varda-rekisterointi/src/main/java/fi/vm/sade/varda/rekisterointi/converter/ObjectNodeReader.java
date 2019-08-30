package fi.vm.sade.varda.rekisterointi.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ObjectNodeReader extends AbstractJsonNodeReader<ObjectNode> {

    public ObjectNodeReader(ObjectReader objectReader) {
        super(objectReader);
    }

    @Override
    protected ObjectNode cast(JsonNode jsonNode) {
        return ObjectNode.class.cast(jsonNode);
    }

}
