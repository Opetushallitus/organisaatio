import React, { useContext } from 'react';
import { LanguageContext } from '../../../contexts/contexts';

export default function TYHeader() {
    const { i18n } = useContext(LanguageContext);
    return <h3>{i18n.translate('TOIMIPISTEEN_YHDISTYS_TITLE')}</h3>;
}
