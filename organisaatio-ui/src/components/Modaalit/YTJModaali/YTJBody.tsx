import React, {useContext, useState} from "react";
import {LanguageContext} from "../../../contexts/contexts";
import styles from './YTJModaali.module.css';
import Input from "@opetushallitus/virkailija-ui-components/Input";
import Button from "@opetushallitus/virkailija-ui-components/Button";
import Axios from "axios";

type Props = {
    ytunnus: string
}

const urlPrefix = process.env.NODE_ENV === 'development' ? '/api' : '/organisaatio';

export default function YTJBody({ ytunnus= "" }: Props) {
    const { i18n} = useContext(LanguageContext);
    const [yTunnus, setyTunnus] = useState(ytunnus);
    const [yTiedot, setyTiedot] = useState({});

    async function haeYtjTiedot() {
        try {
            if (yTunnus) {
                const response = await Axios.get( `${urlPrefix}/ytj/${yTunnus}`);
                console.log('got ytjtieto', response, yTiedot);
                setyTiedot(response);
            }
        } catch (error) {
            console.error('error while getting ytjtieto', error)
        }
    }
    return (
        <div className={styles.BodyKehys}>
                <div className={styles.BodyKentta}>
                    <Input onChange={((e) => setyTunnus(e.target.value))} value={yTunnus}/>
                    <Button onClick={haeYtjTiedot}>{i18n.translate('HAE_YTJTIEDOT')}</Button>
                </div>
            <div className={styles.BodyKentta}>
                <p>fsdddsf
                </p>
            </div>
        </div>
    );
}