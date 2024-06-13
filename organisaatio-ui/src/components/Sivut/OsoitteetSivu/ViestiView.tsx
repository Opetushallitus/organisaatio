import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { AxiosError } from 'axios';
import { useHistory, useLocation, useParams } from 'react-router-dom';
import FormLabel from '@opetushallitus/virkailija-ui-components/FormLabel';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import Spin from '@opetushallitus/virkailija-ui-components/Spin';

import {
    HakutulosRow,
    KayttajaHakutulosRow,
    sendEmail,
    SendEmailRequest,
    uploadAttachment,
    useHakutulos,
} from './OsoitteetApi';
import { ErrorBanner, InfoBanner } from './ErrorBanner';
import { GenericOsoitepalveluError } from './GenericOsoitepalveluError';
import PohjaModaali from '../../Modaalit/PohjaModaali/PohjaModaali';
import { LinklikeButton } from './LinklikeButton';
import { IconArrowBack } from './HakutulosView';

import osoitteetStyles from './SearchView.module.css';
import styles from './ViestiView.module.css';
import hakutulosStyles from './HakutulosView.module.css';
import { LexicalComposer } from '@lexical/react/LexicalComposer';
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
import { RichTextPlugin } from '@lexical/react/LexicalRichTextPlugin';
import { ContentEditable } from '@lexical/react/LexicalContentEditable';
import LexicalErrorBoundary from '@lexical/react/LexicalErrorBoundary';
import { useLexicalComposerContext } from '@lexical/react/LexicalComposerContext';
import { SerializingOnChangePlugin } from './SerializingOnChangePlugin';
import { $createHeadingNode, $isHeadingNode, HeadingNode, HeadingTagType } from '@lexical/rich-text';
import { $setBlocksType } from '@lexical/selection';
import Select from 'react-select';
import { DropdownOption } from './SelectDropdown';
import { AutoLinkPlugin, createLinkMatcherWithRegExp } from '@lexical/react/LexicalAutoLinkPlugin';
import { AutoLinkNode } from '@lexical/link';
import { ListPlugin } from '@lexical/react/LexicalListPlugin';
import {
    $isListNode,
    INSERT_ORDERED_LIST_COMMAND,
    INSERT_UNORDERED_LIST_COMMAND,
    ListItemNode,
    ListNode,
} from '@lexical/list';

type ViestiViewProps = {
    selection: Set<string>;
    setSelection: (s: Set<string>) => void;
};

function useTextInput(initialValue: string) {
    const [value, setValue] = useState<string>(initialValue);
    return [value, (event: React.ChangeEvent<HTMLInputElement>) => setValue(event.target.value)] as const;
}

function useRequiredTextInput(name: string, maxLength: number, initialValue: string) {
    const [value, setValue] = useState<string>(initialValue);
    const [error, setError] = useState<string | null>(null);

    return [
        { value, error },
        (event: React.ChangeEvent<HTMLInputElement>) => {
            const newValue = event.target.value;

            if (newValue.length == 0 && value.length > 0) {
                setError(`${name} on pakollinen`);
            } else if (newValue.length > maxLength) {
                setError(`${name} on liian pitkä (${newValue.length} merkkiä)`);
            } else {
                setError(null);
            }
            setValue(newValue);
        },
    ] as const;
}

type UploadedFile = {
    name: string;
    id: string;
};

