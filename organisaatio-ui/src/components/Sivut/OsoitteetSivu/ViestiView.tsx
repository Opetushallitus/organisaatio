import FormLabel from '@opetushallitus/virkailija-ui-components/FormLabel';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import Textarea from '@opetushallitus/virkailija-ui-components/Textarea';
import React from 'react';
import styles from './ViestiView.module.css';

export const ViestiView = () => {
    return (
        <div className={styles.ViestiView}>
            <div className={styles.Header}>
                <h2>Kirjoita Viesti</h2>
            </div>
            <div className={styles.Content}>
                <div className={styles.Row}>
                    <div className={styles.Column}>
                        <FormLabel>Lähettäjä*</FormLabel>
                        <Input defaultValue={'Opetushallitus'} type={'text'} disabled></Input>
                    </div>
                    <div className={styles.Column}>
                        <FormLabel>Lähetysosoite*</FormLabel>
                        <Input defaultValue={'noreply'} type={'text'} disabled></Input>
                    </div>
                </div>
                <div className={styles.Row}>
                    <div className={styles.Column}>
                        <FormLabel>Vastausosoite (reply-to)</FormLabel>
                        <Input type={'text'}></Input>
                    </div>
                    <div className={styles.Column}>
                        <FormLabel>Kopio-osoite</FormLabel>
                        <Input type={'text'}></Input>
                    </div>
                </div>
                <div className={styles.Row}>
                    <div className={styles.Column}>
                        <FormLabel>Aihe*</FormLabel>
                        <Input type={'text'}></Input>
                    </div>
                </div>
                <div className={styles.Row}>
                    <div className={styles.Column}>
                        <FormLabel>Viesti*</FormLabel>
                        <div className={styles.Viesti}>
                            <Textarea className={styles.ViestiTextarea}></Textarea>
                            <div className={styles.ViestiFooter}>
                                <strong>Osoitelähde:</strong> OPH Opintopolku (Organisaatiopalvelu). Osoitetta käytetään
                                Opetushallituksen ja Opetus- ja kulttuuriministeriön viralliseen viestintään.
                                <br />
                                <strong>Adresskälla:</strong> Utbildningsstyrelsen Studieinfo (Organisationstjänst).
                                Utbildningsstyrelsen och undervisnings- och kulturministeriet använder adressen i sin
                                kommunikation till skolorna och skolornas administratörer.
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};
