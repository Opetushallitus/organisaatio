import React, { useContext, useState } from 'react';
import { LanguageContext } from '../../../contexts/contexts';
import styles from './YTJModaali.module.css';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import Axios from 'axios';
import { YtjOrganisaatio } from '../../../types/types';

type Props = {
    ytunnus: string;
    korvaaOrganisaatio: (ytiedot: YtjOrganisaatio) => void;
};

export default function YTJBody({ ytunnus = '', korvaaOrganisaatio }: Props) {
    const { i18n } = useContext(LanguageContext);
    const [yTunnus, setyTunnus] = useState(ytunnus);
    const [ytjTiedot, setYtjTiedot] = useState<YtjOrganisaatio | undefined>(undefined);

    async function haeYtjTiedot() {
        try {
            if (yTunnus) {
                const { data } = await Axios.get(`/organisaatio/ytj/${yTunnus}`);
                setYtjTiedot(data);
            }
        } catch (error) {
            console.error('error while getting ytjtieto', error);
        }
    }
    return (
        <div className={styles.BodyKehys}>
            <div className={styles.BodyRivi}>
                <Input onChange={(e) => setyTunnus(e.target.value)} value={yTunnus} />
                <Button onClick={haeYtjTiedot}>{i18n.translate('HAE_YTJTIEDOT')}</Button>
            </div>
            <div className={styles.BodyKentta}>
                {ytjTiedot && (
                    <Button onClick={() => korvaaOrganisaatio(ytjTiedot)} variant="text">
                        {`${ytjTiedot.nimi} Mitä tietoja tähän?`}
                    </Button>
                )}
            </div>
        </div>
    );
}
