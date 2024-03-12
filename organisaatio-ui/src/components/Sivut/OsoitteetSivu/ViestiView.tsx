import FormLabel from '@opetushallitus/virkailija-ui-components/FormLabel';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import Textarea from '@opetushallitus/virkailija-ui-components/Textarea';
import React, { useState, useRef } from 'react';
import osoitteetStyles from './SearchView.module.css';
import styles from './ViestiView.module.css';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import { sendEmail, SendEmailRequest, uploadAttachment, useHakutulos } from './OsoitteetApi';
import { useHistory, useParams } from 'react-router-dom';
import { ErrorBanner } from './ErrorBanner';
import Spin from '@opetushallitus/virkailija-ui-components/Spin';
import { GenericOsoitepalveluError } from './GenericOsoitepalveluError';

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

export const ViestiView = () => {
    const { hakutulosId } = useParams<{ hakutulosId: string }>();
    const history = useHistory();
    const [sendError, setSendError] = useState<boolean>(false);
    const [replyTo, onReplyToChange] = useTextInput('');
    const [copy, onCopyChange] = useTextInput('');
    const [subject, onSubjectChange] = useRequiredTextInput('Aihe', 255, '');
    const [body, onBodyChange] = useRequiredTextInput('Viesti', 6291456, '');
    const [files, setFiles] = useState<UploadedFile[]>([]);
    const [fileUploading, setFileUploading] = useState(false);
    const [fileUploadError, setFileUploadError] = useState(false);
    const uploadRef = useRef<HTMLInputElement>(null);
    const subjectValid = subject.value.length >= 1;
    const bodyValid = body.value.length >= 1;
    const sendDisabled = !subjectValid || !bodyValid;
    const hakutulos = useHakutulos(hakutulosId);

    async function onSendMail() {
        setSendError(false);
        try {
            const request: SendEmailRequest = {
                replyTo: replyTo !== '' ? replyTo : undefined,
                copy: copy !== '' ? copy : undefined,
                subject: subject.value,
                body: body.value,
            };
            const response = await sendEmail(hakutulosId, request);
            history.push(`/osoitteet/viesti/${response.emailId}`);
        } catch (e) {
            console.error(e);
            setSendError(true);
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

    async function onFileUpload() {
        const file = uploadRef.current?.files?.[0];
        if (!file || fileUploading) {
            return;
        }

        try {
            setFileUploading(true);
            const id = await uploadAttachment(hakutulosId, file);
            setFiles([...files, { name: file.name, id }]);
        } catch (e) {
            console.error(e);
            setFileUploadError(true);
        } finally {
            setFileUploading(false);
        }
    }

    const attachmentButtonClassName = fileUploading
        ? `${styles.ViestiButton} ${styles.ViestiButtonDisabled}`
        : styles.ViestiButton;

    return (
        <div className={styles.ViestiView}>
            <div className={styles.Header}>
                <h2>Kirjoita Viesti</h2>
            </div>
            <div className={styles.Content}>
                <BlueBanner>
                    {hakutulos.value.rows.length === 1
                        ? `${hakutulos.value.rows.length} vastaanottaja`
                        : `${hakutulos.value.rows.length} vastaanottajaa`}
                </BlueBanner>
                <div className={styles.Row}>
                    <div className={styles.Column}>
                        <FormLabel>Lähettäjä*</FormLabel>
                        <Input defaultValue={'Opetushallitus'} type={'text'} disabled></Input>
                    </div>
                    <div className={styles.Column}>
                        <FormLabel>Lähetysosoite*</FormLabel>
                        <Input defaultValue={'noreply@opintopolku.fi'} type={'text'} disabled></Input>
                    </div>
                </div>
                <div className={styles.Row}>
                    <div className={styles.Column}>
                        <FormLabel>Vastausosoite (reply-to)</FormLabel>
                        <Input type={'text'} value={replyTo} onChange={onReplyToChange}></Input>
                    </div>
                    <div className={styles.Column}>
                        <FormLabel>Kopio-osoite</FormLabel>
                        <Input type={'text'} value={copy} onChange={onCopyChange}></Input>
                    </div>
                </div>
                <div className={styles.Row}>
                    <div className={styles.Column}>
                        <div role={'group'} className={subject.error ? styles.Error : ''}>
                            <FormLabel>
                                Aihe*
                                <Input type={'text'} value={subject.value} onChange={onSubjectChange}></Input>
                            </FormLabel>
                            {subject.error && <p>{subject.error}</p>}
                        </div>
                    </div>
                </div>
                <div className={styles.Row}>
                    <div className={styles.Column}>
                        <div role={'group'} className={body.error ? styles.Error : ''}>
                            <FormLabel>Viesti*</FormLabel>
                            <div className={styles.Viesti}>
                                <div className={styles.ViestiButtons}>
                                    <label
                                        htmlFor="file-upload"
                                        className={attachmentButtonClassName}
                                        aria-disabled={fileUploading}
                                    >
                                        <AttachmentIcon />
                                        <input
                                            id="file-upload"
                                            type="file"
                                            name="file"
                                            ref={uploadRef}
                                            onChange={onFileUpload}
                                            style={{ display: 'none' }}
                                        />
                                    </label>
                                </div>
                                <Textarea
                                    className={styles.ViestiTextarea}
                                    value={body.value}
                                    onChange={onBodyChange}
                                ></Textarea>
                                <div className={styles.ViestiFooter}>
                                    <strong>Osoitelähde:</strong> OPH Opintopolku (Organisaatiopalvelu). Osoitetta
                                    käytetään Opetushallituksen ja Opetus- ja kulttuuriministeriön viralliseen
                                    viestintään.
                                    <br />
                                    <strong>Adresskälla:</strong> Utbildningsstyrelsen Studieinfo (Organisationstjänst).
                                    Utbildningsstyrelsen och undervisnings- och kulturministeriet använder adressen i
                                    sin kommunikation till skolorna och skolornas administratörer.
                                </div>
                            </div>
                            {body.error && <p>{body.error}</p>}
                        </div>
                    </div>
                </div>
                <div>
                    {fileUploadError && (
                        <ErrorBanner onClose={() => setFileUploadError(false)}>
                            Liitteen latauksessa tapahtui virhe. Yritä uudelleen.
                        </ErrorBanner>
                    )}
                    <div className={styles.Attachments}>
                        {files.map((file) => (
                            <div key={file.id} className={styles.Attachment}>
                                <span>{file.name}</span>
                                <button
                                    className={styles.RemoveFileButton}
                                    onClick={() => setFiles(files.filter((f) => f.id !== file.id))}
                                >
                                    <svg
                                        width="13"
                                        height="14"
                                        viewBox="0 0 13 14"
                                        fill="none"
                                        xmlns="http://www.w3.org/2000/svg"
                                    >
                                        <path
                                            d="M1.79865 12.0834L1.0166 11.3013L5.31788 7.00002L1.0166 2.69874L1.79865 1.91669L6.09993 6.21797L10.4012 1.91669L11.1833 2.69874L6.88199 7.00002L11.1833 11.3013L10.4012 12.0834L6.09993 7.78207L1.79865 12.0834Z"
                                            fill="#666666"
                                        />
                                    </svg>
                                </button>
                            </div>
                        ))}
                    </div>
                </div>
                <div className={styles.Row}>
                    <Button onClick={onSendMail} disabled={sendDisabled}>
                        Lähetä
                    </Button>
                </div>
                {sendError && (
                    <div className={osoitteetStyles.ErrorRow}>
                        <ErrorBanner onClose={() => setSendError(false)}>
                            Viestin lähetyksessä tapahtui virhe. Yritä uudelleen.
                        </ErrorBanner>
                    </div>
                )}
            </div>
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
