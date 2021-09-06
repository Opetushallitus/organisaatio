import React, { useContext } from 'react';
import { LanguageContext } from '../../../contexts/contexts';
import styles from './ToimipisteenLakkautus.module.css';
import Input from '@opetushallitus/virkailija-ui-components/Input';

export default function TLBody() {
    const { i18n } = useContext(LanguageContext);
    return (
        <div className={styles.BodyKehys}>
            <div className={styles.BodyKentta}>
                <label>{i18n.translate('TOIMIPISTEEN_LAKKAUTUS_PVM')}</label>
                <Input value={''} />
            </div>
        </div>
    );
}
