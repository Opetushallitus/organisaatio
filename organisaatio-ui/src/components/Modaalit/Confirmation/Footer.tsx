import React from 'react';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import styles from './Confirmation.module.css';
import { useAtom } from 'jotai';
import { languageAtom } from '../../../api/lokalisaatio';

type Props = {
    tallennaCallback?: () => void;
    peruutaCallback?: () => void;
};

export default function Footer({ tallennaCallback, peruutaCallback }: Props) {
    const [i18n] = useAtom(languageAtom);
    return (
        <div className={styles.FooterRivi}>
            <Button className={styles.FooterButton} variant={'outlined'} onClick={peruutaCallback}>
                {i18n.translate('BUTTON_PERUUTA')}
            </Button>
            <Button className={styles.FooterButton} onClick={tallennaCallback}>
                {i18n.translate('BUTTON_VAHVISTA')}
            </Button>
        </div>
    );
}
