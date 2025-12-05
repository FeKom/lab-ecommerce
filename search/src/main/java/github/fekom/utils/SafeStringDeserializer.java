package github.fekom.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class SafeStringDeserializer extends JsonDeserializer<String> {
    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {
        // Aceita string, número, booleano, converte para string
        if (p.currentToken().isScalarValue()) {
            return p.getText();
        }
        return null;
    }
}