import React, { useContext } from 'react';
import { LanguageContext } from '../contexts';

export default function RekisterointiValmis() {
    const { i18n } = useContext(LanguageContext);
    return (
        <form>
            <fieldset className="oph-fieldset">
                <legend className="oph-label">{i18n.translate('REKISTEROINNIN_KASITTELY')}</legend>
            </fieldset>
            <div>{i18n.translate('REKISTEROINNIN_KASITTELY_KUVAUS')}</div>
            <div>{i18n.translate('REKISTEROINNIN_KASITTELY_OHJE')}</div>
        </form>
    );
}
