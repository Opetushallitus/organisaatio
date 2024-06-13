package fi.vm.sade.organisaatio.resource;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

public class LexicalEditorStateRenderer {
    private StringBuilder sb;

    public String toHtml(JsonNode state) {
        sb = new StringBuilder();
        renderRoot(state.get("root"));
        return sb.toString();
    }

    public void renderRoot(JsonNode root) {
        renderChildren(root);
    }

    public void renderNode(JsonNode node) {
        if (isLinebreak(node)) {
            renderLinebreak();
        } else if (isParagraph(node)) {
            renderParagraph(node);
        } else if (isText(node)) {
            renderText(node);
        } else if (isHeadingNode(node)) {
            renderHeading(node);
        } else if (isAutoLink(node)) {
            renderAutoLink(node);
        } else if (isList(node)) {
            renderList(node);
        } else if (isListItem(node)) {
            renderListItem(node);
        }
    }

    private boolean isList(JsonNode node) {
        return "list".equals(node.get("type").textValue());
    }

    private void renderList(JsonNode node) {
        var listType = node.get("listType").textValue();
        var ol = "number".equals(listType);
        var ul = "bullet".equals(listType);

        if (ol) html("<ol>");
        if (ul) html("<ul>");
        renderChildren(node);
        if (ol) html("</ol>\n");
        if (ul) html("</ul>\n");
    }

    private boolean isListItem(JsonNode node) {
        return "listitem".equals(node.get("type").textValue());
    }

    private void renderListItem(JsonNode node) {
        html("<li>");
        renderChildren(node);
        html("</li>");
    }

    private boolean isAutoLink(JsonNode node) {
        return "autolink".equals(node.get("type").textValue());
    }

    private void renderAutoLink(JsonNode node) {
        var url = node.get("url").textValue();
        html("<a href=\"" + HtmlUtils.htmlEscape(url, "UTF-8") + "\">");
        renderChildren(node);
        html("</a>");
    }

    private void renderChildren(JsonNode node) {
        node.get("children").forEach(this::renderNode);
    }

    private boolean isHeadingNode(JsonNode node) {
        return "heading".equals(node.get("type").textValue());
    }

    private static final List<String> headingTags = List.of("h1", "h2", "h3", "h4");

    private void renderHeading(JsonNode node) {
        var tag = headingTags.stream().filter(t -> t.equals(node.get("tag").textValue())).findFirst();

        if (tag.isPresent()) {
            html("<" + tag.get() + ">");
        }
        renderChildren(node);
        if (tag.isPresent()) {
            html("</" + tag.get() + ">\n");
        }
    }

    private boolean isLinebreak(JsonNode node) {
        return "linebreak".equals(node.get("type").textValue());
    }

    private void renderLinebreak() {
        html("<br/>\n");
    }

    private boolean isParagraph(JsonNode node) {
        return "paragraph".equals(node.get("type").textValue());
    }

    private void renderParagraph(JsonNode node) {
        html("<p>");
        renderChildren(node);
        html("</p>\n");
    }

    private boolean isText(JsonNode node) {
        return "text".equals(node.get("type").textValue());
    }

    private void renderText(JsonNode node) {
        var text = node.get("text").textValue();

        var bold = (node.get("format").intValue() & 0x1) == 0x1;
        var italic = (node.get("format").intValue() & 0x2) == 0x2;
        var underlined = (node.get("format").intValue() & 0x8) == 0x8;

        if (bold) html("<b>");
        if (italic) html("<em>");
        if (underlined) html("<u>");
        escape(text);
        if (underlined) html("</u>");
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