export const ViestiView = ({ selection, setSelection }: ViestiViewProps) => {
    const { hakutulosId } = useParams<{ hakutulosId: string }>();
    const history = useHistory();
    const location = useLocation();
    const [sendError, setSendError] = useState<boolean>(false);
    const [validationErrors, setValidationErrors] = useState<string[]>([]);
    const [replyTo, onReplyToChange] = useTextInput('');
    const [copy, onCopyChange] = useTextInput('');
    const [subject, onSubjectChange] = useRequiredTextInput('Aihe', 255, '');
    const [body, setBody] = useState<{ value: string; error?: string }>({ value: '' });
    const [serializedEditor, setSerializedEditor] = useState<SerializedEditorState>(undefined);
    const [text, setText] = useState('');
    const onBodyChange = (newText: string, editor: LexicalEditor) => {
        if (newText == text) {
            return;
        }
        setText(newText);
        const MAX_BODY_LENGTH = 4_000_000;
        if (newText.length == 0) {
            setBody({ value: newText, error: `Viesti on pakollinen` });
        } else if (newText.length > MAX_BODY_LENGTH) {
            setBody({ value: newText, error: `Viesti on liian pitkä (${newText.length} merkkiä)` });
        } else {
            setBody({ value: newText });
            setSerializedEditor(editor.getEditorState().toJSON());
        }
    };
    const [files, setFiles] = useState<UploadedFile[]>([]);
    const [isSending, setIsSending] = useState(false);
    const [fileUploading, setFileUploading] = useState(false);
    const [fileUploadError, setFileUploadError] = useState<string | undefined>();
    const [fileUploadProgress, setFileUploadProgress] = useState(0);
    const [abortController, setAbortController] = useState<AbortController>();
    const uploadRef = useRef<HTMLInputElement>(null);
    const subjectValid = subject.value.length >= 1;
    const bodyValid = body.value.length >= 1;
    const sendDisabled = !subjectValid || !bodyValid || isSending;
    const hakutulos = useHakutulos(hakutulosId);
    const selectedUniqueEmails = useMemo(() => {
        if (hakutulos.state === 'OK') {
            const emails = hakutulos.value.rows.flatMap((r: HakutulosRow | KayttajaHakutulosRow) =>
                r.sahkoposti && selection.has(r.oid) ? r.sahkoposti : []
            );
            return new Set(emails).size;
        }
        return 0;
    }, [hakutulos, selection]);

    useEffect(() => {
        if (hakutulos.state === 'OK') {
            const newSelection = hakutulos.value.rows.flatMap((r: HakutulosRow | KayttajaHakutulosRow) =>
                selection.has(r.oid) ? r.oid : []
            );
            if (newSelection.length === 0) {
                // probably navigated to the viesti view directly with link so we set the whole hakutulos as selected
                setSelection(new Set(hakutulos.value.rows.map((r: HakutulosRow | KayttajaHakutulosRow) => r.oid)));
            } else {
                setSelection(new Set(newSelection));
            }
        }
    }, [hakutulos]);

    async function onSendMail() {
        setSendError(false);
        setValidationErrors([]);

        const selectedOids =
            hakutulos.state === 'OK' && selection.size !== hakutulos.value.rows.length
                ? Array.from(selection)
                : undefined;

        try {
            const request: SendEmailRequest = {
                replyTo: replyTo !== '' ? replyTo : undefined,
                copy: copy !== '' ? copy : undefined,
                subject: subject.value,
                body: serializedEditor,
                attachmentIds: files.map((f) => f.id!).filter(Boolean),
                selectedOids,
            };
            setIsSending(true);
            const response = await sendEmail(hakutulosId, request);
            history.push(`/osoitteet/viesti/${response.emailId}`);
        } catch (e) {
            console.error(e);
            setIsSending(false);
            setSendError(true);
            if (e instanceof AxiosError && e.response?.data?.validointiVirheet) {
                setValidationErrors(e.response?.data?.validointiVirheet);
            }
        }
    }

    if (hakutulos.state === 'ERROR') {
        return <GenericOsoitepalveluError />;
    }

    if (hakutulos.state === 'LOADING') {
        return (
            <div className={osoitteetStyles.LoadingOverlay}>
                <Spin />
            </div>
        );
    }

    function abortFileUpload() {
        abortController?.abort();
        if (uploadRef.current) {
            uploadRef.current.value = '';
        }
        setFileUploading(false);
    }

    async function onFileUpload() {
        const file = uploadRef.current?.files?.[0];
        if (!file || fileUploading || files.some((f) => f.name === file.name)) {
            return;
        }

        if (file.size > 4500000) {
            if (uploadRef.current) {
                uploadRef.current.value = '';
            }
            setFileUploadError('maxsize');
            return;
        }

        try {
            setFileUploading(true);
            const controller = new AbortController();
            setAbortController(controller);
            const id = await uploadAttachment(hakutulosId, file, setFileUploadProgress, controller.signal);
            setFiles([...files, { name: file.name, id }]);
        } catch (e) {
            console.error(e);
            const errorName = e instanceof AxiosError ? e.name : 'generic';
            setFileUploadError(errorName);
        } finally {
            if (uploadRef.current) {
                uploadRef.current.value = '';
            }
            setFileUploading(false);
        }
    }

    function renderFileUploadError() {
        return fileUploadError === 'CanceledError' ? (
            <InfoBanner id="fileuploaderror" onClose={() => setFileUploadError(undefined)}>
                Liitteen lataaminen keskeytettiin.
            </InfoBanner>
        ) : fileUploadError === 'maxsize' ? (
            <ErrorBanner id="fileuploaderror" onClose={() => setFileUploadError(undefined)}>
                Liitetiedoston suurin sallittu koko on 4,5MB.
            </ErrorBanner>
        ) : (
            !!fileUploadError && (
                <ErrorBanner id="fileuploaderror" onClose={() => setFileUploadError(undefined)}>
                    Liitteen latauksessa tapahtui virhe. Yritä uudelleen.
                </ErrorBanner>
            )
        );
    }

    function navigateBackToSearch() {
        if (location.key) {
            history.goBack();
        } else {
            history.push(`/osoitteet/hakutulos/${hakutulosId}`);
        }
    }

    const URL_REGEX = /((https?:\/\/(www\.)?)|(www\.))[-a-zA-Z0-9@:%._+~#=]{1,256}\.[a-zA-Z0-9()]{1,6}\b([-a-zA-Z0-9()@:%_+.~#?&//=]*)/;
    const autoLinkMatcher = createLinkMatcherWithRegExp(URL_REGEX, (text) => {
        return text.startsWith('http') ? text : `https://${text}`;
    });

    return (
        <div className={styles.ViestiView}>
            <div className={hakutulosStyles.TitleRow}>
                <div className={hakutulosStyles.Title}>
                    <h1 className={hakutulosStyles.TitleText}>Kirjoita viesti</h1>
                </div>
                <LinklikeButton onClick={navigateBackToSearch}>
                    <IconArrowBack /> Palaa
                </LinklikeButton>
            </div>
            <div className={styles.Content}>
                <BlueBanner>
                    {selectedUniqueEmails}
                    {selectedUniqueEmails === 1 ? ` vastaanottaja` : ` vastaanottajaa`}
                    {selection.size - selectedUniqueEmails > 0 && ` (${selection.size} valitusta hakutuloksesta)`}
                </BlueBanner>
                <div className={styles.Row}>
                    <div className={styles.Column}>
                        <FormLabel>
                            Lähettäjä*
                            <Input defaultValue={'Opetushallitus'} type={'text'} disabled></Input>
                        </FormLabel>
                    </div>
                    <div className={styles.Column}>
                        <FormLabel>
                            Lähetysosoite*
                            <Input defaultValue={'noreply@opintopolku.fi'} type={'text'} disabled></Input>
                        </FormLabel>
                    </div>
                </div>
                <div className={styles.Row}>
                    <div className={styles.Column}>
                        <FormLabel>
                            Vastausosoite (reply-to)
                            <Input type={'text'} value={replyTo} onChange={onReplyToChange}></Input>
                        </FormLabel>
                    </div>
                    <div className={styles.Column}>
                        <FormLabel>
                            Kopio-osoite<Input type={'text'} value={copy} onChange={onCopyChange}></Input>
                        </FormLabel>
                    </div>
                </div>
                <div className={styles.Row}>
                    <div className={styles.Column}>
                        <div role={'group'} className={subject.error ? styles.Error : ''}>
                            <FormLabel>
                                Aihe*
                                <Input type={'text'} value={subject.value} onChange={onSubjectChange}></Input>
                            </FormLabel>
                            {subject.error && <p className="error">{subject.error}</p>}
                        </div>
                    </div>
                </div>
                <div className={styles.Row}>
                    <div className={styles.Column}>
                        <div role={'group'} className={body.error ? styles.Error : ''}>
                            <FormLabel>Viesti*</FormLabel>
                            <div className={styles.Viesti}>
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
                                                aria-invalid={!!fileUploadError}
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
                                    <ListPlugin />
                                    <AutoLinkPlugin matchers={[autoLinkMatcher]} />
                                    <SerializingOnChangePlugin onChange={onBodyChange} />
                                    <div className={styles.ViestiFooter}>
                                        <strong>Osoitelähde:</strong> OPH Opintopolku. Osoitetta
                                        käytetään Opetushallituksen ja Opetus- ja kulttuuriministeriön viralliseen
                                        viestintään.
                                        <br />
                                        <strong>Adresskälla:</strong> Utbildningsstyrelsen Studieinfo.
                                        Utbildningsstyrelsen och undervisnings- och
                                        kulturministeriet använder adressen i sin kommunikation till skolorna och
                                        skolornas administratörer.
                                    </div>
                                </LexicalComposer>
                            </div>
                            {body.error && <p className="error">{body.error}</p>}
                        </div>
                    </div>
                </div>
                {renderFileUploadError()}
                <div className={styles.Attachments}>
                    {files.map((file, idx) => (
                        <div key={file.name + idx} className={styles.Attachment}>
                            <span>{file.name}</span>
                            <button
                                className={styles.RemoveFileButton}
                                onClick={() => !fileUploading && setFiles(files.filter((f) => f.name !== file.name))}
                                disabled={fileUploading}
                                aria-label={`Poista liite ${file.name}`}
                            >
                                <RemoveAttachmentIcon />
                            </button>
                        </div>
                    ))}
                    {fileUploading && <Spin size="small" />}
                </div>
                <div className={styles.Row}>
                    <Button onClick={onSendMail} disabled={sendDisabled}>
                        Lähetä
                    </Button>
                    {isSending && (
                        <div className={styles.SendSpinner}>
                            <Spin size="small" />
                        </div>
                    )}
                </div>
                {sendError && (
                    <div className={osoitteetStyles.ErrorRow}>
                        <ErrorBanner
                            onClose={() => {
                                setSendError(false);
                                setValidationErrors([]);
                            }}
                        >
                            {validationErrors.length ? (
                                <div>
                                    <span>Viestinvälityspalvelu palautti virheitä viestistä:</span>
                                    <ul>
                                        {validationErrors.map((v) => (
                                            <li key={v}>{v}</li>
                                        ))}
                                    </ul>
                                </div>
                            ) : (
                                'Viestin lähetyksessä tapahtui virhe. Yritä uudelleen.'
                            )}
                        </ErrorBanner>
                    </div>
                )}
            </div>
            {fileUploading && (
                <PohjaModaali
                    suljeCallback={abortFileUpload}
                    header={<div>Tiedostoa ladataan</div>}
                    body={
                        <div>
                            <div className={styles.ProgressBar}>
                                <div style={{ width: `${fileUploadProgress}%` }}>{fileUploadProgress}%</div>
                            </div>
                            <div className={styles.ModalButtons}>
                                <Button onClick={() => abortFileUpload()}>Peruuta</Button>
                            </div>
                        </div>
                    }
                />
            )}
        </div>
    );
};

const LowPriority = 1;

function FormattingButtons() {
    const [editor] = useLexicalComposerContext();
    const [isBold, setIsBold] = useState(false);
    const [isItalic, setIsItalic] = useState(false);
    const [isUnderline, setIsUnderline] = useState(false);
    const [headingType, setHeadingType] = useState('paragraph');

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
                setHeadingType(type);
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
                active={headingType == 'bullet'}
                label="Numeroimaton lista"
                onClick={() => editor.dispatchCommand(INSERT_UNORDERED_LIST_COMMAND, undefined)}
            >
                <UnorderedListIcon />
            </ToolbarIcon>
            <ToolbarIcon
                active={headingType == 'number'}
                label="Numeroitu lista"
                onClick={() => editor.dispatchCommand(INSERT_ORDERED_LIST_COMMAND, undefined)}
            >
                <OrderedListIcon />
            </ToolbarIcon>
            <div className={styles.HeadingTypeSelector}>
                <Select<DropdownOption>
                    options={blockTypeOptions}
                    defaultValue={blockTypeOptions[0]}
                    value={blockTypeOptions.find((option) => option.value == headingType)}
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

function BlueBanner({ children }: { children: React.ReactNode }) {
    return (
        <div className={[styles.Row, styles.BlueBanner].join(' ')}>
            <LetterIcon />
            <div>{children}</div>{' '}
        </div>
    );
}

function LetterIcon() {
    return (
        <svg width="20" height="16" viewBox="0 0 20 16" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path
                d="M2 16C1.45 16 0.979167 15.8042 0.5875 15.4125C0.195833 15.0208 0 14.55 0 14V2C0 1.45 0.195833 0.979167 0.5875 0.5875C0.979167 0.195833 1.45 0 2 0H18C18.55 0 19.0208 0.195833 19.4125 0.5875C19.8042 0.979167 20 1.45 20 2V14C20 14.55 19.8042 15.0208 19.4125 15.4125C19.0208 15.8042 18.55 16 18 16H2ZM10 9L2 4V14H18V4L10 9ZM10 7L18 2H2L10 7ZM2 4V2V14V4Z"
                fill="white"
            />
        </svg>
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

function RemoveAttachmentIcon() {
    return (
        <svg width="13" height="14" viewBox="0 0 13 14" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path
                d="M1.79865 12.0834L1.0166 11.3013L5.31788 7.00002L1.0166 2.69874L1.79865 1.91669L6.09993 6.21797L10.4012 1.91669L11.1833 2.69874L6.88199 7.00002L11.1833 11.3013L10.4012 12.0834L6.09993 7.78207L1.79865 12.0834Z"
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
