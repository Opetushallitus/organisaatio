import { useLexicalComposerContext } from '@lexical/react/LexicalComposerContext';
import { useEffect } from 'react';
import { $getRoot, LexicalEditor } from 'lexical';

type SerializingOnChangePluginProps = {
    onChange: (text: string, content: LexicalEditor) => void;
};

export function SerializingOnChangePlugin({ onChange }: SerializingOnChangePluginProps) {
    const [editor] = useLexicalComposerContext();
    useEffect(() => {
        return editor.registerUpdateListener(({ prevEditorState, tags }) => {
            editor.update(() => {
                if (tags.has('history-merge') || prevEditorState.isEmpty()) {
                    return;
                }
                const text = $getRoot().getTextContent().trim();
                onChange(text, editor);
            });
        });
    }, [editor, onChange]);

    return null;
}
