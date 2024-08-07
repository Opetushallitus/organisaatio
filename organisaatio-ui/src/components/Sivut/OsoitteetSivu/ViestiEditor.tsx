import React, { useCallback, useEffect, useState } from 'react';
import { LexicalComposer } from '@lexical/react/LexicalComposer';
import { RichTextPlugin } from '@lexical/react/LexicalRichTextPlugin';
import { ContentEditable } from '@lexical/react/LexicalContentEditable';
import LexicalErrorBoundary from '@lexical/react/LexicalErrorBoundary';
import { AutoLinkPlugin, createLinkMatcherWithRegExp } from '@lexical/react/LexicalAutoLinkPlugin';
import { ListPlugin } from '@lexical/react/LexicalListPlugin';
import { HistoryPlugin } from '@lexical/react/LexicalHistoryPlugin';
import { useLexicalComposerContext } from '@lexical/react/LexicalComposerContext';
import {
    $createParagraphNode,
    $getSelection,
    $isRangeSelection,
    $isRootOrShadowRoot,
    FORMAT_TEXT_COMMAND,
    LexicalEditor,
    SELECTION_CHANGE_COMMAND,
    SerializedEditorState,
} from 'lexical';
import { $findMatchingParent, mergeRegister } from '@lexical/utils';
import { AutoLinkNode } from '@lexical/link';
import { $createHeadingNode, $isHeadingNode, HeadingNode, HeadingTagType } from '@lexical/rich-text';
import { $setBlocksType } from '@lexical/selection';
import {
    $isListNode,
    INSERT_ORDERED_LIST_COMMAND,
    INSERT_UNORDERED_LIST_COMMAND,
    ListItemNode,
    ListNode,
} from '@lexical/list';
import Select from 'react-select';

import { DropdownOption } from './SelectDropdown';
import { SerializingOnChangePlugin } from './SerializingOnChangePlugin';

import styles from './ViestiView.module.css';

type ViestiEditorProps = {
    onFileUpload: () => void;
    setSerializedEditor: (editor: SerializedEditorState) => void;
    setEditorError: (error: string) => void;
    fileUploading: boolean;
    fileUploadError: boolean;
    uploadRef: React.MutableRefObject<HTMLInputElement>;
};

