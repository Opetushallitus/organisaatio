import React, { useContext } from 'react';
import { LanguageContext } from '../../../contexts/contexts';
import styles from './ToimipisteenNimenmuutos.module.css';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import { Nimi, Perustiedot } from '../../../types/types';
import { FieldErrors } from 'react-hook-form/dist/types/errors';
import { UseFormRegister } from 'react-hook-form/dist/types/form';

type TNProps = {
    nimi: Nimi;
    validationErrors: FieldErrors<Perustiedot>;
    register: UseFormRegister<Perustiedot>;
};

export default function TNBody(props: TNProps) {
    const { i18n } = useContext(LanguageContext);
    const { nimi, validationErrors, register } = props;

    return (
        <div className={styles.BodyKehys}>
            <div className={styles.BodyKentta}>
                <div className={styles.BodyKentta}>
                    <label>{i18n.translate('LABEL_SUOMEKSI')}</label>
                    <Input
                        error={!!validationErrors['nimiFi']}
                        id={'organisaation_nimiFi'}
                        {...register('nimi.fi')}
                        defaultValue={nimi && nimi.fi}
                    />
                </div>
                <div className={styles.BodyKentta}>
                    <label>{i18n.translate('LABEL_RUOTSIKSI')}</label>
                    <Input
                        error={!!validationErrors['nimiSv']}
                        id={'organisaation_nimiSv'}
                        {...register('nimi.sv')}
                        defaultValue={nimi && nimi.sv}
                    />
                </div>
                <div className={styles.BodyKentta}>
                    <label>{i18n.translate('LABEL_ENGLANNIKSI')}</label>
                    <Input
                        error={!!validationErrors['nimiEn']}
                        id={'organisaation_nimiEn'}
                        {...register('nimi.en')}
                        defaultValue={nimi && nimi.en}
                    />
                </div>
            </div>
        </div>
    );
}
