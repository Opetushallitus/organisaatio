package fi.vm.sade.varda.rekisterointi.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class ArrayNodeConverter extends AbstractJsonNodeReader<ArrayNode> {

    public ArrayNodeConverter(ObjectReader objectReader) {
        super(objectReader);
    }

    @Override
    protected ArrayNode cast(JsonNode jsonNode) {
        return ArrayNode.class.cast(jsonNode);
    }

}
