import React, { useContext } from 'react';
import { LanguageContext } from '../../../contexts/contexts';
import styles from './ToimipisteenNimenmuutos.module.css';
import Input from '@opetushallitus/virkailija-ui-components/Input';

interface NimiType {
    fi: 'fi';
    sv: 'sv';
    en: 'en';
}

type TNProps = {
    handleChange: (nimi: { fi: string; sv: string; en: string }) => void;
    nimi: { fi: string; sv: string; en: string };
};

export default function TNUusiBody(props: TNProps) {
    const { i18n } = useContext(LanguageContext);
    const { handleChange, nimi } = props;
    const handleOnChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const input = e.target as HTMLInputElement;
        const inputinNimi = input.name as keyof NimiType;
        const uusiNimi = Object.assign({}, nimi);
        uusiNimi[inputinNimi] = input.value;
        return handleChange(uusiNimi);
    };
    return (
        <div className={styles.BodyKehys}>
            <div className={styles.BodyKentta}>
                <div className={styles.BodyKentta}>
                    <label>{i18n.translate('LABEL_SUOMEKSI')}</label>
                    <Input name="fi" value={nimi.fi || ''} onChange={handleOnChange} />
                </div>
                <div className={styles.BodyKentta}>
                    <label>{i18n.translate('LABEL_RUOTSIKSI')}</label>
                    <Input name="sv" value={nimi.sv || ''} onChange={handleOnChange} />
                </div>
                <div className={styles.BodyKentta}>
                    <label>{i18n.translate('LABEL_ENGLANNIKSI')}</label>
                    <Input name="en" value={nimi.en || ''} onChange={handleOnChange} />
                </div>
            </div>
        </div>
    );
}