import { useLexicalComposerContext } from '@lexical/react/LexicalComposerContext';
import { $generateHtmlFromNodes } from '@lexical/html';
import { useEffect } from 'react';
import { $getRoot } from 'lexical';

type OnHtmlChangePluginProps = {
    onChange: (text: string, html: string) => void;
};

export function OnHtmlChangePlugin({ onChange }: OnHtmlChangePluginProps) {
    const [editor] = useLexicalComposerContext();
    useEffect(() => {
        return editor.registerUpdateListener(({ prevEditorState, tags }) => {
            editor.update(() => {
                if (tags.has('history-merge') || prevEditorState.isEmpty()) {
                    return;
                }
                const text = $getRoot().getTextContent().trim();
                const html = $generateHtmlFromNodes(editor);
                onChange(text, cleanupHtml(html));
            });
        });
    }, [editor, onChange]);

    return null;
}

function cleanupHtml(html: string) {
    const parser = new DOMParser();
    const doc = parser.parseFromString(html, 'text/html');

    // Remove unnecessary spans
    doc.querySelectorAll('span').forEach((span) => span.replaceWith(...span.childNodes));

    // Remove dir attributes
    for (const element of doc.querySelectorAll('[dir]')) {
        element.removeAttribute('dir');
    }

    return doc.body.innerHTML;
}
