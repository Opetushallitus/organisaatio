import React, { useContext, useState } from 'react';
import { KoodistoContext } from '../../../contexts/KoodistoContext';
import { LanguageContext } from '../../../contexts/LanguageContext';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import Button from '@opetushallitus/virkailija-ui-components/Button';

import { isYTunnus } from '../../../tools/ytj';
import { getByYTunnus, isYtjData, searchByName, YtjHaku } from '../../../api/ytj';
import { warning } from '../../Notification/Notification';
import { Perustiedot, Yhteystiedot } from '../../../types/types';
import { UseFormSetValue } from 'react-hook-form/dist/types/form';
import { BodyKehys, BodyKentta, BodyRivi } from '../ModalFields/ModalFields';
import { Icon } from '@iconify/react';
import clearIcon from '@iconify/icons-fa-solid/times-circle';

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
    setters.setPerustiedotValue('lyhytNimi', { fi: ytjData.nimi, sv: ytjData.nimi, en: ytjData.nimi });
    setters.setPerustiedotValue('alkuPvm', ytjData.aloitusPvm);
    setters.setYhteystiedotValue('fi', ytjData.yhteysTiedot);
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
            if (data) setYtjTiedot(data);
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
        <BodyKehys>
            <BodyRivi>
                <BodyKentta>
                    <Input
                        name={'ytjinput'}
                        onChange={(e) => setInput(e.target.value)}
                        value={input}
                        onKeyDown={(e) => {
                            if (e.key === 'Enter') {
                                haeYtjTiedot();
                            }
                        }}
                        suffix={
                            input && (
                                <Button variant={'text'} style={{ boxShadow: 'none' }} onClick={() => setInput('')}>
                                    <Icon color={'#999999'} icon={clearIcon} />
                                </Button>
                            )
                        }
                    />
                </BodyKentta>
                <BodyKentta>
                    <Button onClick={haeYtjTiedot}>{i18n.translate('HAE_YTJTIEDOT')}</Button>
                </BodyKentta>
            </BodyRivi>
            <BodyRivi>
                {ytjTiedot.map((ytj) => {
                    return (
                        <BodyKentta key={ytj.ytunnus}>
                            <Button key={ytj.ytunnus} onClick={() => handleClick(ytj)} variant={'text'}>
                                {`${ytj.nimi} ${ytj.ytunnus}`}
                            </Button>
                        </BodyKentta>
                    );
                })}
            </BodyRivi>
        </BodyKehys>
    );
}
