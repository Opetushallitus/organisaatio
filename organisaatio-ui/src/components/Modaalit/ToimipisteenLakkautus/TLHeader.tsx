import React, { useContext } from 'react';
import { LanguageContext } from '../../../contexts/contexts';

export default function TLHeader() {
    const { i18n } = useContext(LanguageContext);
    return <h3>{i18n.translate('TOIMIPISTEEN_LAKKAUTUS_TITLE')}</h3>;
}
