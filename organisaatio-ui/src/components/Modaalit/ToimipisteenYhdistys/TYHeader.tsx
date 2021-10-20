import React, { useContext } from 'react';
import { LanguageContext } from '../../../contexts/contexts';

export default function TYHeader({ titleKey }: { titleKey: string }) {
    const { i18n } = useContext(LanguageContext);
    return <>{i18n.translate(titleKey)}</>;
}
