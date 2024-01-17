import FormLabel from '@opetushallitus/virkailija-ui-components/FormLabel';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import Textarea from '@opetushallitus/virkailija-ui-components/Textarea';
import React, { useState } from 'react';
import osoitteetStyles from './SearchView.module.css';
import styles from './ViestiView.module.css';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import { sendEmail, SendEmailRequest } from './OsoitteetApi';
import { useHistory, useParams } from 'react-router-dom';
import { ErrorBanner } from './ErrorBanner';

// TODO
// - Peruuta
// - Lomakkeen validointi

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

    return (
        <div className={styles.ViestiView}>
            <div className={styles.Header}>
                <h2>Kirjoita Viesti</h2>
            </div>
            <div className={styles.Content}>
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
