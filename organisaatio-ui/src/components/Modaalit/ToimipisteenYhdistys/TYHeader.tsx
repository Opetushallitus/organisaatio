import React, { useContext } from 'react';
import { LanguageContext } from '../../../contexts/contexts';

export default function TYHeader({ titleKey }: { titleKey: string }) {
    const { i18n } = useContext(LanguageContext);
    return <h3>{i18n.translate(titleKey)}</h3>;
}