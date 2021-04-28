import React, { useContext } from 'react';
import { LanguageContext } from '../../../contexts/contexts';

export default function UOTHeader() {
    const { i18n } = useContext(LanguageContext);
    return <h3>{i18n.translate('UUDEN_OSOITETYYPIN_LISAYS')}</h3>;
}
