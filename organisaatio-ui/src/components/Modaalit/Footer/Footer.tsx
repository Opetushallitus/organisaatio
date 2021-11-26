import React, { useContext } from 'react';
import { LanguageContext } from '../../../contexts/contexts';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import styles from './Footer.module.css';

type Props = {
    tallennaCallback?: () => void;
    peruutaCallback: () => void;
};

export default function Footer({ tallennaCallback, peruutaCallback }: Props) {
    const { i18n } = useContext(LanguageContext);
    return (
        <div className={styles.FooterRivi}>
            <Button className={styles.FooterButton} variant="outlined" onClick={peruutaCallback}>
                {i18n.translate('BUTTON_PERUUTA')}
            </Button>
            {tallennaCallback && (
                <Button className={styles.FooterButton} onClick={tallennaCallback}>
                    {i18n.translate('BUTTON_VAHVISTA')}
                </Button>
            )}
        </div>
    );
}