const URL_REGEX = /((https?:\/\/(www\.)?)|(www\.))[-a-zA-Z0-9@:%._+~#=]{1,256}\.[a-zA-Z0-9()]{1,6}\b([-a-zA-Z0-9()@:%_+.~#?&//=]*)/;
const autoLinkMatcher = createLinkMatcherWithRegExp(URL_REGEX, (text) => {
    return text.startsWith('http') ? text : `https://${text}`;
});
const matchers = [autoLinkMatcher];

const MAX_BODY_LENGTH = 4_000_000;

export const ViestiEditor = ({
    onFileUpload,
    setEditorError,
    setSerializedEditor,
    fileUploading,
    fileUploadError,
    uploadRef,
}: ViestiEditorProps) => {
    const onBodyChange = (body: string, editor: LexicalEditor) => {
        if (body.length == 0) {
            setEditorError('Viesti on pakollinen');
        } else if (body.length > MAX_BODY_LENGTH) {
            setEditorError(`Viesti on liian pitkä (${body.length} merkkiä)`);
        } else {
            setEditorError(undefined);
            setSerializedEditor(editor.getEditorState().toJSON());
        }
    };

    return (
        <LexicalComposer
            initialConfig={{
                namespace: 'Viesti',
                onError: (error: Error, editor: LexicalEditor): void => {
                    console.error(error, editor);
                },
                nodes: [HeadingNode, AutoLinkNode, ListNode, ListItemNode],
                theme: {
                    text: {
                        underline: styles.LexicalThemeUnderline,
                    },
                },
            }}
        >
            <div className={styles.ViestiButtons}>
                <ToolbarIcon label="Lataa liitetiedosto" disabled={fileUploading}>
                    <AttachmentIcon />
                    <input
                        id="file-upload"
                        type="file"
                        name="file"
                        ref={uploadRef}
                        onChange={onFileUpload}
                        style={{ display: 'none' }}
                        disabled={fileUploading}
                        aria-invalid={fileUploadError}
                        aria-errormessage="fileuploaderror"
                    />
                </ToolbarIcon>
                <FormattingButtons />
            </div>
            <RichTextPlugin
                contentEditable={<ContentEditable className={styles.ViestiTextarea} />}
                placeholder={<div></div>}
                ErrorBoundary={LexicalErrorBoundary}
            />
            <HistoryPlugin />
            <ListPlugin />
            <AutoLinkPlugin matchers={matchers} />
            <SerializingOnChangePlugin onChange={onBodyChange} />
            <div className={styles.ViestiFooter}>
                <strong>Osoitelähde:</strong> OPH Opintopolku. Osoitetta käytetään Opetushallituksen ja Opetus- ja
                kulttuuriministeriön viralliseen viestintään.
                <br />
                <strong>Adresskälla:</strong> Utbildningsstyrelsen Studieinfo. Utbildningsstyrelsen och undervisnings-
                och kulturministeriet använder adressen i sin kommunikation till skolorna och skolornas administratörer.
            </div>
        </LexicalComposer>
    );
};

const LowPriority = 1;

function FormattingButtons() {
    const [editor] = useLexicalComposerContext();
    const [isBold, setIsBold] = useState(false);
    const [isItalic, setIsItalic] = useState(false);
    const [isUnderline, setIsUnderline] = useState(false);
    const [blockType, setBlockType] = useState('paragraph');

    const $updateToolbar = useCallback(() => {
        const selection = $getSelection();
        if ($isRangeSelection(selection)) {
            setIsBold(selection.hasFormat('bold'));
            setIsItalic(selection.hasFormat('italic'));
            setIsUnderline(selection.hasFormat('underline'));

            const anchorNode = selection.anchor.getNode();
            let element =
                anchorNode.getKey() === 'root'
                    ? anchorNode
                    : $findMatchingParent(anchorNode, (e) => {
                          const parent = e.getParent();
                          return parent !== null && $isRootOrShadowRoot(parent);
                      });

            if (element === null) {
                element = anchorNode.getTopLevelElementOrThrow();
            }

            const elementDOM = editor.getElementByKey(element.getKey());

            if (elementDOM !== null) {
                const type = $isHeadingNode(element)
                    ? element.getTag()
                    : $isListNode(element)
                    ? element.getListType()
                    : element.getType();
                setBlockType(type);
            }
        }
    }, [editor]);

    useEffect(() => {
        return mergeRegister(
            editor.registerUpdateListener(({ editorState }) => {
                editorState.read(() => {
                    $updateToolbar();
                });
            }),
            editor.registerCommand(
                SELECTION_CHANGE_COMMAND,
                () => {
                    $updateToolbar();
                    return false;
                },
                LowPriority
            )
        );
    }, [editor, $updateToolbar]);

    type BlockType = HeadingTagType & 'paragraph' & 'numbered' & 'bullet';

    const formatBlockType = (blockType: BlockType) => {
        editor.update(() => {
            const selection = $getSelection();
            if ($isRangeSelection(selection)) {
                if (blockType == 'paragraph') {
                    $setBlocksType(selection, () => $createParagraphNode());
                } else {
                    $setBlocksType(selection, () => $createHeadingNode(blockType));
                }
            }
        });
    };

    const blockTypeOptions: DropdownOption[] = [
        { value: 'paragraph', label: 'Leipäteksti' },
        { value: 'h1', label: 'Otsikko 1' },
        { value: 'h2', label: 'Otsikko 2' },
        { value: 'h3', label: 'Otsikko 3' },
        { value: 'h4', label: 'Otsikko 4' },
    ];

    return (
        <>
            <ToolbarIcon
                active={isBold}
                label="Lihavoi"
                onClick={() => {
                    editor.dispatchCommand(FORMAT_TEXT_COMMAND, 'bold');
                }}
            >
                <BoldIcon />
            </ToolbarIcon>
            <ToolbarIcon
                active={isItalic}
                label="Kursivoi"
                onClick={() => {
                    editor.dispatchCommand(FORMAT_TEXT_COMMAND, 'italic');
                }}
            >
                <ItalicIcon />
            </ToolbarIcon>
            <ToolbarIcon
                active={isUnderline}
                label="Alleviivaa"
                onClick={() => {
                    editor.dispatchCommand(FORMAT_TEXT_COMMAND, 'underline');
                }}
            >
                <UnderlineIcon />
            </ToolbarIcon>
            <ToolbarIcon
                active={blockType == 'bullet'}
                label="Numeroimaton lista"
                onClick={() => editor.dispatchCommand(INSERT_UNORDERED_LIST_COMMAND, undefined)}
            >
                <UnorderedListIcon />
            </ToolbarIcon>
            <ToolbarIcon
                active={blockType == 'number'}
                label="Numeroitu lista"
                onClick={() => editor.dispatchCommand(INSERT_ORDERED_LIST_COMMAND, undefined)}
            >
                <OrderedListIcon />
            </ToolbarIcon>
            <div className={styles.HeadingTypeSelector}>
                <Select<DropdownOption>
                    options={blockTypeOptions}
                    defaultValue={blockTypeOptions[0]}
                    value={blockTypeOptions.find((option) => option.value == blockType)}
                    onChange={(option: DropdownOption) => formatBlockType(option.value as BlockType)}
                />
            </div>
        </>
    );
}

type ToolbarIconProps = React.PropsWithChildren<{
    active?: boolean;
    disabled?: boolean;
    className?: string;
    label: string;
    onClick?: () => void;
}>;

function ToolbarIcon({ children, className, label, active = false, disabled = false, onClick }: ToolbarIconProps) {
    const classes = [styles.ViestiButton];
    if (disabled) classes.push(styles.ViestiButtonDisabled);
    if (active) classes.push(styles.ViestiButtonActive);
    if (className) classes.push(className);

    return (
        <label className={classes.join(' ')} aria-label={label} aria-disabled={disabled} onClick={onClick}>
            {children}
        </label>
    );
}

function AttachmentIcon() {
    return (
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path
                d="M12 22C10.4667 22 9.16667 21.4667 8.1 20.4C7.03333 19.3333 6.5 18.0333 6.5 16.5V6C6.5 4.9 6.89167 3.95833 7.675 3.175C8.45833 2.39167 9.4 2 10.5 2C11.6 2 12.5417 2.39167 13.325 3.175C14.1083 3.95833 14.5 4.9 14.5 6V15.5C14.5 16.2 14.2583 16.7917 13.775 17.275C13.2917 17.7583 12.7 18 12 18C11.3 18 10.7083 17.7583 10.225 17.275C9.74167 16.7917 9.5 16.2 9.5 15.5V6H11V15.5C11 15.7833 11.0958 16.0208 11.2875 16.2125C11.4792 16.4042 11.7167 16.5 12 16.5C12.2833 16.5 12.5208 16.4042 12.7125 16.2125C12.9042 16.0208 13 15.7833 13 15.5V6C13 5.3 12.7583 4.70833 12.275 4.225C11.7917 3.74167 11.2 3.5 10.5 3.5C9.8 3.5 9.20833 3.74167 8.725 4.225C8.24167 4.70833 8 5.3 8 6V16.5C8 17.6 8.39167 18.5417 9.175 19.325C9.95833 20.1083 10.9 20.5 12 20.5C13.1 20.5 14.0417 20.1083 14.825 19.325C15.6083 18.5417 16 17.6 16 16.5V6H17.5V16.5C17.5 18.0333 16.9667 19.3333 15.9 20.4C14.8333 21.4667 13.5333 22 12 22Z"
                fill="#666666"
            />
        </svg>
    );
}

function BoldIcon() {
    return (
        <svg xmlns="http://www.w3.org/2000/svg" height="24px" viewBox="0 0 24 24" width="24px" fill="#666666">
            <path d="M0 0h24v24H0V0z" fill="none" />
            <path d="M15.6 10.79c.97-.67 1.65-1.77 1.65-2.79 0-2.26-1.75-4-4-4H7v14h7.04c2.09 0 3.71-1.7 3.71-3.79 0-1.52-.86-2.82-2.15-3.42zM10 6.5h3c.83 0 1.5.67 1.5 1.5s-.67 1.5-1.5 1.5h-3v-3zm3.5 9H10v-3h3.5c.83 0 1.5.67 1.5 1.5s-.67 1.5-1.5 1.5z" />
        </svg>
    );
}

function ItalicIcon() {
    return (
        <svg xmlns="http://www.w3.org/2000/svg" height="24px" viewBox="0 0 24 24" width="24px" fill="#666666">
            <path d="M0 0h24v24H0V0z" fill="none" />
            <path d="M10 4v3h2.21l-3.42 8H6v3h8v-3h-2.21l3.42-8H18V4h-8z" />
        </svg>
    );
}

function UnderlineIcon() {
    return (
        <svg xmlns="http://www.w3.org/2000/svg" height="24px" viewBox="0 0 24 24" width="24px" fill="#5f6368">
            <path d="M0 0h24v24H0V0z" fill="none" />
            <path d="M12 17c3.31 0 6-2.69 6-6V3h-2.5v8c0 1.93-1.57 3.5-3.5 3.5S8.5 12.93 8.5 11V3H6v8c0 3.31 2.69 6 6 6zm-7 2v2h14v-2H5z" />
        </svg>
    );
}

function UnorderedListIcon() {
    return (
        <svg xmlns="http://www.w3.org/2000/svg" height="24px" viewBox="0 -960 960 960" width="24px" fill="#5f6368">
            <path d="M360-200v-80h480v80H360Zm0-240v-80h480v80H360Zm0-240v-80h480v80H360ZM200-160q-33 0-56.5-23.5T120-240q0-33 23.5-56.5T200-320q33 0 56.5 23.5T280-240q0 33-23.5 56.5T200-160Zm0-240q-33 0-56.5-23.5T120-480q0-33 23.5-56.5T200-560q33 0 56.5 23.5T280-480q0 33-23.5 56.5T200-400Zm0-240q-33 0-56.5-23.5T120-720q0-33 23.5-56.5T200-800q33 0 56.5 23.5T280-720q0 33-23.5 56.5T200-640Z" />
        </svg>
    );
}

function OrderedListIcon() {
    return (
        <svg xmlns="http://www.w3.org/2000/svg" height="24px" viewBox="0 -960 960 960" width="24px" fill="#5f6368">
            <path d="M120-80v-60h100v-30h-60v-60h60v-30H120v-60h120q17 0 28.5 11.5T280-280v40q0 17-11.5 28.5T240-200q17 0 28.5 11.5T280-160v40q0 17-11.5 28.5T240-80H120Zm0-280v-110q0-17 11.5-28.5T160-510h60v-30H120v-60h120q17 0 28.5 11.5T280-560v70q0 17-11.5 28.5T240-450h-60v30h100v60H120Zm60-280v-180h-60v-60h120v240h-60Zm180 440v-80h480v80H360Zm0-240v-80h480v80H360Zm0-240v-80h480v80H360Z" />
        </svg>
    );
}
