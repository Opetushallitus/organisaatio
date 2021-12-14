import React, { useContext } from 'react';
import { LanguageContext } from '../../../contexts/LanguageContext';

export default function UOTHeader() {
    const { i18n } = useContext(LanguageContext);
    return <>{i18n.translate('UUDEN_OSOITETYYPIN_LISAYS')}</>;
}
