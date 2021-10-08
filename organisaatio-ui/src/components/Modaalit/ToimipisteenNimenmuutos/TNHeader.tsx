import React, { useContext } from 'react';
import { LanguageContext } from '../../../contexts/contexts';

export default function TNHeader() {
    const { i18n } = useContext(LanguageContext);
    return <span>{i18n.translate('TOIMIPISTEEN_NIMENMUUTOS_TITLE')}</span>;
}
