import FormLabel from '@opetushallitus/virkailija-ui-components/FormLabel';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import Textarea from '@opetushallitus/virkailija-ui-components/Textarea';
import React, { useState } from 'react';
import osoitteetStyles from './SearchView.module.css';
import styles from './ViestiView.module.css';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import { sendEmail, SendEmailRequest, useHakutulos } from './OsoitteetApi';
import { useHistory, useParams } from 'react-router-dom';
import { ErrorBanner } from './ErrorBanner';
import Spin from '@opetushallitus/virkailija-ui-components/Spin';

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

export const ViestiView = () => {
    const { hakutulosId } = useParams<{ hakutulosId: string }>();
    const history = useHistory();
    const [sendError, setSendError] = useState<boolean>(false);
    const [replyTo, onReplyToChange] = useTextInput('');
    const [copy, onCopyChange] = useTextInput('');
    const [subject, onSubjectChange] = useRequiredTextInput('Aihe', 255, '');
    const [body, onBodyChange] = useRequiredTextInput('Viesti', 6291456, '');
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
        return <div>Tapahtui virhe</div>;
    }

    if (hakutulos.state === 'LOADING') {
        return (
            <div className={osoitteetStyles.LoadingOverlay}>
                <Spin />
            </div>
        );
    }

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
