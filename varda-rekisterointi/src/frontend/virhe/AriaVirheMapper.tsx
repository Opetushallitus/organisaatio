import * as React from 'react'
import { LanguageContext } from '../contexts';
import styles from './AriaVirheMapper.module.css'
import {useContext} from "react";

type Props = {
    errors: Record<string, string>,
    listId: string,
}

const mapKeyToLocalization = (key: string) => {
    switch (key) {
        case 'yhteystiedot.sahkoposti':
            return 'ORGANISAATION_SAHKOPOSTI';
        case 'yhteystiedot.puhelinnumero':
            return 'PUHELINNUMERO';
        case 'yhteystiedot.postiosoite.katuosoite':
            return 'POSTIOSOITE';
        case 'yhteystiedot.postiosoite.postinumeroUri':
            return 'POSTINUMERO';
        case 'yhteystiedot.kayntiosoite.postinumeroUri':
            return 'POSTINUMERO';
        case 'yhteystiedot.kayntiosoite.katuosoite':
            return 'KAYNTIOSOITE';
        case 'kunnat':
            return 'ORGANISAATION_KUNNAT';
        case 'sahkopostit':
            return 'ORGANISAATION_SAHKOPOSTIT';
        case 'alkuPvm':
            return 'TOIMINNAN_ALKAMISAIKA';
        case 'kotipaikkaUri':
            return 'KOTIPAIKKA';
        default:
            return key.toUpperCase();
    }
};

export default function Osoitevalidator(props: Props) {
    const { i18n } = useContext(LanguageContext);
    const errorKeys = Object.keys(props.errors);
    return(
        <div id={props.listId} className={styles.AriaVirhelista} role="alert">
            {errorKeys.map(key => {
                return <p>{`${i18n.translate(mapKeyToLocalization(key))}: ${props.errors[key]}`}</p>
            }) }
        </div>
    );
};
