import React, { useContext } from 'react';
import { LanguageContext } from '../../../contexts/contexts';
import styles from './ToimipisteenNimenmuutos.module.css';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import { Nimi } from '../../../types/types';
import { FieldErrors } from 'react-hook-form/dist/types/errors';
import { UseFormRegister } from 'react-hook-form/dist/types/form';

type TNProps = {
    validationErrors: FieldErrors<Nimi>;
    register: UseFormRegister<Nimi>;
};

export default function TNBody(props: TNProps) {
    const { i18n } = useContext(LanguageContext);
    const { validationErrors, register } = props;
    return (
        <div className={styles.BodyKehys}>
            <div className={styles.BodyKentta}>
                <div className={styles.BodyKentta}>
                    <label>{i18n.translate('LABEL_SUOMEKSI')} *</label>
                    <Input error={!!validationErrors['fi']} id={'organisaation_nimiFi'} {...register('fi')} />
                </div>
                <div className={styles.BodyKentta}>
                    <label>{i18n.translate('LABEL_RUOTSIKSI')} *</label>
                    <Input error={!!validationErrors['sv']} id={'organisaation_nimiSv'} {...register('sv')} />
                </div>
                <div className={styles.BodyKentta}>
                    <label>{i18n.translate('LABEL_ENGLANNIKSI')} *</label>
                    <Input error={!!validationErrors['en']} id={'organisaation_nimiEn'} {...register('en')} />
                </div>
            </div>
        </div>
    );
}
