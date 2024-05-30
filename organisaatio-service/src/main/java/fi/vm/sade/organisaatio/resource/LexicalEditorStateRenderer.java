package fi.vm.sade.organisaatio.resource;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.util.HtmlUtils;

public class LexicalEditorStateRenderer {
    private StringBuilder sb;

    public String toHtml(JsonNode state) {
        sb = new StringBuilder();
        renderRoot(state.get("root"));
        return sb.toString();
    }

    public void renderRoot(JsonNode root) {
        root.get("children").forEach(this::renderNode);
    }

    public void renderNode(JsonNode node) {
        if (isLinebreak(node)) {
            renderLinebreak(node);
        } else if (isParagraph(node)) {
            renderParagraph(node);
        } else if (isText(node)) {
            renderText(node);
        }
    }

    private boolean isLinebreak(JsonNode node) {
        return "linebreak".equals(node.get("type").textValue());
    }

    private void renderLinebreak(JsonNode node) {
        html("<br/>\n");
    }

    private boolean isParagraph(JsonNode node) {
        return "paragraph".equals(node.get("type").textValue());
    }

    private void renderParagraph(JsonNode node) {
        html("<p>");
        node.get("children").forEach(this::renderNode);
        html("</p>\n");
    }

    private boolean isText(JsonNode node) {
        return "text".equals(node.get("type").textValue());
    }

    private void renderText(JsonNode node) {
        var text = node.get("text").textValue();

        var bold = (node.get("format").intValue() & 0x1) == 0x1;
        var italic = (node.get("format").intValue() & 0x2) == 0x2;

        if (bold) html("<b>");
        if (italic) html("<em>");
        escape(text);
        if (italic) html("</em>");
        if (bold) html("</b>");
    }

    private void html(String html) {
        sb.append(html);
    }

    private void escape(String text) {
        sb.append(HtmlUtils.htmlEscape(text, "UTF-8"));
    }
}
