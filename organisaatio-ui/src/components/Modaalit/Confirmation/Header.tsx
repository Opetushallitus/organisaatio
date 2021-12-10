import React, { useContext } from 'react';
import { LanguageContext } from '../../../contexts/LanguageContext';

export default function Header({ headerKey }: { headerKey: string }) {
    const { i18n } = useContext(LanguageContext);
    return <>{i18n.translate(headerKey)}</>;
}
