import * as React from 'react';
import styles from './YhdistysJaSiirto.module.css';
import { useContext } from 'react';
import { LanguageContext } from '../../../contexts/contexts';
import Input from '@opetushallitus/virkailija-ui-components/Input';

type Props = {};

export default function YhdistysJaSiirto(props: Props) {
    const { i18n } = useContext(LanguageContext);
    return (
        <div className={styles.SiirtoLomake}>
            <div className={styles.Rivi}>
                <h3>{i18n.translate('ORGANISAATION_YHDISTAMINEN_OTSIKKO')}</h3>
            </div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>{i18n.translate('YHDISTETTAVA_KOULUTUSTOIMIJA')}</label>
                    <Input value={''} />
                </div>
            </div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>{i18n.translate('NIMENMUUTOS_TULEE_VOIMAAN')}</label>
                    <Input value={''} />
                </div>
            </div>
        </div>
    );
}
