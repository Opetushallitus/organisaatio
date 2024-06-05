package fi.vm.sade.organisaatio.resource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class LexicalEditorStateRendererTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testFormattingRendering() throws IOException {
        var body = readLexicalState("/lexical/formatting-test.json");
        var renderer = new LexicalEditorStateRenderer();
        assertThat(renderer.toHtml(body)).isEqualTo("""
                <p><b>bold</b></p>
                <p><em>italics</em></p>
                <p><b><em>bold and italics</em></b></p>
                <p><b>bold </b><b><em>and</em></b><br/>
                <b><em>partly</em></b><b> italics</b></p>
                """);
    }

    @Test
    void testEscapingHtml() throws IOException {
        var body = readLexicalState("/lexical/script-tag-test.json");
        var renderer = new LexicalEditorStateRenderer();
        assertThat(renderer.toHtml(body)).isEqualTo("""
                <p>&lt;script&gt;alert(1)&lt;/script&gt;</p>
                """);
    }

    @Test
    void testHeadingRendering() throws IOException {
        var body = readLexicalState("/lexical/block-test.json");
        var render = new LexicalEditorStateRenderer();
        assertThat(render.toHtml(body)).isEqualTo("""
                <h1>Heading 1</h1>
                <h2>Heading 2</h2>
                <h3>Heading 3</h3>
                <h4>Heading 4</h4>
                """);
    }

    private JsonNode readLexicalState(String file) throws IOException {
        try (var is = getClass().getResourceAsStream(file)) {
            return objectMapper.readTree(is);
        }

    }
}