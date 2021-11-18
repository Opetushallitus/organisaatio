import React, { useContext, useState } from 'react';
import { KoodistoContext, LanguageContext } from '../../../contexts/contexts';
import styles from './YTJModaali.module.css';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import Button from '@opetushallitus/virkailija-ui-components/Button';

import { isYTunnus } from '../../../tools/ytj';
import { getByYTunnus, isYtjData, searchByName, YtjHaku } from '../../../api/ytj';
import { warning } from '../../Notification/Notification';
import { Perustiedot, Yhteystiedot } from '../../../types/types';
import { UseFormSetValue } from 'react-hook-form/dist/types/form';

type Props = {
    ytunnus: string;
    suljeModaali: () => void;
    setters: { setPerustiedotValue: UseFormSetValue<Perustiedot>; setYhteystiedotValue: UseFormSetValue<Yhteystiedot> };
};

const korvaaOrganisaatio = ({ ytjData, setters, suljeModaali }) => {
    if (ytjData.kunta) setters.setPerustiedotValue('kotipaikka', ytjData.kunta);
    else warning({ message: 'YTJ_DATA_KOTIPAIKKA_NOT_FOUND_IN_KOODISTO' });
    if (ytjData.kieli) setters.setPerustiedotValue('kielet', [ytjData.kieli]);
    else warning({ message: 'YTJ_DATA_UNKNOWN_KIELI' });
    setters.setPerustiedotValue('ytunnus', ytjData.ytunnus);
    setters.setPerustiedotValue('nimi', { fi: ytjData.nimi, sv: ytjData.nimi, en: ytjData.nimi });
    setters.setPerustiedotValue('alkuPvm', ytjData.aloitusPvm);
    setters.setYhteystiedotValue('fi', ytjData.yhteysTiedot); // TODO fix this to reflect kielet
    setters.setYhteystiedotValue('osoitteetOnEri', !!ytjData.kayntiOsoite);
    suljeModaali();
};

export default function YTJBody({ ytunnus, suljeModaali, setters }: Props) {
    const { i18n } = useContext(LanguageContext);
    const koodistot = useContext(KoodistoContext);
    const [input, setInput] = useState(ytunnus);
    const [ytjTiedot, setYtjTiedot] = useState<YtjHaku[]>([]);
    async function haeYtjTiedot() {
        if (isYTunnus(input)) {
            const data = await getByYTunnus(input, koodistot);
            if (data) setYtjTiedot([data]);
        } else {
            const data = await searchByName(input);
            setYtjTiedot(data);
        }
    }
    async function handleClick(ytjHaku: YtjHaku) {
        if (isYtjData(ytjHaku)) korvaaOrganisaatio({ ytjData: ytjHaku, setters, suljeModaali });
        else {
            const ytj = await getByYTunnus(ytjHaku.ytunnus, koodistot);
            if (ytj) korvaaOrganisaatio({ ytjData: ytj, setters, suljeModaali });
        }
    }
    return (
        <div className={styles.BodyKehys}>
            <div className={styles.BodyRivi}>
                <Input name={'ytjinput'} onChange={(e) => setInput(e.target.value)} value={input} />
                <Button onClick={haeYtjTiedot}>{i18n.translate('HAE_YTJTIEDOT')}</Button>
            </div>
            {ytjTiedot.map((ytj) => {
                return (
                    <div key={ytj.ytunnus} className={styles.BodyKentta}>
                        <Button key={ytj.ytunnus} onClick={() => handleClick(ytj)} variant="text">
                            {`${ytj.nimi} ${ytj.ytunnus}`}
                        </Button>
                    </div>
                );
            })}
        </div>
    );
}
