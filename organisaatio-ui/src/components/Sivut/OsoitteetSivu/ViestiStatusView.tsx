import React, { useEffect, useState } from 'react';
import { useHistory, useParams } from 'react-router-dom';
import { getEmail, GetEmailResponse } from './OsoitteetApi';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import searchStyles from './SearchView.module.css';
import styles from './ViestiStatusView.module.css';
import { LinklikeButton } from './LinklikeButton';
import Spin from '@opetushallitus/virkailija-ui-components/Spin';
import { useAtom } from 'jotai';
import { frontPropertiesAtom } from '../../../api/config';
import { ModalishBox } from './ModalishBox';
import { GenericOsoitepalveluError } from './GenericOsoitepalveluError';

export const ViestiStatusView = () => {
    const [{ viestinvalityspalveluUrl }] = useAtom(frontPropertiesAtom);
    const history = useHistory();
    const { emailId } = useParams<{ emailId: string }>();
    const [error, setError] = useState(false);
    const [email, setEmail] = useState<GetEmailResponse | undefined>(undefined);

    useEffect(() => {
        const getEmailFn = async () => {
            try {
                const newEmail = await getEmail(emailId);
                setEmail(newEmail);
            } catch (_) {
                console.error(error);
                setError(true);
            }
        };

        const handler = setInterval(() => getEmailFn(), 5000);
        return () => {
            if (handler) {
                clearInterval(handler);
            }
        };
    }, [emailId]);

    function toViestinvalityspalvelu() {
        window.open(viestinvalityspalveluUrl);
    }
    function toLahetysraportti(lahetysTunniste: string) {
        window.open(`${viestinvalityspalveluUrl}/lahetys/${lahetysTunniste}`);
    }
    function backToOsoitepalvelu() {
        history.push('/osoitteet');
    }

    if (error) {
        return <GenericOsoitepalveluError />;
    }

    if (!email) {
        return (
            <div className={searchStyles.LoadingOverlay}>
                <Spin />
            </div>
        );
    } else if (email.status === 'QUEUED') {
        return (
            <ModalishBox className={styles.Queued}>
                <h1>Lähetyksessä on viivettä</h1>
                <p>
                    Viestiä ei vielä välitetty, mutta sinun ei tarvitse tehdä lisätoimia.
                    <br />
                    Kun viesti on lähtenyt, viestin lähetysraportti tulee normaalisti näkyville Viestinvälityspalveluun.
                </p>
                <div className={styles.ButtonRow}>
                    <Button onClick={toViestinvalityspalvelu}>Viestinvälityspalvelu</Button>
                    <LinklikeButton onClick={backToOsoitepalvelu}>Palaa Osoitepalveluun</LinklikeButton>
                </div>
            </ModalishBox>
        );
    } else if (email.status === 'SENT') {
        return (
            <ModalishBox className={styles.Sent}>
                <h1>Lähetys onnistui!</h1>
                <p>
                    Voit tarkastella lähetetyn viestisi lähetysraporttia
                    <br />
                    tai palata takaisin Osoitepalveluun.
                </p>
                <div className={styles.ButtonRow}>
                    <Button onClick={() => toLahetysraportti(email.lahetysTunniste)}>Lähetysraportti</Button>
                    <LinklikeButton onClick={backToOsoitepalvelu}>Palaa Osoitepalveluun</LinklikeButton>
                </div>
            </ModalishBox>
        );
    }
    return <div></div>;
};
