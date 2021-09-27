import React, { useContext } from 'react';
import { LanguageContext } from '../../../contexts/contexts';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import styles from './ToimipisteenYhdistys.module.css';

type Props = {
    tallennaCallback: (props: { newParent: string; date: string; merge: boolean }) => void;
    peruutaCallback?: () => void;
};

export default function TYFooter({ tallennaCallback, peruutaCallback }: Props) {
    const { i18n } = useContext(LanguageContext);
    return (
        <div className={styles.FooterRivi}>
            <Button className={styles.FooterButton} variant="outlined" onClick={peruutaCallback}>
                {i18n.translate('BUTTON_PERUUTA')}
            </Button>
            <Button
                className={styles.FooterButton}
                onClick={() => tallennaCallback({ newParent: '123', date: '123', merge: true })}
            >
                {i18n.translate('BUTTON_VAHVISTA')}
            </Button>
        </div>
    );
}
