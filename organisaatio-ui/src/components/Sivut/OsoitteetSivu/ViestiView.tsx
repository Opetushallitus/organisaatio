import React, { useEffect, useMemo, useRef, useState } from 'react';
import { AxiosError } from 'axios';
import { SerializedEditorState } from 'lexical';
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
import { ViestiEditor } from './ViestiEditor';

import osoitteetStyles from './SearchView.module.css';
import styles from './ViestiView.module.css';
import hakutulosStyles from './HakutulosView.module.css';

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
    const [editorError, setEditorError] = useState<string>(undefined);
    const [serializedEditor, setSerializedEditor] = useState<SerializedEditorState>(undefined);
    const [files, setFiles] = useState<UploadedFile[]>([]);
    const [isSending, setIsSending] = useState(false);
    const [fileUploading, setFileUploading] = useState(false);
    const [fileUploadError, setFileUploadError] = useState<string | undefined>();
    const [fileUploadProgress, setFileUploadProgress] = useState(0);
    const [abortController, setAbortController] = useState<AbortController>();
    const uploadRef = useRef<HTMLInputElement>(null);
    const subjectValid = subject.value.length >= 1;
    const sendDisabled = !subjectValid || editorError || isSending || !serializedEditor?.root?.children?.length;
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
                        <div role={'group'} className={editorError ? styles.Error : ''}>
                            <FormLabel>Viesti*</FormLabel>
                            <div className={styles.Viesti}>
                                <ViestiEditor
                                    onFileUpload={onFileUpload}
                                    setEditorError={setEditorError}
                                    setSerializedEditor={setSerializedEditor}
                                    fileUploading={fileUploading}
                                    fileUploadError={!!fileUploadError}
                                    uploadRef={uploadRef}
                                />
                            </div>
                            {editorError && <p className="error">{editorError}</p>}
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
