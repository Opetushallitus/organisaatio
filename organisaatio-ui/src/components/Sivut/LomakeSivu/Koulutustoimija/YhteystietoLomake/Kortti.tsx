import * as React from 'react';
import styles from './YhteystietoLomake.module.css';
import Input from '@opetushallitus/virkailija-ui-components/Input';

export const Kortti = (data) => {
    return (
        <div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>{i18n.translate('YHTEYSTIEDOT_POSTIOSOITE_SUOMI')} *</label>
                    <Input
                        name={`kieli_fi#1.postiOsoite`}
                        error={!!validationErrors['kieli_fi#1'] && validationErrors['kieli_fi#1'].postiOsoite}
                    />
                </div>
                <div className={styles.KenttaLyhyt}>
                    <label>{i18n.translate('YHTEYSTIEDOT_POSTINUMERO_SUOMI')}</label>
                    <Input
                        {...registerToimipaikkaUpdate(
                            postiOsoiteToimipaikkaFiName,
                            formRegister(`kieli_fi#1.postiOsoitePostiNro`)
                        )}
                        error={validationErrors['kieli_fi#1'] && !!validationErrors['kieli_fi#1'].postiOsoitePostiNro}
                    />
                </div>
                <OsoitteenToimipaikkaKentta
                    name={postiOsoiteToimipaikkaFiName}
                    labelTxt={i18n.translate('YHTEYSTIEDOT_TOIMIPAIKKA_SUOMI')}
                    control={formControl}
                />
            </div>
        </div>
    );
};
