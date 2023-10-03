import React, { ChangeEvent, useEffect, useRef, useState } from 'react';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import Button from '@opetushallitus/virkailija-ui-components/Button';

import { isYTunnus } from '../../../tools/ytj';
import { getByYTunnus, isYtjData, searchByName, YtjData, YtjHaku } from '../../../api/ytj';
import { warning } from '../../Notification/Notification';
import { Nimi, Perustiedot, Yhteystiedot } from '../../../types/types';
import { UseFormSetValue } from 'react-hook-form/dist/types/form';
import { BodyKehys, BodyKentta, BodyRivi } from '../ModalFields/ModalFields';
import clearIcon from '@iconify/icons-fa-solid/times-circle';
import IconWrapper from '../../IconWapper/IconWrapper';
import { getUiDateStr } from '../../../tools/mappers';
import { useAtom } from 'jotai';
import { languageAtom } from '../../../api/lokalisaatio';
import { koodistotAtom } from '../../../api/koodisto';
import Spin from '@opetushallitus/virkailija-ui-components/Spin';

type Props = {
    ytunnus: string;
    suljeModaali: (nimi: Nimi) => void;
    setters: { setPerustiedotValue: UseFormSetValue<Perustiedot>; setYhteystiedotValue: UseFormSetValue<Yhteystiedot> };
};

type KorvaaOrganisaatioProps = {
    ytjData: YtjData;
    setters: { setPerustiedotValue: UseFormSetValue<Perustiedot>; setYhteystiedotValue: UseFormSetValue<Yhteystiedot> };
    suljeModaali: (nimi: Nimi) => void;
};

const korvaaOrganisaatio = ({ ytjData, setters, suljeModaali }: KorvaaOrganisaatioProps) => {
    if (ytjData.kunta) setters.setPerustiedotValue('kotipaikka', ytjData.kunta);
    else warning({ message: 'YTJ_DATA_KOTIPAIKKA_NOT_FOUND_IN_KOODISTO' });
    if (ytjData.kieli) setters.setPerustiedotValue('kielet', [ytjData.kieli]);
    else warning({ message: 'YTJ_DATA_UNKNOWN_KIELI' });
    setters.setPerustiedotValue('ytunnus', ytjData.ytunnus);
    setters.setPerustiedotValue('yritysmuoto', ytjData.yritysmuoto);
    setters.setPerustiedotValue('alkuPvm', getUiDateStr(ytjData.aloitusPvm));
    setters.setYhteystiedotValue('fi', ytjData.yhteysTiedot);
    setters.setYhteystiedotValue('osoitteetOnEri', !!ytjData.kayntiOsoite);
    return suljeModaali({ fi: ytjData.nimi, sv: ytjData.nimi, en: ytjData.nimi });
};

export default function YTJBody({ ytunnus, suljeModaali, setters }: Props) {
    const [i18n] = useAtom(languageAtom);
    const [koodistot] = useAtom(koodistotAtom);
    const [input, setInput] = useState(ytunnus);
    const [isLoading, setIsLoading] = useState(false);
    const [ytjTiedot, setYtjTiedot] = useState<YtjHaku[]>([]);
    const inputRef = useRef<HTMLInputElement>(null);

    useEffect(() => {
        inputRef.current?.select();
    }, []);

    async function haeYtjTiedot() {
        setIsLoading(true);
        if (isYTunnus(input)) {
            const data = await getByYTunnus(input, koodistot);
            if (data) setYtjTiedot([data]);
        } else {
            const data = await searchByName(input);
            if (data) setYtjTiedot(data);
        }
        setIsLoading(false);
    }

    async function handleClick(ytjHaku: YtjHaku) {
        if (isYtjData(ytjHaku)) korvaaOrganisaatio({ ytjData: ytjHaku, setters, suljeModaali });
        else {
            setIsLoading(true);
            const ytj = await getByYTunnus(ytjHaku.ytunnus, koodistot);
            if (ytj) korvaaOrganisaatio({ ytjData: ytj, setters, suljeModaali });
        }
    }

    return (
        <BodyKehys>
            <BodyRivi>
                <BodyKentta>
                    <Input
                        ref={inputRef}
                        name={'ytjinput'}
                        onChange={(e: ChangeEvent<HTMLInputElement>) => setInput(e.target.value)}
                        value={input}
                        onKeyDown={(e: KeyboardEvent) => {
                            if (e.key === 'Enter') {
                                haeYtjTiedot();
                            }
                        }}
                        suffix={
                            input && (
                                <Button variant={'text'} style={{ boxShadow: 'none' }} onClick={() => setInput('')}>
                                    <IconWrapper color={'#999999'} icon={clearIcon} />
                                </Button>
                            )
                        }
                    />
                </BodyKentta>
                <BodyKentta>
                    <Button disabled={isLoading} onClick={haeYtjTiedot}>
                        {i18n.translate('HAE_YTJTIEDOT')}
                    </Button>
                </BodyKentta>
            </BodyRivi>
            <BodyRivi>
                {(isLoading && (
                    <BodyKentta>
                        <Spin />
                    </BodyKentta>
                )) ||
                    ytjTiedot.map((ytj) => (
                        <BodyKentta key={ytj.ytunnus}>
                            <Button key={ytj.ytunnus} onClick={() => handleClick(ytj)} variant={'text'}>
                                {`${ytj.nimi} ${ytj.ytunnus}`}
                            </Button>
                        </BodyKentta>
                    ))}
            </BodyRivi>
        </BodyKehys>
    );
}
