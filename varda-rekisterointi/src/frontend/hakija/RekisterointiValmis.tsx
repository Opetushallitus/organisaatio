import React, { useContext } from 'react';
import { LanguageContext } from '../contexts';
import { ReactComponent as Image } from './hakemus odottaa hyväksyntää.svg';

export default function RekisterointiValmis() {
    const { i18n } = useContext(LanguageContext);
    return (
        <form>
            <fieldset className="oph-fieldset">
                <legend className="oph-label">{i18n.translate('REKISTEROINNIN_KASITTELY')}</legend>
            </fieldset>
            <div>{i18n.translate('REKISTEROINNIN_KASITTELY_KUVAUS')}</div>
            <div>{i18n.translate('REKISTEROINNIN_KASITTELY_OHJE')}</div>
            <div className="center"><Image /></div>
        </form>
    );
}
