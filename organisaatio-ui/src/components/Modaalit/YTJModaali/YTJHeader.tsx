import React, { useContext } from 'react';
import { LanguageContext } from '../../../contexts/contexts';

export default function YTJHeader() {
    const { i18n } = useContext(LanguageContext);
    return <>{i18n.translate('VALITSE_ORGANISAATIO')}</>;
}
