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
        window.open(`${viestinvalityspalveluUrl}/viestinvalitys-raportointi`);
    }
    function toLahetysraportti(lahetysTunniste: string) {
        window.open(`${viestinvalityspalveluUrl}/viestinvalitys-raportointi/lahetys/${lahetysTunniste}`);
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

type ModalishBoxProps = React.PropsWithChildren<{
    className: string;
}>;
function ModalishBox({ children, className }: ModalishBoxProps) {
    const classList = [styles.ModalishBox, className];
    return (
        <div className={classList.join(' ')}>
            <div className={styles.Content}>{children}</div>
        </div>
    );
}
