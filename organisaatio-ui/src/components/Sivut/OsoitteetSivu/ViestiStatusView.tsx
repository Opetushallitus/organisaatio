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

export const ViestiStatusView = () => {
    const [{ viestinvalityspalveluUrl }] = useAtom(frontPropertiesAtom);
    const history = useHistory();
    const { emailId } = useParams<{ emailId: string }>();
    const [error, setError] = useState(false);
    const [email, setEmail] = useState<GetEmailResponse | undefined>(undefined);
    useEffect(() => {
        setError(false);
        getEmail(emailId)
            .then((email) => setEmail(email))
            .catch((error) => {
                console.error(error);
                setError(true);
            });
    }, [emailId]);

    function toViestinvalityspalvelu() {
        window.open(viestinvalityspalveluUrl);
    }
    function backToOsoitepalvelu() {
        history.push('/osoitteet');
    }

    if (error) {
        return <div>Tapahtui virhe</div>;
    }

    if (!email) {
        return (
            <div className={searchStyles.LoadingOverlay}>
                <Spin />
            </div>
        );
    }

    if (email.status === 'QUEUED') {
        return (
            <ModalishBox status="warning">
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
    } else {
        return (
            <div>
                <pre>{JSON.stringify(email, null, 2)}</pre>
            </div>
        );
    }
};

type ModalishBoxProps = React.PropsWithChildren<{
    status: 'warning' | 'success';
}>;
function ModalishBox({ children }: ModalishBoxProps) {
    return (
        <div className={styles.ModalishBox}>
            <div className={styles.Content}>{children}</div>
        </div>
    );
}
