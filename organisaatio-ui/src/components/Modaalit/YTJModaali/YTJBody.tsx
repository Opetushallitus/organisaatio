import React, { useContext, useState } from 'react';
import { LanguageContext } from '../../../contexts/contexts';
import styles from './YTJModaali.module.css';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import Button from '@opetushallitus/virkailija-ui-components/Button';

import { isYTunnus } from '../../../tools/ytj';
import { YtjHaku, YtjOrganisaatio } from '../../../types/apiTypes';
import { getByYTunnus, searchByName } from '../../../api/ytj';

type Props = {
    ytunnus?: string;
    korvaaOrganisaatio: (ytiedot: YtjOrganisaatio) => void;
};
const isYtjOrganisaatio = (input: YtjHaku | YtjOrganisaatio): input is YtjOrganisaatio => {
    if ((input as YtjOrganisaatio).yritysTunnus) return true;
    return false;
};

export default function YTJBody({ ytunnus = '', korvaaOrganisaatio }: Props) {
    const { i18n } = useContext(LanguageContext);
    const [input, setInput] = useState(ytunnus);
    const [ytjTiedot, setYtjTiedot] = useState<YtjHaku[]>([]);
    async function haeYtjTiedot() {
        try {
            if (input) {
                if (isYTunnus(input)) {
                    const data = await getByYTunnus(input);
                    if (data) setYtjTiedot([data]);
                } else {
                    const data = await searchByName(input);
                    setYtjTiedot(data);
                }
            }
        } catch (error) {
            console.error('error while getting ytjtieto', error);
        }
    }
    async function handleClick(ytjHaku: YtjHaku) {
        if (isYtjOrganisaatio(ytjHaku)) korvaaOrganisaatio(ytjHaku);
        else {
            const ytj = await getByYTunnus(ytjHaku.ytunnus);
            if (ytj) korvaaOrganisaatio(ytj);
        }
    }
    return (
        <div className={styles.BodyKehys}>
            <div className={styles.BodyRivi}>
                <Input name={'ytjinput'} onChange={(e) => setInput(e.target.value)} value={input} />
                <Button onClick={haeYtjTiedot}>{i18n.translate('HAE_YTJTIEDOT')}</Button>
            </div>
            <div className={styles.BodyKentta}>
                {ytjTiedot.map((ytj) => {
                    return (
                        <Button key={ytj.ytunnus} onClick={() => handleClick(ytj)} variant="text">
                            {`${ytj.nimi} ${ytj.ytunnus}`}
                        </Button>
                    );
                })}
            </div>
        </div>
    );
}
