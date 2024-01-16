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

function useTextInput(initialValue: string) {
    const [value, setValue] = useState<string>(initialValue);
    return [value, (event: React.ChangeEvent<HTMLInputElement>) => setValue(event.target.value)] as const;
}

export const ViestiView = () => {
    const { hakutulosId } = useParams<{ hakutulosId: string }>();
    const history = useHistory();
    const [sendError, setSendError] = useState<boolean>(false);
    const [replyTo, onReplyToChange] = useTextInput('');
    const [cc, onCcChange] = useTextInput('');
    const [subject, onSubjectChange] = useTextInput('');
    const [body, onBodyChange] = useTextInput('');
    const sendDisabled = !(subject.length >= 1 && body.length >= 1);

    async function onSendMail() {
        setSendError(false);
        try {
            const request: SendEmailRequest = { replyTo, cc, subject, body };
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
                        <Input type={'text'} value={cc} onChange={onCcChange}></Input>
                    </div>
                </div>
                <div className={styles.Row}>
                    <div className={styles.Column}>
                        <FormLabel>Aihe*</FormLabel>
                        <Input type={'text'} value={subject} onChange={onSubjectChange}></Input>
                    </div>
                </div>
                <div className={styles.Row}>
                    <div className={styles.Column}>
                        <FormLabel>Viesti*</FormLabel>
                        <div className={styles.Viesti}>
                            <Textarea className={styles.ViestiTextarea} value={body} onChange={onBodyChange}></Textarea>
                            <div className={styles.ViestiFooter}>
                                <strong>Osoitelähde:</strong> OPH Opintopolku (Organisaatiopalvelu). Osoitetta käytetään
                                Opetushallituksen ja Opetus- ja kulttuuriministeriön viralliseen viestintään.
                                <br />
                                <strong>Adresskälla:</strong> Utbildningsstyrelsen Studieinfo (Organisationstjänst).
                                Utbildningsstyrelsen och undervisnings- och kulturministeriet använder adressen i sin
                                kommunikation till skolorna och skolornas administratörer.
                            </div>
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
