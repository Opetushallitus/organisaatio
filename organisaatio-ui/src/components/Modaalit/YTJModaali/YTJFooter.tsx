import React, { useContext } from 'react';
import { LanguageContext } from '../../../contexts/contexts';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import styles from './YTJModaali.module.css';

type Props = {
    peruutaCallback?: () => void;
};

export default function YTJFooter({ peruutaCallback }: Props) {
    const { i18n } = useContext(LanguageContext);
    return (
        <div className={styles.FooterRivi}>
            <Button className={styles.FooterButton} variant="outlined" onClick={peruutaCallback}>
                {i18n.translate('BUTTON_PERUUTA')}
            </Button>
        </div>
    );
}