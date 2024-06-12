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
                <p><em>italic</em></p>
                <p><u>underlined</u></p>
                <p><b><em>italic and bold</em></b></p>
                <p><b><u>bold and underlined</u></b></p>
                <p><em><u>italic and underlined</u></em></p>
                <p></p>
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
        var body = readLexicalState("/lexical/heading-test.json");
        var render = new LexicalEditorStateRenderer();
        assertThat(render.toHtml(body)).isEqualTo("""
                <h1>Heading 1</h1>
                <h2>Heading 2</h2>
                <h3>Heading 3</h3>
                <h4>Heading 4</h4>
                """);
    }

    @Test
    void testPlainTextRendering() throws IOException {
        var body = readLexicalState("/lexical/plaintext-test.json");
        var renderer = new LexicalEditorStateRenderer();
        assertThat(renderer.toHtml(body)).isEqualTo("""
                <p>plain text</p>
                """);
    }

    @Test
    void testLinkRendering() throws IOException {
        var body = readLexicalState("/lexical/link-test.json");
        var renderer = new LexicalEditorStateRenderer();
        assertThat(renderer.toHtml(body)).isEqualTo("""
                <p><a href="https://oph.fi">https://oph.fi</a></p>
                """);
    }

    private JsonNode readLexicalState(String file) throws IOException {
        try (var is = getClass().getResourceAsStream(file)) {
            return objectMapper.readTree(is);
        }

    }
}